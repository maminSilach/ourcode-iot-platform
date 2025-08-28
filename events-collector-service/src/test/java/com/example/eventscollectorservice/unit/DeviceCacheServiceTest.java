package com.example.eventscollectorservice.unit;

import com.example.eventscollectorservice.service.DeviceCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static com.example.eventscollectorservice.utils.DeviceData.DEVICE_IDS;
import static com.example.eventscollectorservice.utils.DeviceData.LAPTOP_DEVICE_ID;
import static com.example.eventscollectorservice.utils.DeviceData.PHONE_DEVICE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class DeviceCacheServiceTest {

    @Spy
    private DeviceCacheService deviceCacheService;

    @Test
    public void device_cacheRegistration_duplicateDeviceIdsHandling() {
        Set<String> cachedDeviceIds = deviceCacheService.getCachedDeviceIds();
        assertEquals(0, cachedDeviceIds.size());

        boolean isFirstPhoneDeviceRegistered = deviceCacheService.registerDeviceIdIfNotExist(PHONE_DEVICE_ID);
        cachedDeviceIds = deviceCacheService.getCachedDeviceIds();

        assertEquals(1, cachedDeviceIds.size());
        assertTrue(cachedDeviceIds.contains(PHONE_DEVICE_ID));
        assertTrue(isFirstPhoneDeviceRegistered);

        boolean isSecondPhoneDeviceRegistered = deviceCacheService.registerDeviceIdIfNotExist(PHONE_DEVICE_ID);
        cachedDeviceIds = deviceCacheService.getCachedDeviceIds();

        assertEquals(1, cachedDeviceIds.size());
        assertTrue(cachedDeviceIds.contains(PHONE_DEVICE_ID));
        assertFalse(isSecondPhoneDeviceRegistered);

        boolean isLaptopDeviceRegistered = deviceCacheService.registerDeviceIdIfNotExist(LAPTOP_DEVICE_ID);
        cachedDeviceIds = deviceCacheService.getCachedDeviceIds();

        assertEquals(2, cachedDeviceIds.size());
        assertTrue(cachedDeviceIds.containsAll(DEVICE_IDS));
        assertTrue(isLaptopDeviceRegistered);

        deviceCacheService.invalidateDeviceIds();
    }
}
