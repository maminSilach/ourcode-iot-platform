package com.example.failedeventsprocessor.utils;

import com.example.events.ErrorMeta;
import com.example.events.FailedEvent;
import com.example.events.FailedEventRecord;

public final class FailedEventData {

    private FailedEventData() {
        throw new UnsupportedOperationException();
    }

    public static final String PHONE_DEVICE_ID = "phone";
    public static final String FAILED_EVENT_REASON = "NullPointerException";

    public static final FailedEvent FAILED_EVENT = new FailedEvent(
            "event-add",
            PHONE_DEVICE_ID,
            "phone-type",
            10000L,
            "phone-meta"
    );

    public static final ErrorMeta ERROR_META = new ErrorMeta(
          "device-collector-service",
            FAILED_EVENT_REASON,
            "org.postgresql.util.PSQLException: Null"
    );

    public static final FailedEventRecord FAILED_EVENT_RECORD = new FailedEventRecord(
            FAILED_EVENT,
            ERROR_META
    );
}
