package com.example.apiorchestrator.domain.dto.response;

public record DeviceVersionResponse(
        String deviceId,
        String prevVersion,
        String targetVersion,
        String idempotenceKey
) {}
