package com.example.demo.services;

import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.dtos.builders.DeviceBuilder;
import com.example.demo.entities.Device;
import com.example.demo.entities.UserMapping;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.DeviceRepository;
import com.example.demo.repositories.UserMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);

    private final DeviceRepository deviceRepository;
    private final UserMappingRepository userMappingRepository;

    private final DeviceSyncPublisher deviceSyncPublisher; // NEW

    @Autowired
    public DeviceService(DeviceRepository deviceRepository, UserMappingRepository userMappingRepository,DeviceSyncPublisher deviceSyncPublisher) {
        this.deviceRepository = deviceRepository;
        this.userMappingRepository = userMappingRepository;
        this.deviceSyncPublisher = deviceSyncPublisher;
    }

    //UserMapping Sync Methods
    @Transactional
    public void createUserMapping(UUID userId) {
        if (userMappingRepository.existsById(userId)) {
            LOGGER.warn("UserMapping for ID {} already exists.", userId);
            return;
        }
        userMappingRepository.save(new UserMapping(userId));
        LOGGER.info("Created UserMapping for ID {}", userId);
    }

    @Transactional
    public void deleteUserMapping(UUID userId) {
        // Deleting the user mapping will set associated devices' FK to NULL
        if (!userMappingRepository.existsById(userId)) {
            LOGGER.warn("UserMapping for ID {} not found. Cannot delete.", userId);
            return;
        }
        userMappingRepository.deleteById(userId);
        LOGGER.info("Deleted UserMapping for ID {}", userId);
    }

    // Device CRUD Methods
    public List<DeviceDTO> findDevices() {
        return deviceRepository.findAll().stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public List<DeviceDTO> findDevicesByUserId(UUID userId) {
        return deviceRepository.findDevicesByUserId(userId).stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public DeviceDTO findDeviceById(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id));
        return DeviceBuilder.toDeviceDTO(device);
    }

    public UUID insert(DeviceDetailsDTO deviceDTO) {
        Device device = new Device();
        device.setName(deviceDTO.getName());
        device.setMaxConsumption(deviceDTO.getMaxConsumption());

        // Find the UserMapping object and link it
        if (deviceDTO.getUserId() != null) {
            UserMapping userMapping = userMappingRepository.findById(deviceDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("UserMapping not found for id: " + deviceDTO.getUserId()));
            device.setUserMapping(userMapping);
        }

        device = deviceRepository.save(device);
        LOGGER.debug("Device with id {} was inserted in db", device.getId());

        // NEW: Publish sync event to RabbitMQ
        deviceSyncPublisher.publishDeviceCreated(device.getId());

        return device.getId();
    }

    public DeviceDTO update(UUID id, DeviceDetailsDTO deviceDTO) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id));

        device.setName(deviceDTO.getName());
        device.setMaxConsumption(deviceDTO.getMaxConsumption());

        // This is how we "Assign devices to users"
        if (deviceDTO.getUserId() != null) {
            UserMapping userMapping = userMappingRepository.findById(deviceDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("UserMapping not found for id: " + deviceDTO.getUserId()));
            device.setUserMapping(userMapping);
        } else {
            device.setUserMapping(null); // Un-assign device
        }

        device = deviceRepository.save(device);
        LOGGER.debug("Device with id {} was updated in db", device.getId());
        return DeviceBuilder.toDeviceDTO(device);
    }

    public void delete(UUID id) {
        if (!deviceRepository.existsById(id)) {
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        deviceRepository.deleteById(id);
        LOGGER.debug("Device with id {} was deleted from db", id);
        // NEW: Publish sync event to RabbitMQ
        deviceSyncPublisher.publishDeviceDeleted(id);
    }
}