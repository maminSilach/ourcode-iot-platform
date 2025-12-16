package com.example.apiorchestrator.domain.dto.request;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DeviceVersionRequest {
    private final String deviceId;
    private final String targetVersion;
    private final String idempotenceKey;

    private String eventId;
    private String oldTargetVersion;
}