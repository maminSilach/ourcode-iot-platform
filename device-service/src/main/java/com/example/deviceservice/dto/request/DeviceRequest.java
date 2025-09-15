package com.example.deviceservice.dto.request;

public record DeviceRequest(
        String id,
        String deviceType,
        Long createdAt,
        String meta
) {}
