package com.example.devicecollector.unit;

import com.example.avro.DeviceEvent;
import com.example.devicecollector.dto.DeviceResponse;
import com.example.devicecollector.entity.Device;
import com.example.devicecollector.mapper.DeviceMapperImpl;
import com.example.devicecollector.repository.DeviceRepository;
import com.example.devicecollector.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.example.devicecollector.utils.DeviceData.DEVICES;
import static com.example.devicecollector.utils.DeviceData.DEVICE_LAPTOP_EVENT;
import static com.example.devicecollector.utils.DeviceData.DEVICE_PHONE_EVENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Spy
    private DeviceMapperImpl deviceMapper;

    @InjectMocks
    private DeviceService deviceService;

    @Test
    public void device_serviceSingleCreation_successful() {
        InOrder inOrder = Mockito.inOrder(deviceMapper, deviceRepository);
        ArgumentCaptor<DeviceEvent> deviceEventArgumentCaptor = ArgumentCaptor.forClass(DeviceEvent.class);
        ArgumentCaptor<Device> deviceArgumentCaptor = ArgumentCaptor.forClass(Device.class);

        when(deviceRepository.saveOrUpdateDeviceById(any(Device.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        DeviceResponse deviceResponse = deviceService.createOrUpdateDeviceById(DEVICE_PHONE_EVENT);

        inOrder.verify(deviceMapper).toDevice(any(DeviceEvent.class));
        inOrder.verify(deviceRepository).saveOrUpdateDeviceById(any(Device.class));

        verify(deviceMapper, Mockito.times(1)).toDevice(deviceEventArgumentCaptor.capture());
        assertEquals(DEVICE_PHONE_EVENT, deviceEventArgumentCaptor.getValue());

        verify(deviceRepository, Mockito.times(1)).saveOrUpdateDeviceById(deviceArgumentCaptor.capture());
        assertTrue(
                assertionsDeviceByEvent(deviceArgumentCaptor.getValue(), DEVICE_PHONE_EVENT)
        );

        assertTrue(
                assertionsDeviceResponseByEvent(deviceResponse, DEVICE_PHONE_EVENT)
        );
    }

    @Test
    public void device_serviceBatchCreation_successful() {
        int[][] expectedResultBatch = new int[][] {{1, 2}};
        InOrder inOrder = Mockito.inOrder(deviceMapper, deviceRepository);
        ArgumentCaptor<List<DeviceEvent>> deviceEventArgumentCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Device>> deviceArgumentCaptor = ArgumentCaptor.forClass(List.class);

        when(deviceRepository.saveOrUpdateDevicesById(any(List.class))).thenAnswer(_ -> expectedResultBatch);

        int[][] deviceResultBatch = deviceService.createOrUpdateDevicesById(DEVICES);

        inOrder.verify(deviceMapper).toDevices(any(List.class));
        inOrder.verify(deviceRepository).saveOrUpdateDevicesById(any(List.class));

        verify(deviceMapper, Mockito.times(1)).toDevices(deviceEventArgumentCaptor.capture());
        assertEquals(DEVICES, deviceEventArgumentCaptor.getValue());

        verify(deviceRepository, Mockito.times(1)).saveOrUpdateDevicesById(deviceArgumentCaptor.capture());
        assertTrue(
                assertionsDevicesByPhoneAndLaptopEvents(deviceArgumentCaptor.getValue())
        );

        assertEquals(expectedResultBatch, deviceResultBatch);
    }

    private boolean assertionsDevicesByPhoneAndLaptopEvents(List<Device> devices) {
        Device devicePhoneEvents = devices.getFirst();
        boolean deviceEventsIsPhoneDevice = assertionsDeviceByEvent(devicePhoneEvents, DEVICE_PHONE_EVENT);
        Device deviceLaptopEvents = devices.getLast();
        boolean deviceEventsIsLaptopDevice = assertionsDeviceByEvent(deviceLaptopEvents, DEVICE_LAPTOP_EVENT);

        return deviceEventsIsPhoneDevice && deviceEventsIsLaptopDevice;
    }

    private boolean assertionsDeviceByEvent(Device device, DeviceEvent deviceEvent) {
        return device.getDeviceId().equals(deviceEvent.getDeviceId())
                && device.getDeviceType().equals(deviceEvent.getDeviceType())
                && device.getCreatedAt().equals(deviceEvent.getCreatedAt())
                && device.getMeta().equals(deviceEvent.getMeta());
    }

    private boolean assertionsDeviceResponseByEvent(DeviceResponse deviceResponse, DeviceEvent deviceEvent) {
        return deviceResponse.deviceId().equals(deviceEvent.getDeviceId())
                && deviceResponse.deviceType().equals(deviceEvent.getDeviceType())
                && deviceResponse.createdAt().equals(deviceEvent.getCreatedAt())
                && deviceResponse.meta().equals(deviceEvent.getMeta());
    }
}
