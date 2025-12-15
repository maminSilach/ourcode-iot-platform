package com.example.apiorchestrator.adapter.in.controller;

import com.example.apiorchestrator.domain.dto.request.CommandRequest;
import com.example.apiorchestrator.domain.port.in.RouterManagerUseCase;
import com.google.protobuf.Empty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/commands")
public class RouterManagerController {

    private final RouterManagerUseCase routerManagerUseCase;

    @PostMapping
    public Mono<Empty> sendCommand(@RequestBody CommandRequest commandRequest) {
        return routerManagerUseCase.sendCommand(commandRequest).log();
    }
}
