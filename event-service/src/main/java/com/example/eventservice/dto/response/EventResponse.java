package com.example.eventservice.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record EventResponse(

        String deviceId,

        String eventId,

        Long timestamp,

        String type,

        String payload
) {}
