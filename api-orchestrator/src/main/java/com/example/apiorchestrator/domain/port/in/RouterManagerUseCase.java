package com.example.apiorchestrator.domain.port.in;

import com.example.apiorchestrator.domain.dto.request.CommandRequest;
import com.google.protobuf.Empty;
import reactor.core.publisher.Mono;

public interface RouterManagerUseCase {

    Mono<Empty> sendCommand(CommandRequest commandRequest);
}
