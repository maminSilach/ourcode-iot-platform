package com.example.devicecollector.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter failedMessageCounter(MeterRegistry meterRegistry) {
        return Counter.builder("messages.failed.total")
                .description("Total number of failed messages")
                .tag("type", "message_processing")
                .register(meterRegistry);
    }
}
