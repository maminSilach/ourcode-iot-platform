package com.example.apiorchestrator.domain.dto.request;

public record DeviceVersionRequest(
        String targetVersion,
        String idempotenceKey
) {
}
