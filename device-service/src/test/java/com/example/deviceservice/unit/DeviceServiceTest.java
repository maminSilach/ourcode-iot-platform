package com.example.deviceservice.unit;

import com.example.deviceservice.dto.request.DeviceRequest;
import com.example.deviceservice.dto.response.DeviceResponse;
import com.example.deviceservice.exception.DeviceNotFoundException;
import com.example.deviceservice.mapper.DeviceMapperImpl;
import com.example.deviceservice.model.Device;
import com.example.deviceservice.repository.DeviceRepository;
import com.example.deviceservice.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.example.deviceservice.utils.DeviceData.DEVICE_LAPTOP;
import static com.example.deviceservice.utils.DeviceData.DEVICE_LAPTOP_REQUEST;
import static com.example.deviceservice.utils.DeviceData.DEVICE_PHONE;
import static com.example.deviceservice.utils.DeviceData.DEVICE_PHONE_REQUEST;
import static com.example.deviceservice.utils.DeviceData.LAPTOP_DEVICE_ID;
import static com.example.deviceservice.utils.DeviceData.PHONE_DEVICE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    public void device_serviceCreation_successful() {
        InOrder inOrder = Mockito.inOrder(deviceMapper, deviceRepository);
        ArgumentCaptor<DeviceRequest> deviceRequestArgumentCaptor = ArgumentCaptor.forClass(DeviceRequest.class);
        ArgumentCaptor<Device> deviceArgumentCaptor = ArgumentCaptor.forClass(Device.class);

        when(deviceRepository.save(any(Device.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        DeviceResponse deviceResponse = deviceService.createOrUpdateDevice(DEVICE_PHONE_REQUEST);

        inOrder.verify(deviceMapper).toDevice(any(DeviceRequest.class));
        inOrder.verify(deviceRepository).save(any(Device.class));

        verify(deviceMapper, times(1)).toDevice(deviceRequestArgumentCaptor.capture());
        assertEquals(DEVICE_PHONE_REQUEST, deviceRequestArgumentCaptor.getValue());

        verify(deviceRepository, times(1)).save(deviceArgumentCaptor.capture());
        assertTrue(
                assertionsDeviceRequestByDevice(DEVICE_PHONE_REQUEST, deviceArgumentCaptor.getValue())
        );

        assertTrue(
                assertionsDeviceResponseByRequest(DEVICE_PHONE_REQUEST, deviceResponse)
        );
    }

    @Test
    public void device_serviceUpdateById_successful() {
        InOrder inOrder = Mockito.inOrder(deviceRepository, deviceMapper);
        ArgumentCaptor<DeviceRequest> deviceRequestArgumentCaptor = ArgumentCaptor.forClass(DeviceRequest.class);
        ArgumentCaptor<Device> deviceArgumentCaptor = ArgumentCaptor.forClass(Device.class);

        when(deviceRepository.save(any(Device.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        Device copyDeviceLaptop = new Device(DEVICE_LAPTOP.getId(), DEVICE_LAPTOP.getDeviceType(), DEVICE_LAPTOP.getCreatedAt(), DEVICE_LAPTOP.getMeta(), null, null);
        when(deviceRepository.getDeviceById(anyString())).thenAnswer((_) -> Optional.of(copyDeviceLaptop));

        DeviceResponse deviceResponse = deviceService.updateDevice(DEVICE_PHONE_REQUEST, LAPTOP_DEVICE_ID);

        inOrder.verify(deviceRepository).getDeviceById(anyString());
        inOrder.verify(deviceMapper).updateDevice(any(Device.class), any(DeviceRequest.class));
        inOrder.verify(deviceRepository).save(any(Device.class));
        inOrder.verify(deviceMapper).toDeviceResponse(any(Device.class), any());

        verify(deviceRepository, times(1)).getDeviceById(anyString());
        verify(deviceRepository).getDeviceById(
                argThat(deviceId -> deviceId.equals(LAPTOP_DEVICE_ID))
        );

        verify(deviceMapper, times(1)).updateDevice(any(Device.class), deviceRequestArgumentCaptor.capture());
        assertEquals(DEVICE_PHONE_REQUEST, deviceRequestArgumentCaptor.getValue());

        verify(deviceRepository, times(1)).save(deviceArgumentCaptor.capture());
        assertTrue(
                assertionsDeviceRequestByDevice(DEVICE_PHONE_REQUEST, deviceArgumentCaptor.getValue())
        );

        assertTrue(
                assertionsDeviceResponseByRequest(DEVICE_PHONE_REQUEST, deviceResponse)
        );
    }

    @Test
    public void device_serviceGetById_successful() {
        InOrder inOrder = Mockito.inOrder(deviceRepository, deviceMapper);

        when(deviceRepository.getDeviceById(anyString())).thenAnswer((_) -> Optional.of(DEVICE_PHONE));

        DeviceResponse deviceResponse = deviceService.getDeviceById(PHONE_DEVICE_ID);

        inOrder.verify(deviceRepository).getDeviceById(anyString());
        inOrder.verify(deviceMapper).toDeviceResponse(any(Device.class));

        verify(deviceRepository, times(1)).getDeviceById(anyString());
        verify(deviceRepository).getDeviceById(
                argThat(deviceId -> deviceId.equals(PHONE_DEVICE_ID))
        );

        assertTrue(
                assertionsDeviceResponseByRequest(DEVICE_PHONE_REQUEST, deviceResponse)
        );
    }

    @Test
    public void device_serviceGetById_notFound() {
        InOrder inOrder = Mockito.inOrder(deviceRepository);

        when(deviceRepository.getDeviceById(anyString())).thenAnswer((_) -> Optional.empty());

        assertThrows(DeviceNotFoundException.class, () -> deviceService.getDeviceById(PHONE_DEVICE_ID));

        inOrder.verify(deviceRepository).getDeviceById(anyString());

        verify(deviceRepository, times(1)).getDeviceById(anyString());
        verify(deviceMapper, Mockito.never()).toDeviceResponse(any(Device.class));
    }


    @Test
    public void device_serviceUpdateById_notFound() {
        InOrder inOrder = Mockito.inOrder(deviceRepository, deviceMapper);

        when(deviceRepository.getDeviceById(anyString())).thenAnswer((_) -> Optional.empty());

        assertThrows(DeviceNotFoundException.class, () -> deviceService.updateDevice(DEVICE_LAPTOP_REQUEST, PHONE_DEVICE_ID));

        inOrder.verify(deviceRepository).getDeviceById(anyString());

        verify(deviceRepository, times(1)).getDeviceById(anyString());
        verify(deviceRepository).getDeviceById(
                argThat(deviceId -> deviceId.equals(PHONE_DEVICE_ID))
        );

        verify(deviceMapper, Mockito.never()).updateDevice(any(Device.class), any(DeviceRequest.class));
        verify(deviceRepository, Mockito.never()).save(any(Device.class));
        verify(deviceMapper, Mockito.never()).toDeviceResponse(any(Device.class));
    }

    @Test
    public void device_serviceDeleteById_successful() {
        InOrder inOrder = Mockito.inOrder(deviceRepository);

        when(deviceRepository.existsDeviceById(anyString())).thenAnswer((_) -> true);

        deviceService.deleteDeviceById(PHONE_DEVICE_ID);

        inOrder.verify(deviceRepository).existsDeviceById(anyString());
        inOrder.verify(deviceRepository).deleteDeviceById(anyString());

        verify(deviceRepository, times(1)).existsDeviceById(anyString());
        verify(deviceRepository).existsDeviceById(
                argThat(deviceId -> deviceId.equals(PHONE_DEVICE_ID))
        );

        verify(deviceRepository, times(1)).deleteDeviceById(anyString());
        verify(deviceRepository).deleteDeviceById(
                argThat(deviceId -> deviceId.equals(PHONE_DEVICE_ID))
        );
    }

    @Test
    public void device_serviceDeleteById_NotFound() {
        when(deviceRepository.existsDeviceById(anyString())).thenAnswer((_) -> false);

        assertThrows(DeviceNotFoundException.class, () -> deviceService.deleteDeviceById(PHONE_DEVICE_ID));

        verify(deviceRepository, times(1)).existsDeviceById(anyString());
        verify(deviceRepository).existsDeviceById(
                argThat(deviceId -> deviceId.equals(PHONE_DEVICE_ID))
        );

        verify(deviceRepository, never()).deleteDeviceById(anyString());
    }

    private boolean assertionsDeviceRequestByDevice(DeviceRequest deviceRequest, Device device) {
        return deviceRequest.id().equals(device.getId())
                && deviceRequest.deviceType().equals(device.getDeviceType())
                && deviceRequest.createdAt().equals(device.getCreatedAt())
                && deviceRequest.meta().equals(device.getMeta());
    }

    private boolean assertionsDeviceResponseByRequest(DeviceRequest deviceRequest, DeviceResponse deviceResponse) {
        return deviceRequest.id().equals(deviceResponse.id())
                && deviceRequest.deviceType().equals(deviceResponse.deviceType())
                && deviceRequest.createdAt().equals(deviceResponse.createdAt())
                && deviceRequest.meta().equals(deviceResponse.meta());
    }
}
