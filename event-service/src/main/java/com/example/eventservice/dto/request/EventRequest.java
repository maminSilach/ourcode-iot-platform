package com.example.eventservice.dto.request;

import com.example.eventservice.enums.EventStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import static com.example.eventservice.enums.EventStatus.UPDATING;

@Data
@RequiredArgsConstructor
public class EventRequest {
    private final String targetVersion;
    private final String eventId = UUID.randomUUID().toString();
    private final String deviceId;
    private final String idempotenceKey;
    private final EventStatus status = UPDATING;
}