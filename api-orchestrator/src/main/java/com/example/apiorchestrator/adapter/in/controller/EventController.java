package com.example.apiorchestrator.adapter.in.controller;

import com.example.apiorchestrator.domain.dto.request.filter.EventFilter;
import com.example.apiorchestrator.domain.dto.request.filter.EventParameter;
import com.example.apiorchestrator.domain.port.in.EventUseCase;
import com.example.eventservice.model.Event;
import com.example.eventservice.model.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventUseCase eventUseCase;

    @GetMapping("/{event_id}")
    public Mono<Event> getEvent(@PathVariable("event_id") String eventId, @RequestParam("device_id") String deviceId) {
       return eventUseCase.getEvent(eventId, deviceId).log();
    }

    @GetMapping
    public Mono<PageResponse> getEvents(
            @RequestParam(name = "device_id") String deviceId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize,
            EventFilter filter
    ) {
        var eventParameter = EventParameter.of(filter, deviceId, page, pageSize);
        return eventUseCase.getEvents(eventParameter).log();
    }
}
