package com.example.apiorchestrator.adapter.out;

import com.example.apiorchestrator.domain.port.out.DeviceClient;
import com.example.deviceservice.api.DeviceApi;
import com.example.deviceservice.model.DeviceResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceFeignClient implements DeviceClient {

    private final DeviceApi deviceApi;

    @Override
    @Retry(name = "default")
    @CircuitBreaker(name = "default")
    public DeviceResponse getDevice(String deviceId) {
        log.info("Get device by id: {}", deviceId);
        return deviceApi.getDeviceById(deviceId);
    }

    @Override
    @Retry(name = "default")
    @CircuitBreaker(name = "default")
    public List<DeviceResponse> getDevices() {
        return deviceApi.getDevices();
    }
}
