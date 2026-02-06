package com.example.deviceservice.dto.response;

public record DeviceVersionResponse(
        String oldTargetVersion,
        String newTargetVersion,
        String idempotenceKey
) {
}
