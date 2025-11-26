package com.example.demo.controllers;

import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.services.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/devices")
@Validated
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    // Admin: all devices
    @Operation(summary = "Get all devices", description = "Returns list of all devices")
    @ApiResponse(responseCode = "200", description = "Devices retrieved")
    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getDevices() {
        return ResponseEntity.ok(deviceService.findDevices());
    }

    // Admin: create a device
    @Operation(summary = "Create device", description = "Creates new device")
    @ApiResponse(responseCode = "201", description = "Device created")
    @PostMapping
    public ResponseEntity<UUID> createDevice(@Valid @RequestBody DeviceDetailsDTO deviceDTO) {
        UUID id = deviceService.insert(deviceDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).body(id); // 201 Created
    }

    // Admin: get a device
    @Operation(summary = "Get device by ID", description = "Returns specific device")
    @ApiResponse(responseCode = "200", description = "Device retrieved")
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDTO> getDevice(@PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.findDeviceById(id));
    }

    // admin: update a device (si pt "Assign devices to users")
    @Operation(summary = "Update device", description = "Updates existing device")
    @ApiResponse(responseCode = "200", description = "Device updated")
    @PutMapping("/{id}")
    public ResponseEntity<DeviceDTO> updateDevice(@PathVariable UUID id, @Valid @RequestBody DeviceDetailsDTO deviceDTO) {
        DeviceDTO updatedDevice = deviceService.update(id, deviceDTO);
        return ResponseEntity.ok(updatedDevice); // 200 OK
    }

    // Admin: delete a device
    @Operation(summary = "Delete device", description = "Deletes device")
    @ApiResponse(responseCode = "204", description = "Device deleted")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // Client: get devices for a specific user
    @Operation(summary = "Get user devices", description = "Returns devices assigned to user")
    @ApiResponse(responseCode = "200", description = "Devices retrieved")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DeviceDTO>> getDevicesByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(deviceService.findDevicesByUserId(userId));
    }
}