package com.example.eventservice.integration.event;

import com.example.eventservice.dto.response.ErrorResponse;
import com.example.eventservice.dto.response.EventResponse;
import com.example.eventservice.entity.Event;
import com.example.eventservice.integration.config.EventServiceConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.example.eventservice.integration.utils.EventData.*;
import static org.assertj.core.api.Assertions.assertThat;

public class EventTest extends EventServiceConfiguration {

    private static final  String BASE_EVENT_V1_ENDPOINT = "/api/v1/events";
    private static final String GET_EVENTS_V1_ENDPOINT = BASE_EVENT_V1_ENDPOINT;
    private static final String GET_EVENT_V1_ENDPOINT = "%s/%s".formatted(GET_EVENTS_V1_ENDPOINT, "{event_id}");

    private static final String DEVICE_ID_PARAM = "device_id";
    private static final String FROM_TIMESTAMP_PARAM = "fromTimestamp";
    private static final String TO_TIMESTAMP_PARAM = "toTimestamp";
    private static final String TYPE_PARAM = "type";

    @Test
    void events_getEventByIdAndDeviceId_successfully() throws Exception {
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(GET_EVENT_V1_ENDPOINT, EVENT_ID)
                        .param(DEVICE_ID_PARAM, DEVICE_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        var eventResponse = deserializeResponse(EventResponse.class, mvcResult);

        assertThat(
                assertionsEventResponseByEvent(eventResponse, EVENT)
        ).isTrue();
    }

    @Test
    void events_getEventByIdAndDeviceId_notFound() throws Exception {
        var nonExistsDeviceId = UUID.randomUUID().toString();

        var result = mockMvc.perform(MockMvcRequestBuilders.get(GET_EVENT_V1_ENDPOINT, EVENT_ID)
                        .param(DEVICE_ID_PARAM, nonExistsDeviceId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();

        ErrorResponse errorResponse = deserializeResponse(ErrorResponse.class, result);
        assertThat(errorResponse.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(errorResponse.type()).isEqualTo(BASE_EVENT_V1_ENDPOINT + "/" + EVENT_ID);
        assertThat(errorResponse.title()).isEqualTo(
                "Event with event_id %s and device_id %s not found"
                        .formatted(
                                EVENT_ID,
                                nonExistsDeviceId
                        )
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void events_getEventWithFilters_successfullyGetWithApplyFilters() throws Exception {
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(GET_EVENTS_V1_ENDPOINT)
                        .param(DEVICE_ID_PARAM, DEVICE_ID)
                        .param(FROM_TIMESTAMP_PARAM, "1001")
                        .param(TO_TIMESTAMP_PARAM, "100001")
                        .param(TYPE_PARAM, "CL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        var eventMap = deserializeResponse(Map.class, mvcResult);
        var pageableInfo = (Map<String, Object>) eventMap.get("page");

        assertThat(pageableInfo.get("totalElements")).isEqualTo(1);
        assertThat(pageableInfo.get("size")).isEqualTo(1);
        assertThat(pageableInfo.get("number")).isEqualTo(0);
        assertThat(pageableInfo.get("totalPages")).isEqualTo(1);

        var events = ((List<?>) eventMap.get("content")).stream()
                .map(item -> objectMapper.convertValue(item, EventResponse.class))
                .toList();

        assertThat(events.size()).isEqualTo(1);

        assertThat(
                assertionsEventResponseByEvent(events.getFirst(), EVENT)
        ).isTrue();
    }

    private <T> T deserializeResponse(Class<T> clazz, MvcResult mvcResult) throws IOException {
        return objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), clazz);
    }

    private boolean assertionsEventResponseByEvent(EventResponse eventResponse, Event event) {
        return eventResponse.eventId().equals(event.getEventId())
                && eventResponse.deviceId().equals(event.getDeviceId())
                && eventResponse.timestamp().equals(event.getTimestamp())
                && eventResponse.type().equals(event.getType());
    }
}
