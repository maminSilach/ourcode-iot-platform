package com.example.apiorchestrator.domain.port.out;

import com.example.apiorchestrator.domain.dto.request.filter.EventParameter;
import com.example.eventservice.model.Event;
import com.example.eventservice.model.PageResponse;


public interface EventClient {

    Event getEvent(String eventId, String deviceId);

    PageResponse getEvents(EventParameter eventParameter);
}
