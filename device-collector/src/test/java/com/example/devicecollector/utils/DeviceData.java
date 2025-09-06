package com.example.devicecollector.utils;

import com.example.avro.DeviceEvent;

import java.util.List;

public final class DeviceData {

    private DeviceData() {
        throw new UnsupportedOperationException();
    }

    public static final String PHONE_DEVICE_ID = "phone";
    public static final String LAPTOP_DEVICE_ID = "device laptop id";

    public static final DeviceEvent DEVICE_PHONE_EVENT = new DeviceEvent(
            PHONE_DEVICE_ID,
            "phone-type",
            10000L,
            "phone-meta"
    );

    public static final DeviceEvent DEVICE_LAPTOP_EVENT = new DeviceEvent(
            LAPTOP_DEVICE_ID,
            "laptop-type",
            10000L,
            "laptop-meta"
    );

    public static final List<DeviceEvent> DEVICES = List.of(DEVICE_PHONE_EVENT, DEVICE_LAPTOP_EVENT);

}
