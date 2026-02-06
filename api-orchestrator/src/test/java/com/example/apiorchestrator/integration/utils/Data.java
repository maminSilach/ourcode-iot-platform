package com.example.apiorchestrator.integration.utils;

import com.example.apiorchestrator.utils.FileExtension;
import com.example.apiorchestrator.utils.ResourceLoader;

public final class Data {

    public static final String EVENT_RESPONSE = ResourceLoader.loadResourceAsString("event/event-response", FileExtension.JSON);

    public static final String DEVICE_RESPONSE = ResourceLoader.loadResourceAsString("device/device-response", FileExtension.JSON);
}
