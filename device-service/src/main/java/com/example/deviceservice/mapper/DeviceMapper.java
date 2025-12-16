package com.example.deviceservice.mapper;


import com.example.deviceservice.dto.request.DeviceRequest;
import com.example.deviceservice.dto.response.DeviceResponse;
import com.example.deviceservice.model.Device;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DeviceMapper {

    Device toDevice(DeviceRequest deviceRequest);

    Device updateDevice(@MappingTarget Device device, DeviceRequest deviceRequest);

    DeviceResponse toDeviceResponse(Device device);

    DeviceResponse toDeviceResponse(Device device, String oldTargetVersion);
}
