package com.example.apiorchestrator.domain.port.out;

import com.example.deviceservice.model.DeviceResponse;

import java.util.List;

public interface DeviceClient {

    DeviceResponse getDevice(String deviceId);

    List<DeviceResponse> getDevices();
}
