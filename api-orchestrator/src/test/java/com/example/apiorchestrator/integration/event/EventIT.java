package com.example.apiorchestrator.integration.event;

import com.example.apiorchestrator.integration.ApiOrchestratorApplicationTests;
import com.example.apiorchestrator.utils.JwtUtils;
import com.example.eventservice.model.Event;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.UUID;

import static com.example.apiorchestrator.integration.utils.Data.EVENT_RESPONSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.MediaType.APPLICATION_JSON;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class EventIT extends ApiOrchestratorApplicationTests {

    private static final String BASE_EVENT_PATH = "/api/v1/events/.*";

    private static String bearerAuthorizationHeader;

    @BeforeAll
    public static void beforeAll() throws URISyntaxException {
        bearerAuthorizationHeader = JwtUtils.getBearerAuthorizationHeader(
                getAuthOpenIdConnectTokenUrl()
        );
    }

    @BeforeEach
    void setup() {
        EVENT_CLIENT.reset();
    }

    @Test
    public void getEvent_withValidParameters_shouldReturnEvent() {
        String eventId = "5bc25367-a787-4f0a-99a9-85df4e60fed6";
        String deviceId = "be932527-e70a-40cf-82d1-035ef583e895";

        EVENT_CLIENT
                .when(request()
                        .withMethod("GET")
                        .withPath(BASE_EVENT_PATH)
                        .withQueryStringParameter("device_id", deviceId))
                .respond(response()
                        .withStatusCode(200)
                        .withBody(EVENT_RESPONSE)
                        .withContentType(APPLICATION_JSON));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/events/{event_id}")
                        .queryParam("device_id", deviceId)
                        .build(eventId))
                .header(AUTHORIZATION, bearerAuthorizationHeader)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Event.class)
                .consumeWith(result -> {
                            Event event = result.getResponseBody();
                            assertThat(event).isNotNull();
                            assertThat(event.getEventId()).isEqualTo(eventId);
                            assertThat(event.getDeviceId()).isEqualTo(deviceId);
                        }
                );

    }

    @Test
    public void getEvent_withInvalidEventId_shouldReturnNotFound() {
        String eventId = UUID.randomUUID().toString();
        String deviceId = "be932527-e70a-40cf-82d1-035ef583e895";

        EVENT_CLIENT.reset();
        EVENT_CLIENT
                .when(request()
                        .withMethod("GET")
                        .withPath(BASE_EVENT_PATH)
                        .withQueryStringParameter("device_id", deviceId))
                .respond(response()
                        .withStatusCode(404));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BASE_EVENT_PATH + "/{event_id}")
                        .queryParam("device_id", deviceId)
                        .build(eventId))
                .header(AUTHORIZATION, bearerAuthorizationHeader)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void getEvents_withMissingDeviceId_shouldReturnBadRequest() {
        webTestClient.get()
                .uri(BASE_EVENT_PATH)
                .header(AUTHORIZATION, bearerAuthorizationHeader)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
