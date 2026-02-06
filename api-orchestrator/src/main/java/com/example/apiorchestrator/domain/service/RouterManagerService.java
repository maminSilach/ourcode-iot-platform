package com.example.apiorchestrator.domain.service;

import com.example.apiorchestrator.domain.dto.request.CommandRequest;
import com.example.apiorchestrator.domain.mapper.RouterManagerMapper;
import com.example.apiorchestrator.domain.port.in.RouterManagerUseCase;
import com.example.apiorchestrator.domain.port.out.RouterManagerClient;
import com.google.protobuf.Empty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class RouterManagerService implements RouterManagerUseCase {

    private final RouterManagerClient routerManagerClient;
    private final RouterManagerMapper routerManagerMapper;

    @Override
    public Mono<Empty> sendCommand(CommandRequest commandRequest) {
       var protoCommandRequest = routerManagerMapper.toProtoCommand(commandRequest);

        return Mono.fromCallable(
                () -> routerManagerClient.sendCommand(protoCommandRequest)
        ).subscribeOn(
                Schedulers.boundedElastic()
        );
    }
}
