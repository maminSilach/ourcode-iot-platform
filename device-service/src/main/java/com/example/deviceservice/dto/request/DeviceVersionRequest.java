package com.example.deviceservice.dto.request;

public record DeviceVersionRequest(
        String targetVersion,
        String idempotenceKey
) {}
