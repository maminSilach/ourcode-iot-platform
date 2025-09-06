package com.example.devicecollector.consumer;

import com.example.avro.DeviceEvent;
import com.example.devicecollector.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBooleanProperty(value = "consumer.device.batch", havingValue = false)
public class DeviceSingleConsumer {

    private final DeviceService deviceService;

    @KafkaListener(
            topics = "${consumer.device.topic}",
            concurrency = "${consumer.device.concurrency}",
            groupId = "${consumer.device.groupId}",
            containerFactory = "deviceEventListenerContainerFactory"
    )
    public void handleDeviceEvent(DeviceEvent deviceEvent) {
        deviceService.createOrUpdateDeviceById(deviceEvent);
    }
}
