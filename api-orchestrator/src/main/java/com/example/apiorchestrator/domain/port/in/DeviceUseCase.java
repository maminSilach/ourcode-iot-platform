package com.example.apiorchestrator.domain.port.in;

import com.example.apiorchestrator.domain.dto.request.DeviceVersionRequest;
import com.example.apiorchestrator.domain.dto.response.DeviceVersionResponse;
import com.example.deviceservice.model.DeviceResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeviceUseCase {

    Mono<DeviceResponse> getDevice(String deviceId);

    Flux<DeviceResponse> getDevices();

    Mono<DeviceVersionResponse> updateDeviceVersion(DeviceVersionRequest deviceRequest);

}
