package com.example.devicecollector.service;

import com.example.avro.DeviceEvent;
import com.example.devicecollector.dto.DeviceResponse;
import com.example.devicecollector.entity.Device;
import com.example.devicecollector.mapper.DeviceMapper;
import com.example.devicecollector.repository.DeviceRepository;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;
    private final Counter failedMessageCounter;

    public DeviceResponse createOrUpdateDeviceById(DeviceEvent deviceEvent) {
        log.info("Processing device event with deviceId = {}", deviceEvent.getDeviceId());

        Device deviceToSave = deviceMapper.toDevice(deviceEvent);
        Device savedDevice = deviceRepository.saveOrUpdateDeviceById(deviceToSave);
        log.info("Device saved with body = {}", savedDevice);

        return deviceMapper.toDeviceResponse(savedDevice);
    }

    public int[][] createOrUpdateDevicesById(List<DeviceEvent> deviceEvents) {
        List<String> deviceIds = getDeviceIds(deviceEvents);
        log.info("Processing device events with device ids = {}", deviceIds);

        List<Device> devicesToSave = deviceMapper.toDevices(deviceEvents);
        int[][] updateResults = deviceRepository.saveOrUpdateDevicesById(devicesToSave);
        log.info("Batch update completed. Sent {} batches", updateResults.length);

        return updateResults;
    }

    public void processInvalidDeviceEvents(List<DeviceEvent> deviceEvents) {
        List<String> deviceIds = getDeviceIds(deviceEvents);
        log.error("Device events failed processing and moved to DLQ. Affected device IDs: {}}", deviceIds);
        failedMessageCounter.increment(deviceIds.size());
    }

    private List<String> getDeviceIds(List<DeviceEvent> deviceEvents) {
        return deviceEvents.stream()
                .map(DeviceEvent::getDeviceId)
                .toList();
    }
}
