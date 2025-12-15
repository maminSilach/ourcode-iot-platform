package com.example.apiorchestrator.configuration;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GrpcConfiguration {

    @Value("${command.service.host}")
    private final String host;

    @Value("${command.service.port}")
    private final int port;

    @Value("${command.service.max-retry}")
    private final int maxRetry;

    @Bean
    public ManagedChannel routerManagedChannel() {
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .maxRetryAttempts(maxRetry)
                .build();
    }
}
