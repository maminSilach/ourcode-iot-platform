package com.example.eventscollectorservice.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeviceCacheService {

    private static final Set<String> DEVICE_ID_CONTEXT = ConcurrentHashMap.newKeySet();

    public boolean registerDeviceIdIfNotExist(String deviceId) {
        return DEVICE_ID_CONTEXT.add(deviceId);
    }

    public Set<String> getCachedDeviceIds() {
        return Set.copyOf(DEVICE_ID_CONTEXT);
    }

    public void invalidateDeviceIds() {
        DEVICE_ID_CONTEXT.clear();
    }
}
