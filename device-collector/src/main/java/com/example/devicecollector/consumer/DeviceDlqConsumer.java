package com.example.devicecollector.consumer;

import com.example.avro.DeviceEvent;
import com.example.devicecollector.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceDlqConsumer {

    private final DeviceService deviceService;

    @KafkaListener(
            topics = "${kafka.device.topic-dlq}",
            concurrency = "${consumer.device-dlq.concurrency}",
            groupId = "${consumer.device-dlq.groupId}",
            containerFactory = "deviceEventBatchListenerContainerFactory"
    )
    public void handleDlqDeviceEvent(List<DeviceEvent> deviceEvent) {
        deviceService.processInvalidDeviceEvents(deviceEvent);
    }
}
