package com.example.eventservice.dto.request;

public record EventFilter(

        Long fromTimestamp,

        Long toTimestamp,

        String type
) {}
