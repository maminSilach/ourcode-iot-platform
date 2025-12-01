package com.example.failedeventsprocessor.service;

import com.example.events.FailedEventRecord;
import com.example.failedeventsprocessor.dto.response.FileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FailedEventService {

    private static final String RESOURCE_NAME = "%s_%s_%d.json";

    @Value("${event.failed-bucket-name}")
    private final String failedEventBucketName;

    private final FileService fileService;

    public FileResponse processFailedEvent(FailedEventRecord failedEvent) {
        String errorReason = failedEvent.getErrorMeta().getReason();
        String resourceName = generateFailedEventResourceName(errorReason);
        log.info("Processing failed event - error: {}, resource: {}", errorReason, resourceName);

        FileResponse fileResponse = fileService.uploadJsonResourceWithRetry(failedEvent.toString(), resourceName, failedEventBucketName);
        log.info("Upload complete - bucket: {}, fileKey: {}", failedEventBucketName, fileResponse.key);

        return fileResponse;
    }

    private String generateFailedEventResourceName(String eventType) {
        String uuid = UUID.randomUUID().toString();
        long timestamp = Instant.now().toEpochMilli();

        return String.format(RESOURCE_NAME, eventType, uuid, timestamp);
    }
}
