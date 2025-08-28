package com.example.eventscollectorservice.utils;

import com.example.avro.DeviceEvent;

import java.util.Set;

public final class DeviceData {

    private DeviceData() {
        throw new UnsupportedOperationException();
    }

    public static final String PHONE_DEVICE_ID = "phone";
    public static final String LAPTOP_DEVICE_ID = "laptop";

    public static final String PHONE_EVENT_ID = "phone-event";
    public static final String LAPTOP_EVENT_ID = "laptop-event";

    public static final Set<String> DEVICE_IDS = Set.of(PHONE_DEVICE_ID, LAPTOP_DEVICE_ID);

    public static final DeviceEvent DEVICE_PHONE_EVENT = new DeviceEvent(
            PHONE_EVENT_ID,
            PHONE_DEVICE_ID,
            10000L,
            "phone-type",
            "phone-payload"
    );

    public static final DeviceEvent DEVICE_LAPTOP_EVENT = new DeviceEvent(
            LAPTOP_EVENT_ID,
            LAPTOP_DEVICE_ID,
            10000L,
            "laptop-type",
            "laptop-payload"
    );
}
