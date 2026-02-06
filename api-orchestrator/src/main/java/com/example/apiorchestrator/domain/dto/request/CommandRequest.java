package com.example.apiorchestrator.domain.dto.request;

public record CommandRequest(

        String routerId,

        CommandType commandType,

        String payload
) {}
