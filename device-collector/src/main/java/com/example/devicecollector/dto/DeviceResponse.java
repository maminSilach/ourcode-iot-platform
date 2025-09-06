package com.example.devicecollector.dto;

public record DeviceResponse(
        String deviceId,
        String deviceType,
        Long createdAt,
        String meta
) {}
