package com.example.eventservice.unit;

import com.example.eventservice.dto.request.EventFilter;
import com.example.eventservice.dto.request.EventParameter;
import com.example.eventservice.dto.response.EventResponse;
import com.example.eventservice.entity.Event;
import com.example.eventservice.exception.NotFoundException;
import com.example.eventservice.mapper.EventMapperImpl;
import com.example.eventservice.repository.EventRepository;
import com.example.eventservice.service.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Spy
    private EventMapperImpl eventMapper;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void getEventsByFilters_shouldReturnPageOfEventResponses() {
        var eventFilter = new EventFilter(100L, 2000L, "SomeType");
        var eventParameter = new EventParameter(eventFilter, "device123", 0, 100);
        var event = new Event("device123", "event123", 10000L, "CL", "some-payload");

        when(eventRepository.loadEventsByFilters(eventParameter)).thenReturn(new SliceImpl<>(List.of(event)));

        var result = eventService.getEventsByFilters(eventParameter);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).containsExactly(
                new EventResponse(event.getDeviceId(), event.getEventId(), event.getTimestamp(), event.getType(), event.getPayload())
        );

        verify(eventRepository).loadEventsByFilters(eventParameter);
        verify(eventMapper, times(1)).toEventResponse(any());
    }

    @Test
    void getEvent_withNonExistingEvent_shouldThrowNotFoundException() {
        String eventId = "nonExistingEvent";
        String deviceId = "device456";

        when(eventRepository.loadEvent(eventId, deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEvent(eventId, deviceId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event with event_id " + eventId + " and device_id " + deviceId + " not found");

        verify(eventRepository).loadEvent(eventId, deviceId);
        verify(eventMapper, never()).toEventResponse(any());
    }

    @Test
    void getEvent_withNullDeviceId_shouldThrowNotFoundException() {
        String eventId = "event123";
        String deviceId = null;

        when(eventRepository.loadEvent(eventId, deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEvent(eventId, deviceId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event with event_id " + eventId + " and device_id " + deviceId + " not found");
    }

    @Test
    void getEvent_withEmptyStrings_shouldThrowNotFoundException() {
        String eventId = "";
        String deviceId = "";

        when(eventRepository.loadEvent(eventId, deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEvent(eventId, deviceId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event with event_id " + eventId + " and device_id " + deviceId + " not found");
    }
}