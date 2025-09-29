package com.example.failedeventsprocessor.consumer;

import com.example.events.FailedEventRecord;
import com.example.failedeventsprocessor.dto.response.FileResponse;
import com.example.failedeventsprocessor.service.FailedEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class FailedEventConsumer {

    private final FailedEventService failedEventService;

    @KafkaListener(
            topics = "${consumer.failed-event.topic}",
            concurrency = "${consumer.failed-event.concurrency}",
            groupId = "${consumer.failed-event.groupId}",
            containerFactory = "failedEventListenerContainerFactory"
    )
    public void handleFailedEvent(FailedEventRecord failedEventRecord) {
        log.info("Received event : {} and timestamp: {}", failedEventRecord.getFailedEvent(), LocalDateTime.now());

        FileResponse fileResponse = failedEventService.processFailedEvent(failedEventRecord);

        log.info("Failed event was processed with S3 key: {} and timestamp: {}", fileResponse.key,  LocalDateTime.now());
    }
}
