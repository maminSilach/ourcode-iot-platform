package com.example.deviceservice.dto;

public record MigrationSource(
        String url,
        String username,
        String password
) {}
