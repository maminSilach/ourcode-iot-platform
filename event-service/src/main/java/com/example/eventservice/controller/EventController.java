package com.example.eventservice.controller;

import com.example.eventservice.dto.request.EventFilter;
import com.example.eventservice.dto.request.EventParameter;
import com.example.eventservice.dto.response.EventResponse;
import com.example.eventservice.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<PagedModel<EventResponse>> getEventsWithFilters(
            @RequestParam(name = "device_id") String deviceId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize,
            EventFilter filter
    ) {
        var eventParameter = EventParameter.of(filter, deviceId, page, pageSize);
        var eventResponsePage = eventService.getEventsByFilters(eventParameter);

        return ResponseEntity.ok(new PagedModel<>(eventResponsePage));
    }

    @GetMapping("/{event_id}")
    public ResponseEntity<EventResponse> getEventByIdAndDeviceId(
            @PathVariable(name = "event_id") String eventId,
            @RequestParam(name = "device_id") String deviceId
    ) {
        var eventResponse = eventService.getEvent(eventId, deviceId);

        return ResponseEntity.ok(eventResponse);
    }
}
