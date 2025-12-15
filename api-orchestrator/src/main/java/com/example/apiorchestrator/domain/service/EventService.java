package com.example.apiorchestrator.domain.service;

import com.example.apiorchestrator.domain.dto.request.filter.EventParameter;
import com.example.apiorchestrator.domain.port.in.EventUseCase;
import com.example.apiorchestrator.domain.port.out.EventClient;
import com.example.eventservice.model.Event;
import com.example.eventservice.model.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class EventService implements EventUseCase {

    private final EventClient eventClient;

    @Override
    public Mono<Event> getEvent(String eventId, String deviceId) {
        return Mono
                .fromCallable(() -> eventClient.getEvent(eventId, deviceId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<PageResponse> getEvents(EventParameter eventParameter) {
        return Mono.fromCallable(
                () -> eventClient.getEvents(eventParameter)
        ).subscribeOn(Schedulers.boundedElastic());
    }
}
