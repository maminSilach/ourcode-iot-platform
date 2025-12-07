package com.example.eventservice.dto.response;

public record EventResponse(

        String deviceId,

        String eventId,

        Long timestamp,

        String type,

        String payload
) {}
