package com.example.eventscollectorservice.mapper;

import com.example.avro.DeviceEvent;
import com.example.eventscollectorservice.dto.DeviceResponse;
import com.example.eventscollectorservice.entity.Device;
import org.mapstruct.Mapper;

@Mapper
public interface DeviceMapper {

    Device toDevice(DeviceEvent deviceEvent);

    DeviceResponse toDeviceResponse(Device device);
}
