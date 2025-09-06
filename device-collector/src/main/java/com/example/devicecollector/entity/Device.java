package com.example.devicecollector.entity;

import lombok.Data;

@Data
public class Device {
    private String deviceId;
    private String deviceType;
    private Long createdAt;
    private String meta;
}
