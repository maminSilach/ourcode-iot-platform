package com.example.eventscollectorservice.consumer;

import com.example.avro.DeviceEvent;
import com.example.eventscollectorservice.dto.DeviceResponse;
import com.example.eventscollectorservice.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final DeviceService deviceService;

    @KafkaListener(
            topics = "${consumer.event.topic}",
            concurrency = "${consumer.event.concurrency}",
            groupId = "${consumer.event.groupId}",
            containerFactory = "eventListenerContainerFactory"
    )
    public void handleEvent(DeviceEvent deviceEvent) {
        log.info("Received event with id: {} and timestamp: {}", deviceEvent.getDeviceId(), LocalDateTime.now());

        DeviceResponse deviceResponse = deviceService.createDevice(deviceEvent);

        log.info("Event was processed with id: {} and timestamp: {}", deviceResponse.eventId(), LocalDateTime.now());
    }
}
