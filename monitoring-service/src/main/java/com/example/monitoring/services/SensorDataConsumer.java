package com.example.monitoring.services;

import com.example.monitoring.dtos.SensorDataDTO;
import com.example.monitoring.entities.DeviceMapping;
import com.example.monitoring.entities.HourlyConsumption;
import com.example.monitoring.repositories.DeviceMappingRepository;
import com.example.monitoring.repositories.HourlyConsumptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;


@Service
public class SensorDataConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorDataConsumer.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final HourlyConsumptionRepository hourlyConsumptionRepository;
    private final ObjectMapper objectMapper;

    private final DeviceMappingRepository deviceMappingRepository;
    private final RabbitTemplate rabbitTemplate;

    public SensorDataConsumer(HourlyConsumptionRepository hourlyConsumptionRepository,
                              DeviceMappingRepository deviceMappingRepository,
                              RabbitTemplate rabbitTemplate) {
        this.hourlyConsumptionRepository = hourlyConsumptionRepository;
        this.objectMapper = new ObjectMapper();
        this.deviceMappingRepository = deviceMappingRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "${app.queue.sensor-data}")
    @Transactional
    public void receiveSensorData(String jsonMessage) {
        try {
            LOGGER.debug("Received message: {}", jsonMessage);

            // Parse JSON message
            SensorDataDTO sensorData = objectMapper.readValue(jsonMessage, SensorDataDTO.class);
            LOGGER.info("Parsed sensor data: {}", sensorData);

            // Parse timestamp
            LocalDateTime timestamp = LocalDateTime.parse(sensorData.getTimestamp(), TIMESTAMP_FORMAT);
            LocalDate date = timestamp.toLocalDate();
            int hour = timestamp.getHour();

            // Parse device ID
            UUID deviceId = UUID.fromString(sensorData.getDeviceId());

            // Get measurement value
            Double measurementValue = sensorData.getMeasurementValue();

            // Aggregate into hourly consumption (upsert logic)
            aggregateHourlyConsumption(deviceId, date, hour, measurementValue);

        } catch (Exception e) {
            LOGGER.error("Error processing sensor data message: {}", jsonMessage, e);
        }
    }

    private void aggregateHourlyConsumption(UUID deviceId, LocalDate date, int hour, Double measurementValue) {
        // Try to find existing record for this device/date/hour
        Optional<HourlyConsumption> existingRecord =
                hourlyConsumptionRepository.findByDeviceIdAndDateAndHour(deviceId, date, hour);
        double newTotal = 0;
        if (existingRecord.isPresent()) {
            // Update existing record
            HourlyConsumption record = existingRecord.get();
            newTotal = record.getTotalConsumption() + measurementValue;
            record.setTotalConsumption(newTotal);
            hourlyConsumptionRepository.save(record);
            LOGGER.debug("Updated hourly consumption: deviceId={}, date={}, hour={}, total={}",
                    deviceId, date, hour, record.getTotalConsumption());
        } else {
            // Create new record
            newTotal = measurementValue;
            HourlyConsumption newRecord = new HourlyConsumption(deviceId, date, hour, measurementValue);
            hourlyConsumptionRepository.save(newRecord);
            LOGGER.debug("Created new hourly consumption: deviceId={}, date={}, hour={}, value={}",
                    deviceId, date, hour, measurementValue);
        }
        checkAndSendNotification(deviceId, newTotal);
    }

    private void checkAndSendNotification(UUID deviceId, double currentConsumption) {
        Optional<DeviceMapping> deviceOpt = deviceMappingRepository.findById(deviceId);

        if (deviceOpt.isPresent()) {
            DeviceMapping device = deviceOpt.get();
            double limit = device.getMaxHourlyConsumption() != null ? device.getMaxHourlyConsumption() : Double.MAX_VALUE;

            if (currentConsumption > limit) {
                LOGGER.warn("Overconsumption detected for device {}! Current: {}, Max: {}", deviceId, currentConsumption, limit);

                // Create a simple DTO or JSON string for the notification
                // Matching the structure the ChatService expects (see ChatMessage DTO or specific Notification DTO)
                try {
                    // Constructing a simple JSON payload for the Chat Service
                    // The Chat Service listens to "overconsumption.notifications"
                    String userId = device.getUserId() != null ? device.getUserId().toString() : "null";

                    String messageJson = String.format(
                            "{\"deviceId\":\"%s\", \"userId\":\"%s\", \"current\":%.2f, \"max\":%.2f}",
                            deviceId, userId, currentConsumption, limit
                    );

                    rabbitTemplate.convertAndSend("overconsumption.notifications", messageJson);

                } catch (Exception e) {
                    LOGGER.error("Failed to send notification", e);
                }
            }
        }
    }
}