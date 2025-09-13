package com.example.devicecollector.dto;

public record MigrationSource(
        String url,
        String username,
        String password
) {}
