package com.example.apiorchestrator.domain.port.in;

import com.example.apiorchestrator.domain.dto.request.filter.EventParameter;
import com.example.eventservice.model.Event;
import com.example.eventservice.model.PageResponse;
import reactor.core.publisher.Mono;

public interface EventUseCase {

    Mono<Event> getEvent(String eventId, String deviceId);

    Mono<PageResponse> getEvents(EventParameter eventParameter);
}
