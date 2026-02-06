package com.example.deviceservice.controller;

import com.example.deviceservice.dto.request.DeviceRequest;
import com.example.deviceservice.dto.response.DeviceResponse;
import com.example.deviceservice.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_write')")
    public ResponseEntity<DeviceResponse> createDevice(@RequestBody DeviceRequest deviceRequest) {
        DeviceResponse deviceResponse = deviceService.createOrUpdateDevice(deviceRequest);
        return ResponseEntity.ok(deviceResponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_write')")
    public ResponseEntity<DeviceResponse> updateDevice(@RequestBody DeviceRequest deviceRequest, @PathVariable String id) {
        DeviceResponse deviceResponse = deviceService.updateDevice(deviceRequest, id);
        return ResponseEntity.ok(deviceResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_read')")
    public ResponseEntity<DeviceResponse> getDeviceById(@PathVariable String id) {
        DeviceResponse deviceResponse = deviceService.getDeviceById(id);
        return ResponseEntity.ok(deviceResponse);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_read')")
    public ResponseEntity<List<DeviceResponse>> getDevices() {
        List<DeviceResponse> deviceResponses = deviceService.getDevices();
        return ResponseEntity.ok(deviceResponses);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_write')")
    public ResponseEntity<Void> deleteDeviceById(@PathVariable String id) {
        deviceService.deleteDeviceById(id);
        return ResponseEntity.noContent().build();
    }
}
