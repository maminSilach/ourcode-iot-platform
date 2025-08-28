package com.example.eventscollectorservice.dto;

public record DeviceResponse(
        String deviceId,
        String eventId,
        Long timestamp,
        String type,
        String payload
) {}
