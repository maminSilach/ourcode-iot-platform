package com.example.eventservice.mapper;

import com.example.eventservice.dto.response.EventResponse;
import com.example.eventservice.entity.Event;
import org.mapstruct.Mapper;

@Mapper
public interface EventMapper {

    EventResponse toEventResponse(Event event);
}
