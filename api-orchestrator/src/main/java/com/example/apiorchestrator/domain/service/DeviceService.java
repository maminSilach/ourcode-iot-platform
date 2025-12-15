package com.example.apiorchestrator.domain.service;

import com.example.apiorchestrator.domain.dto.request.DeviceVersionRequest;
import com.example.apiorchestrator.domain.dto.response.DeviceVersionResponse;
import com.example.apiorchestrator.domain.port.in.DeviceUseCase;
import com.example.apiorchestrator.domain.port.out.DeviceClient;
import com.example.deviceservice.model.DeviceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService implements DeviceUseCase {

    private final DeviceClient deviceClient;

    @Override
    public Mono<DeviceResponse> getDevice(String deviceId) {
        return Mono.fromCallable(
                () -> deviceClient.getDevice(deviceId)
        ).subscribeOn(
                Schedulers.boundedElastic()
        );
    }

    @Override
    public Flux<DeviceResponse> getDevices() {
        return Flux.defer(
                () -> Flux.fromIterable(deviceClient.getDevices())
        ).subscribeOn(
                Schedulers.boundedElastic()
        );
    }

    @Override
    public Mono<DeviceVersionResponse> updateDeviceVersion(DeviceVersionRequest deviceRequest) {
        return null;
    }
}
