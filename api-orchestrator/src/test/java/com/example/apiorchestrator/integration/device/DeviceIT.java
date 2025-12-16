package com.example.apiorchestrator.integration.device;

import com.example.apiorchestrator.domain.dto.request.DeviceVersionRequest;
import com.example.apiorchestrator.domain.dto.response.DeviceVersionResponse;
import com.example.apiorchestrator.domain.dto.response.SagaErrorResponse;
import com.example.apiorchestrator.enums.FacadeService;
import com.example.apiorchestrator.integration.ApiOrchestratorApplicationTests;
import com.example.apiorchestrator.integration.utils.Data;
import com.example.apiorchestrator.utils.JwtUtils;
import com.example.deviceservice.model.DeviceResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.net.URISyntaxException;

import static com.example.apiorchestrator.integration.utils.Data.DEVICE_RESPONSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.MediaType.APPLICATION_JSON;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class DeviceIT extends ApiOrchestratorApplicationTests {

    private static final String BASE_DEVICE_PATH = "/api/v1/devices";
    private static String bearerAuthorizationHeader;

    @BeforeAll
    public static void beforeAll() throws URISyntaxException {
        bearerAuthorizationHeader = JwtUtils.getBearerAuthorizationHeader(
                getAuthOpenIdConnectTokenUrl(), KEYCLOAK_CLIENT_ID, KEYCLOAK_USER, KEYCLOAK_PASSWORD
        );
    }

    @BeforeEach
    void setup() {
        DEVICE_CLIENT.reset();
        EVENT_CLIENT.reset();
    }

    @Test
    public void getDevice_withValidDeviceId_shouldReturnDevice() {
        String deviceId = "be932527-e70a-40cf-82d1-035ef583e895";

        DEVICE_CLIENT
                .when(request()
                        .withMethod("GET")
                        .withPath("/v1/devices/.*"))
                .respond(response()
                        .withStatusCode(200)
                        .withBody(DEVICE_RESPONSE)
                        .withContentType(APPLICATION_JSON));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BASE_DEVICE_PATH + "/{device_id}")
                        .build(deviceId))
                .header(AUTHORIZATION, bearerAuthorizationHeader)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DeviceResponse.class)
                .consumeWith(result -> {
                    DeviceResponse device = result.getResponseBody();
                    assertThat(device).isNotNull();
                    assertThat(device.getId()).isEqualTo(deviceId);
                    assertThat(device.getDeviceType()).isEqualTo("THERMOSTAT");
                    assertThat(device.getCreatedAt()).isNotNull();
                });
    }

    @Test
    public void getDevice_withInvalidDeviceId_shouldReturnNotFound() {
        String deviceId = "non-existent-device-id";

        DEVICE_CLIENT
                .when(request()
                        .withMethod("GET")
                        .withPath("/v1/devices/.*"))
                .respond(response()
                        .withStatusCode(404));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BASE_DEVICE_PATH + "/{device_id}")
                        .build(deviceId))
                .header(AUTHORIZATION, bearerAuthorizationHeader)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void updateDeviceVersion_withInvalidRequestBody_shouldReturnBadRequest() {
        String invalidJson = "{ invalid json }";

        webTestClient.post()
                .uri(BASE_DEVICE_PATH + "/version")
                .header(AUTHORIZATION, bearerAuthorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidJson)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void updateDeviceVersion_successfulSaga_shouldCompleteAllSteps() {
        String deviceId = "be932527-e70a-40cf-82d1-035ef583e895";
        String idempotenceKey = "idemp-12345";
        String oldVersion = "1.0.0";
        String newVersion = "1.1.0";

        DeviceVersionRequest request = new DeviceVersionRequest(
                deviceId,
                newVersion,
                idempotenceKey
        );

        EVENT_CLIENT
                .when(request()
                        .withMethod("POST")
                        .withPath("/api/v1/events/.*"))
                .respond(response()
                        .withStatusCode(201)
                        .withBody(Data.EVENT_RESPONSE)
                        .withContentType(APPLICATION_JSON));

        DEVICE_CLIENT
                .when(request()
                        .withMethod("PUT")
                        .withPath("/v1/devices/.*"))
                .respond(response()
                        .withStatusCode(200)
                        .withBody(DEVICE_RESPONSE)
                        .withContentType(APPLICATION_JSON));

        webTestClient.post()
                .uri(BASE_DEVICE_PATH + "/version")
                .header(AUTHORIZATION, bearerAuthorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DeviceVersionResponse.class)
                .consumeWith(result -> {
                            DeviceVersionResponse response = result.getResponseBody();
                            assertThat(response).isNotNull();
                            assertThat(response.deviceId()).isEqualTo(deviceId);
                            assertThat(response.prevVersion()).isEqualTo(oldVersion);
                            assertThat(response.targetVersion()).isEqualTo(newVersion);
                            assertThat(response.idempotenceKey()).isEqualTo(idempotenceKey);
                        }
                );
    }

    @Test
    public void updateDeviceVersion_whenEventServiceFails_shouldThrowException() {
        String deviceId = "be932527-e70a-40cf-82d1-035ef583e895";
        String idempotenceKey = "idemp-12345";
        String newVersion = "1.1.0";

        DeviceVersionRequest request = new DeviceVersionRequest(
                deviceId,
                newVersion,
                idempotenceKey
        );

        EVENT_CLIENT
                .when(request()
                        .withMethod("POST")
                        .withPath("/api/v1/events/.*"))
                .respond(response()
                        .withStatusCode(500)
                        .withBody("{\"error\": \"Event service unavailable\"}"));

        webTestClient.post()
                .uri(BASE_DEVICE_PATH + "/version")
                .header(AUTHORIZATION, bearerAuthorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(SagaErrorResponse.class)
                .consumeWith(result -> {
                            SagaErrorResponse response = result.getResponseBody();
                            assertThat(response).isNotNull();
                            assertThat(response.service()).isEqualTo(FacadeService.ORCHESTRATOR);
                            assertThat(response.compensated()).isTrue();
                            assertThat(response.errorResponse()).isNotNull();
                        }
                );
    }

    @Test
    public void updateDeviceVersion_withInvalidRequestBody_shouldReturnBadRequestEventService() {
        String invalidJson = "{ invalid json }";

        webTestClient.post()
                .uri(BASE_DEVICE_PATH + "/version")
                .header(AUTHORIZATION, bearerAuthorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidJson)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void updateDeviceVersion_successfulSaga_shouldReturnBadRequestDeviceService() {
        String deviceId = "be932527-e70a-40cf-82d1-035ef583e895";
        String idempotenceKey = "idemp-12345";
        String newVersion = "1.1.0";

        DeviceVersionRequest request = new DeviceVersionRequest(
                deviceId,
                newVersion,
                idempotenceKey
        );

        EVENT_CLIENT
                .when(request()
                        .withMethod("POST")
                        .withPath("/api/v1/events/.*"))
                .respond(response()
                        .withStatusCode(201)
                        .withBody(Data.EVENT_RESPONSE)
                        .withContentType(APPLICATION_JSON));

        EVENT_CLIENT
                .when(request()
                        .withMethod("PUT")
                        .withPath("/v1/events/.*"))
                .respond(response()
                        .withStatusCode(500)
                        .withBody("{\"error\": \"Event service unavailable\"}"));

        webTestClient.post()
                .uri(BASE_DEVICE_PATH + "/version")
                .header(AUTHORIZATION, bearerAuthorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(SagaErrorResponse.class)
                .consumeWith(result -> {
                            SagaErrorResponse response = result.getResponseBody();
                            assertThat(response).isNotNull();
                            assertThat(response.service()).isEqualTo(FacadeService.DEVICE_SERVICE);
                            assertThat(response.compensated()).isTrue();
                            assertThat(response.errorResponse()).isNotNull();
                        }
                );
    }
}