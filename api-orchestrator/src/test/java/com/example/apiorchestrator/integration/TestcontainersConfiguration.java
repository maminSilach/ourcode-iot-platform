package com.example.apiorchestrator.integration;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.mockserver.client.MockServerClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class TestcontainersConfiguration {

    public static KeycloakContainer KEYCLOAK = new KeycloakContainer()
            .withRealmImportFile("realm-out-platform.json");

    protected static final MockServerContainer MOCK_SERVER_CONTAINER =
            new MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.15.0"));

    protected static MockServerClient DEVICE_CLIENT;

    protected static MockServerClient EVENT_CLIENT;

    static {
        MOCK_SERVER_CONTAINER.start();
        KEYCLOAK.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {

        configureMockClients();

        // Event
        registry.add("event.service.base-uri", MOCK_SERVER_CONTAINER::getEndpoint);

        // Device
        registry.add("device.service.base-uri", MOCK_SERVER_CONTAINER::getEndpoint);

        // Command
        registry.add("command.service.host", MOCK_SERVER_CONTAINER::getHost);
        registry.add("command.service.post", MOCK_SERVER_CONTAINER::getFirstMappedPort);

        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", TestcontainersConfiguration::getAuthUrl);
        registry.add("security.oauth2.client.provider.keycloak.token-uri", TestcontainersConfiguration::getAuthOpenIdConnectTokenUrl);
    }

    private static void configureMockClients() {

        DEVICE_CLIENT = new MockServerClient(
                MOCK_SERVER_CONTAINER.getHost(),
                MOCK_SERVER_CONTAINER.getServerPort()
        );


        EVENT_CLIENT = new MockServerClient(
                MOCK_SERVER_CONTAINER.getHost(),
                MOCK_SERVER_CONTAINER.getServerPort()
        );
    }

    protected static String getAuthUrl() {
        return KEYCLOAK.getAuthServerUrl() + "/realms/out-platform";
    }

    protected static String getAuthOpenIdConnectTokenUrl() {
        return getAuthUrl() + "/protocol/openid-connect/token";
    }
}
