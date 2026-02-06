package com.example.deviceservice.utils;

import com.example.deviceservice.dto.request.DeviceRequest;
import com.example.deviceservice.model.Device;

public final class DeviceData {

    private DeviceData() {
        throw new UnsupportedOperationException();
    }

    public static final String PHONE_DEVICE_ID = "phone";
    public static final String LAPTOP_DEVICE_ID = "device laptop id";

    public static final Device DEVICE_PHONE = new Device(
            PHONE_DEVICE_ID,
            "phone-type",
            10000L,
            "phone-meta",
            null,
            null
    );

    public static final Device DEVICE_LAPTOP = new Device(
            LAPTOP_DEVICE_ID,
            "laptop-type",
            10000L,
            "laptop-meta",
            null,
            null
    );

    public static final DeviceRequest DEVICE_PHONE_REQUEST = new DeviceRequest(
            PHONE_DEVICE_ID,
            "phone-type",
            10000L,
            "phone-meta",
            null,
            null
    );

    public static final DeviceRequest DEVICE_LAPTOP_REQUEST = new DeviceRequest(
            LAPTOP_DEVICE_ID,
            "laptop-type",
            10000L,
            "laptop-meta",
            null,
            null
    );
}
