package com.example.eventservice.integration.utils;

import com.example.eventservice.entity.Event;

public final class EventData {

    private EventData() {
        throw new UnsupportedOperationException();
    }

    public static final String EVENT_ID = "500efbda-9621-4ec3-a80e-2ac049d61e6c";
    public static final String DEVICE_ID = "f09862fa-9d7e-4bb5-a574-2f1d0e3e2186";

    public static final Event EVENT = new Event(
            DEVICE_ID,
            EVENT_ID,
            10000L,
            "CL",
            null
    );
}
