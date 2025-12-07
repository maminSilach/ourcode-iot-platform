package com.example.eventservice.service;

import com.example.eventservice.dto.request.EventParameter;
import com.example.eventservice.dto.response.EventResponse;
import com.example.eventservice.exception.NotFoundException;
import com.example.eventservice.mapper.EventMapper;
import com.example.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import static com.example.eventservice.util.CriteriaUtil.mapPage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventMapper eventMapper;
    private final EventRepository eventRepository;

    public Page<EventResponse> getEventsByFilters(EventParameter parameter) {
        log.info("Loading events with parameter: {}", parameter);
        var eventSlice = eventRepository.loadEventsByFilters(parameter);

        return mapPage(eventSlice, eventMapper::toEventResponse);
    }

    public EventResponse getEvent(String eventId, String deviceId) {
        return eventRepository.loadEvent(eventId, deviceId)
                .map(eventMapper::toEventResponse)
                .orElseThrow(
                        () -> new NotFoundException("Event with event_id " + eventId + " and device_id " + deviceId + " not found")
                );
    }
}
