package com.example.apiorchestrator.adapter.in.controller;

import com.example.apiorchestrator.domain.port.in.DeviceUseCase;
import com.example.deviceservice.model.DeviceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceUseCase deviceUseCase;

    @GetMapping("/{device_id}")
    public Mono<DeviceResponse> getDevice(@PathVariable("device_id") String deviceId) {
        return deviceUseCase.getDevice(deviceId).log();
    }

    @GetMapping
    public Flux<DeviceResponse> getDevices() {
        return deviceUseCase.getDevices().log();
    }
}
