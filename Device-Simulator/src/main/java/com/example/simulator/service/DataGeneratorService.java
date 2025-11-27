package com.example.simulator.service;

import com.example.simulator.model.SensorReading;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class DataGeneratorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataGeneratorService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final Random random;

    @Value("${app.queue.sensor-data}")
    private String queueName;

    @Value("${simulator.device-id}")
    private String deviceId;


    @Value("${simulator.fast-demo:false}")
    private boolean fastDemo;

    // State to track the simulated time
    private LocalDateTime currentSimulatedTime;


    public DataGeneratorService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = new ObjectMapper();
        this.random = new Random();
        this.currentSimulatedTime = LocalDateTime.now();
    }


    @Scheduled(fixedRate = 5000)
    @ConditionalOnProperty(name = "simulator.fast-demo", havingValue = "true")
    public void scheduleFast() {
        processAndSend();
    }


    @Scheduled(fixedRate = 600000)
    @ConditionalOnProperty(name = "simulator.fast-demo", havingValue = "false", matchIfMissing = true)
    public void scheduleNormal() {
        processAndSend();
    }


    private void processAndSend() {
        try {

            if (fastDemo) {

                currentSimulatedTime = currentSimulatedTime.plusMinutes(10);
            } else {

                currentSimulatedTime = LocalDateTime.now();
            }

            String timestamp = currentSimulatedTime.format(TIMESTAMP_FORMAT);

            // Generate measurement value
            double measurementValue = generateMeasurement(currentSimulatedTime.getHour());

            // Create sensor reading
            SensorReading reading = new SensorReading(timestamp, deviceId, measurementValue);

            // Convert to JSON and send
            String jsonMessage = objectMapper.writeValueAsString(reading);
            rabbitTemplate.convertAndSend(queueName, jsonMessage);

            LOGGER.info("Sent: {} | Device: {} | Value: {} kWh",
                    timestamp, deviceId, measurementValue);

        } catch (Exception e) {
            LOGGER.error("Error generating/sending sensor data", e);
        }
    }

    /**

     * - Night (0-6): Low consumption (0.1 - 0.5 kWh)
     * - Morning (6-9): Rising consumption (0.5 - 1.5 kWh)
     * - Day (9-17): Medium consumption (0.8 - 1.2 kWh)
     * - Evening (17-22): Peak consumption (1.5 - 3.0 kWh)
     * - Late night (22-24): Decreasing (0.5 - 1.0 kWh)
     */
    private double generateMeasurement(int hour) {

        double baseValue;
        double variance;

        if (hour >= 0 && hour < 6) {
            // Night: low consumption
            baseValue = 0.2;
            variance = 0.3;
        } else if (hour >= 6 && hour < 9) {
            // Morning: rising
            baseValue = 0.8;
            variance = 0.7;
        } else if (hour >= 9 && hour < 17) {
            // Daytime: medium
            baseValue = 0.9;
            variance = 0.4;
        } else if (hour >= 17 && hour < 22) {
            // Evening: peak
            baseValue = 2.0;
            variance = 1.0;
        } else {
            // Late night: decreasing
            baseValue = 0.6;
            variance = 0.4;
        }

        // Add random fluctuation
        double fluctuation = (random.nextDouble() - 0.5) * variance;
        double value = baseValue + fluctuation;

        // Ensure non-negative
        return Math.max(0.05, value);
    }
}