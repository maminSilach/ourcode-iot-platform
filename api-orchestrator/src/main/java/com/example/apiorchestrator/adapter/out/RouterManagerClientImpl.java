package com.example.apiorchestrator.adapter.out;

import com.example.apiorchestrator.domain.port.out.RouterManagerClient;
import com.example.proto.CommandRequest;
import com.example.proto.CommandServiceGrpc;
import com.google.protobuf.Empty;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.grpc.ManagedChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouterManagerClientImpl implements RouterManagerClient {

    private final ManagedChannel managedChannel;

    @Override
    @Retry(name = "default")
    @CircuitBreaker(name = "default")
    public Empty sendCommand(CommandRequest commandRequest) {
        var commandServiceBlockingStub = CommandServiceGrpc.newBlockingStub(managedChannel);
        return commandServiceBlockingStub.sendCommand(commandRequest);
    }
}
