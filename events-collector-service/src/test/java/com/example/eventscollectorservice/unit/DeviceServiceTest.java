package com.example.eventscollectorservice.unit;

import com.example.avro.DeviceEvent;
import com.example.eventscollectorservice.dto.DeviceResponse;
import com.example.eventscollectorservice.entity.Device;
import com.example.eventscollectorservice.mapper.DeviceMapper;
import com.example.eventscollectorservice.mapper.DeviceMapperImpl;
import com.example.eventscollectorservice.repository.DeviceRepository;
import com.example.eventscollectorservice.service.DeviceCacheService;
import com.example.eventscollectorservice.service.DeviceService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static com.example.eventscollectorservice.utils.DeviceData.DEVICE_LAPTOP_EVENT;
import static com.example.eventscollectorservice.utils.DeviceData.DEVICE_PHONE_EVENT;
import static com.example.eventscollectorservice.utils.DeviceData.LAPTOP_DEVICE_ID;
import static com.example.eventscollectorservice.utils.DeviceData.PHONE_DEVICE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeviceServiceTest {

    private static final String DEVICE_ID_TOPIC = "device_id_topic";

    private final DeviceRepository deviceRepository = Mockito.mock(DeviceRepository.class);

    private final DeviceMapper deviceMapper = Mockito.spy(DeviceMapperImpl.class);

    private final DeviceCacheService deviceCacheService = Mockito.mock(DeviceCacheService.class);

    private final KafkaTemplate<String, String> kafkaTemplate = Mockito.mock(KafkaTemplate.class);

    private final DeviceService deviceService = new DeviceService(
            DEVICE_ID_TOPIC, deviceRepository, deviceMapper, deviceCacheService, kafkaTemplate
    );

    @Test
    public void device_serviceCreation_successfulWithKafkaPublishing() {
        InOrder inOrder = Mockito.inOrder(deviceRepository, deviceMapper, deviceCacheService, kafkaTemplate);
        ArgumentCaptor<DeviceEvent> deviceEventArgumentCaptor = ArgumentCaptor.forClass(DeviceEvent.class);
        ArgumentCaptor<Device> deviceArgumentCaptor = ArgumentCaptor.forClass(Device.class);

        when(deviceRepository.save(any(Device.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(deviceCacheService.registerDeviceIdIfNotExist(anyString())).thenReturn(true);

        DeviceResponse deviceResponse = deviceService.createDevice(DEVICE_PHONE_EVENT);

        inOrder.verify(deviceMapper).toDevice(any(DeviceEvent.class));
        inOrder.verify(deviceRepository).save(any(Device.class));
        inOrder.verify(deviceCacheService).registerDeviceIdIfNotExist(any(String.class));
        inOrder.verify(kafkaTemplate).send(any(String.class), any(String.class));

        verify(deviceMapper, Mockito.times(1)).toDevice(deviceEventArgumentCaptor.capture());
        assertEquals(DEVICE_PHONE_EVENT, deviceEventArgumentCaptor.getValue());

        verify(deviceRepository, Mockito.times(1)).save(deviceArgumentCaptor.capture());
        Assertions.assertTrue(
                assertionsDeviceByEvent(deviceArgumentCaptor.getValue(), DEVICE_PHONE_EVENT)
        );

        verify(deviceCacheService, Mockito.times(1)).registerDeviceIdIfNotExist(anyString());
        verify(deviceCacheService).registerDeviceIdIfNotExist(
                argThat(deviceId -> deviceId.equals(PHONE_DEVICE_ID))
        );

        verify(kafkaTemplate, Mockito.times(1)).send(anyString(), anyString());
        verify(kafkaTemplate).send(
                argThat(deviceIdTopic -> deviceIdTopic.equals(DEVICE_ID_TOPIC)),
                argThat(deviceId -> deviceId.equals(PHONE_DEVICE_ID))
        );

        assertTrue(
                assertionsDeviceResponseByEvent(deviceResponse, DEVICE_PHONE_EVENT)
        );
    }

    @Test
    public void device_serviceCreation_skipKafkaPublishingForDuplicate() {
        InOrder inOrder = Mockito.inOrder(deviceRepository, deviceMapper, deviceCacheService);
        ArgumentCaptor<DeviceEvent> deviceEventArgumentCaptor = ArgumentCaptor.forClass(DeviceEvent.class);
        ArgumentCaptor<Device> deviceArgumentCaptor = ArgumentCaptor.forClass(Device.class);

        when(deviceRepository.save(any(Device.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(deviceCacheService.registerDeviceIdIfNotExist(anyString())).thenReturn(false);

        DeviceResponse deviceResponse = deviceService.createDevice(DEVICE_LAPTOP_EVENT);

        inOrder.verify(deviceMapper).toDevice(any(DeviceEvent.class));
        inOrder.verify(deviceRepository).save(any(Device.class));
        inOrder.verify(deviceCacheService).registerDeviceIdIfNotExist(any(String.class));

        verify(deviceMapper, Mockito.times(1)).toDevice(deviceEventArgumentCaptor.capture());
        assertEquals(DEVICE_LAPTOP_EVENT, deviceEventArgumentCaptor.getValue());

        verify(deviceRepository, Mockito.times(1)).save(deviceArgumentCaptor.capture());
        Assertions.assertTrue(
                assertionsDeviceByEvent(deviceArgumentCaptor.getValue(), DEVICE_LAPTOP_EVENT)
        );

        verify(deviceCacheService, Mockito.times(1)).registerDeviceIdIfNotExist(anyString());
        verify(deviceCacheService).registerDeviceIdIfNotExist(
                argThat(deviceId -> deviceId.equals(LAPTOP_DEVICE_ID))
        );

        verify(kafkaTemplate, never()).send(anyString(), anyString());
        assertTrue(
                assertionsDeviceResponseByEvent(deviceResponse, DEVICE_LAPTOP_EVENT)
        );

    }

    private boolean assertionsDeviceByEvent(Device device, DeviceEvent deviceEvent) {
        return device.getDeviceId().equals(deviceEvent.getDeviceId())
                && device.getEventId().equals(deviceEvent.getEventId())
                && device.getTimestamp().equals(deviceEvent.getTimestamp())
                && device.getType().equals(deviceEvent.getType())
                && device.getPayload().equals(deviceEvent.getPayload());
    }

    private boolean assertionsDeviceResponseByEvent(DeviceResponse deviceResponse, DeviceEvent deviceEvent) {
        return deviceResponse.deviceId().equals(deviceEvent.getDeviceId())
                && deviceResponse.eventId().equals(deviceEvent.getEventId())
                && deviceResponse.timestamp().equals(deviceEvent.getTimestamp())
                && deviceResponse.type().equals(deviceEvent.getType())
                && deviceResponse.payload().equals(deviceEvent.getPayload());
    }
}
