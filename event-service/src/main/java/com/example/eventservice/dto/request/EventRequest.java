package com.example.eventservice.dto.request;

import com.example.eventservice.enums.EventStatus;

public record EventRequest(
        String targetVersion,
        String deviceId,
        EventStatus status
) {}
