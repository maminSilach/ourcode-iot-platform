package com.example.apiorchestrator.domain.dto.request.filter;

public record EventFilter(

        Long fromTimestamp,

        Long toTimestamp,

        String type
) {}
