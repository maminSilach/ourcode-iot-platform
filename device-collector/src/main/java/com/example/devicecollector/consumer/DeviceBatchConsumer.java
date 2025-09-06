package com.example.devicecollector.consumer;

import com.example.avro.DeviceEvent;
import com.example.devicecollector.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBooleanProperty(value = "consumer.device.batch")
public class DeviceBatchConsumer {

    private final DeviceService deviceService;

    @KafkaListener(
            topics = "${consumer.device.topic}",
            concurrency = "${consumer.device.concurrency}",
            groupId = "${consumer.device.groupId}",
            containerFactory = "deviceEventBatchListenerContainerFactory"
    )
    public void handleDevicesEvent(List<DeviceEvent> devicesEvent) {
        deviceService.createOrUpdateDevicesById(devicesEvent);
    }
}
