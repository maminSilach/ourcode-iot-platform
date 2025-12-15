package com.example.apiorchestrator.adapter.out;

import com.example.apiorchestrator.domain.dto.request.filter.EventParameter;
import com.example.apiorchestrator.domain.port.out.EventClient;
import com.example.eventservice.api.EventApi;
import com.example.eventservice.model.Event;
import com.example.eventservice.model.PageResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventFeignClient implements EventClient {

    private final EventApi eventApi;

    @Override
    @Retry(name = "default")
    @CircuitBreaker(name = "default")
    public Event getEvent(String eventId, String deviceId) {
       return eventApi.apiV1EventsEventIdGet(eventId, deviceId);
    }

    @Override
    @Retry(name = "default")
    @CircuitBreaker(name = "default")
    public PageResponse getEvents(EventParameter eventParameter) {
        return eventApi.apiV1EventsGet(
                eventParameter.deviceId(),
                eventParameter.fromTimestamp(),
                eventParameter.toTimestamp(),
                eventParameter.type(),
                eventParameter.page(),
                eventParameter.pageSize()
        );
    }
}
