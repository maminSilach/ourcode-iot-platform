package com.example.apiorchestrator.domain.mapper;

import com.example.apiorchestrator.domain.dto.request.CommandRequest;
import com.example.apiorchestrator.domain.dto.request.CommandType;
import com.example.apiorchestrator.domain.dto.request.DeviceVersionRequest;
import com.example.apiorchestrator.domain.dto.response.DeviceVersionResponse;
import com.example.apiorchestrator.enums.EventStatus;
import com.example.deviceservice.model.DeviceRequest;
import com.example.deviceservice.model.DeviceResponse;
import com.example.eventservice.model.ApiV1EventsDevicesVersionPostRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeviceMapper {

    @Autowired
    private ObjectMapper objectMapper;

    public ApiV1EventsDevicesVersionPostRequest toEventRequest(DeviceVersionRequest deviceVersionRequest) {
        return new ApiV1EventsDevicesVersionPostRequest()
                .deviceId(deviceVersionRequest.getDeviceId())
                .idempotencyKey(deviceVersionRequest.getIdempotenceKey())
                .status(EventStatus.UPDATING.name())
                .targetVersion(deviceVersionRequest.getTargetVersion());
    }

    public DeviceRequest toDeviceRequest(DeviceVersionRequest deviceVersionRequest) {
        return new DeviceRequest()
                .id(deviceVersionRequest.getDeviceId())
                .targetVersion(deviceVersionRequest.getTargetVersion())
                .idempotenceKey(deviceVersionRequest.getIdempotenceKey());
    }


    public DeviceRequest toRollbackDeviceRequest(DeviceVersionRequest deviceVersionRequest) {
        return new DeviceRequest()
                .id(deviceVersionRequest.getDeviceId())
                .targetVersion(deviceVersionRequest.getOldTargetVersion())
                .idempotenceKey(deviceVersionRequest.getIdempotenceKey());
    }


    @SneakyThrows
    public CommandRequest tomCommandRequest(DeviceVersionRequest deviceVersionRequest) {
        return new CommandRequest(
               null,
                CommandType.PING,
                objectMapper.writeValueAsString(deviceVersionRequest)
        );
    }

    public DeviceVersionResponse toDeviceVersionResponse(DeviceResponse deviceResponse) {
        return new DeviceVersionResponse(
                deviceResponse.getId(),
                deviceResponse.getOldTargetVersion(),
                deviceResponse.getNewTargetVersion(),
                deviceResponse.getIdempotenceKey()
        );
    }
}
