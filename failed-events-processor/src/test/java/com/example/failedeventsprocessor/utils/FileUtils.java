package com.example.failedeventsprocessor.utils;

import com.example.failedeventsprocessor.dto.response.FileResponse;

import java.time.LocalDateTime;

public final class FileUtils {

    private FileUtils() {
        throw new UnsupportedOperationException();
    }

    public static final String AWS_HOST = "http://127.0.0.1:9000";
    public static final String AWS_REGION = "us-west-1";

    public static final String EVENT_NULL_POINTER_FILE_KEY = "NullPointerException_0bd312ba-b21b-45ac-bf82-12c50d77e445_1758482838308.json";
    public static final String EVENT_NULL_POINTER_FILE_URL = AWS_HOST + "/failed-events/" + EVENT_NULL_POINTER_FILE_KEY;

    public static final FileResponse EVENT_NULL_POINTER_FILE_RESPONSE = new FileResponse(
            EVENT_NULL_POINTER_FILE_URL,
            EVENT_NULL_POINTER_FILE_KEY,
            LocalDateTime.now()
    );

}
