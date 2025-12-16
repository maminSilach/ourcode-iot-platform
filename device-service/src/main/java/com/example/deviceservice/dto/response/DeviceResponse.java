package com.example.deviceservice.dto.response;

public record DeviceResponse(
        String id,
        String deviceType,
        Long createdAt,
        String meta,
        String idempotenceKey,
        String oldTargetVersion,
        String newTargetVersion
) {}
