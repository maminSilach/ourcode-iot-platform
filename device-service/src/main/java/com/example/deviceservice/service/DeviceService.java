package com.example.deviceservice.service;

import com.example.deviceservice.dto.request.DeviceRequest;
import com.example.deviceservice.dto.response.DeviceResponse;
import com.example.deviceservice.exception.DeviceNotFoundException;
import com.example.deviceservice.mapper.DeviceMapper;
import com.example.deviceservice.model.Device;
import com.example.deviceservice.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public DeviceResponse createOrUpdateDevice(DeviceRequest deviceRequest) {
        log.info("Start idempotence saving device with deviceId = {}", deviceRequest.id());

        Device deviceToSave = deviceMapper.toDevice(deviceRequest);
        Device savedDevice = deviceRepository.save(deviceToSave);
        log.debug("Device saved with body = {}", savedDevice);

        return deviceMapper.toDeviceResponse(savedDevice);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public DeviceResponse updateDevice(DeviceRequest deviceRequest, String id) {
        Device deviceToUpdate = loadDeviceById(id);
        log.info("Start updating device with deviceId = {}", id);

        Device updatedDeviceToSave = deviceMapper.updateDevice(deviceToUpdate, deviceRequest);
        Device savedDevice = deviceRepository.save(updatedDeviceToSave);
        log.debug("Device updated with body = {}", savedDevice);

        return deviceMapper.toDeviceResponse(savedDevice);
    }

    public DeviceResponse getDeviceById(String deviceId) {
        log.info("Start getting device by deviceId = {}", deviceId);
        Device device = loadDeviceById(deviceId);
        return deviceMapper.toDeviceResponse(device);
    }

    public List<DeviceResponse> getDevices() {
        log.info("Start getting devices");
        return deviceRepository.findAll()
                .stream()
                .map(deviceMapper::toDeviceResponse)
                .toList();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void deleteDeviceById(String deviceId) {
        if (existsDeviceId(deviceId)) {
            log.info("Start deleting device by deviceId = {}", deviceId);
            deviceRepository.deleteDeviceById(deviceId);
        } else {
            throw new DeviceNotFoundException(deviceId);
        }
    }

    private Device loadDeviceById(String deviceId) {
        return deviceRepository
                .getDeviceById(deviceId)
                .orElseThrow(
                        () -> new DeviceNotFoundException(deviceId)
                );
    }

    private boolean existsDeviceId(String deviceId) {
        log.info("Checking exists device by id = {}", deviceId);
        return deviceRepository.existsDeviceById(deviceId);
    }
}
