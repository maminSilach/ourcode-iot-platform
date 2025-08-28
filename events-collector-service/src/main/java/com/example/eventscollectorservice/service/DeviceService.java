package com.example.eventscollectorservice.service;

import com.example.avro.DeviceEvent;
import com.example.eventscollectorservice.dto.DeviceResponse;
import com.example.eventscollectorservice.entity.Device;
import com.example.eventscollectorservice.mapper.DeviceMapper;
import com.example.eventscollectorservice.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    @Value("${producer.deviceId.topic}")
    private final String deviceIdTopic;

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;
    private final DeviceCacheService deviceCacheService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public DeviceResponse createDevice(DeviceEvent deviceEvent) {
        log.info("Creating device with body = {}", deviceEvent);

        Device deviceToSave = deviceMapper.toDevice(deviceEvent);
        Device savedDevice = deviceRepository.save(deviceToSave);

        log.info("Device saved with body = {}", savedDevice);

        sendDeviceIdIfNotExists(savedDevice.getDeviceId());

        return deviceMapper.toDeviceResponse(savedDevice);
    }

    private void sendDeviceIdIfNotExists(String deviceId) {
        if (deviceCacheService.registerDeviceIdIfNotExist(deviceId)) {

            log.info("Sending deviceId = {} to topic = {}", deviceId, deviceIdTopic);
            kafkaTemplate.send(deviceIdTopic, deviceId);

        } else {
            log.warn("Device id {} already exists. He doesn't save to topic = {}", deviceId, deviceIdTopic);
        }
    }
}
