package com.example.devicecollector.mapper;

import com.example.avro.DeviceEvent;
import com.example.devicecollector.dto.DeviceResponse;
import com.example.devicecollector.entity.Device;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface DeviceMapper {

    @Mapping(source = "deviceId", target = "deviceId")
    @Mapping(source = "deviceType", target = "deviceType")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "meta", target = "meta")
    Device toDevice(DeviceEvent device);

    List<Device> toDevices(List<DeviceEvent> devices);

    DeviceResponse toDeviceResponse(Device device);
}
