package com.example.apiorchestrator.domain.service;

import com.example.apiorchestrator.domain.dto.request.DeviceVersionRequest;
import com.example.apiorchestrator.domain.dto.response.DeviceVersionResponse;
import com.example.apiorchestrator.domain.mapper.DeviceMapper;
import com.example.apiorchestrator.domain.port.in.DeviceUseCase;
import com.example.apiorchestrator.domain.port.in.EventUseCase;
import com.example.apiorchestrator.domain.port.in.RouterManagerUseCase;
import com.example.apiorchestrator.domain.port.out.DeviceClient;
import com.example.apiorchestrator.enums.FacadeService;
import com.example.apiorchestrator.enums.SagaStatus;
import com.example.apiorchestrator.exception.SagaException;
import com.example.deviceservice.model.DeviceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService implements DeviceUseCase {

    private final DeviceClient deviceClient;
    private final EventUseCase eventUseCase;
    private final DeviceMapper deviceMapper;
    private final RouterManagerUseCase routerManagerUseCase;

    @Override
    public Mono<DeviceResponse> getDevice(String deviceId) {
        return Mono.fromCallable(
                () -> deviceClient.getDevice(deviceId)
        ).subscribeOn(
                Schedulers.boundedElastic()
        );
    }

    @Override
    public Flux<DeviceResponse> getDevices() {
        return Flux.defer(
                () -> Flux.fromIterable(deviceClient.getDevices())
        ).subscribeOn(
                Schedulers.boundedElastic()
        );
    }

    @Override
    public Mono<DeviceVersionResponse> updateDeviceVersion(DeviceVersionRequest deviceVersionRequest) {
        return Mono.fromCallable(
                        () -> sagaUpdateDeviceVersion(deviceVersionRequest)
                )
                .subscribeOn(
                        Schedulers.boundedElastic()
                );
    }

    public DeviceVersionResponse sagaUpdateDeviceVersion(DeviceVersionRequest deviceVersionRequest) {
        var status = SagaStatus.START;
        try {
            var eventRequest = deviceMapper.toEventRequest(deviceVersionRequest);

            var event = eventUseCase.createEvent(eventRequest);
            deviceVersionRequest.setEventId(event.getEventId());
            status = SagaStatus.DEVICE;

            var deviceRequest = deviceMapper.toDeviceRequest(deviceVersionRequest);
            var device = deviceClient.updateDevice(deviceVersionRequest.getDeviceId(), deviceRequest);
            deviceVersionRequest.setOldTargetVersion(device.getOldTargetVersion());

            status = SagaStatus.COMMAND;

            var commandRequest = deviceMapper.tomCommandRequest(deviceVersionRequest);
            routerManagerUseCase.sendCommand(commandRequest).block();

            return deviceMapper.toDeviceVersionResponse(device);
        } catch (Exception e) {
            var facadeService = FacadeService.ORCHESTRATOR;

            switch (status) {
                case DEVICE -> {
                    facadeService = FacadeService.DEVICE_SERVICE;
                    eventUseCase.rollbackSagaVersion(deviceVersionRequest.getEventId(), deviceVersionRequest.getDeviceId());
                }
                case COMMAND -> {
                    facadeService = FacadeService.ROUTER_MANAGER_SERVICE;
                    String eventId = deviceVersionRequest.getEventId();
                    String deviceId = deviceVersionRequest.getDeviceId();
                    eventUseCase.rollbackSagaVersion(eventId, deviceId);

                    var deviceRequest = deviceMapper.toRollbackDeviceRequest(deviceVersionRequest);
                    deviceClient.updateDevice(deviceVersionRequest.getDeviceId(), deviceRequest);
                }
            }

            throw new SagaException(e, facadeService);
        }
    }
}
