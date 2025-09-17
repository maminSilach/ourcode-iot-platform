package com.example.deviceservice.integration.config;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
abstract class TestcontainersConfiguration {

    protected static final String POSTGRES_USER = "postgres";
    protected static final String POSTGRES_PASSWORD = "postgres";

    protected static final String KEYCLOAK_USER = "test";
    protected static final String KEYCLOAK_PASSWORD = "test";
    protected static final String KEYCLOAK_CLIENT_ID = "device-client";

    @Container
    public static KeycloakContainer keycloakContainer = new KeycloakContainer()
            .withRealmImportFile("realm-out-platform.json");

    @Container
    public static PostgreSQLContainer<?> shard0Master = new PostgreSQLContainer<>("postgres:15-alpine")
            .withExposedPorts(5432)
            .withUsername(POSTGRES_USER)
            .withPassword(POSTGRES_PASSWORD)
            .withInitScript("db/init_devices.sql")
            .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                    new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(53676), new ExposedPort(5432)))
            ));

    @Container
    public static PostgreSQLContainer<?> shard0Replica = new PostgreSQLContainer<>("postgres:15-alpine")
            .withExposedPorts(5432)
            .withUsername(POSTGRES_USER)
            .withPassword(POSTGRES_PASSWORD)
            .withInitScript("db/init_devices.sql")
            .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                    new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(53692), new ExposedPort(5432)))
            ));

    @Container
    public static PostgreSQLContainer<?> shard1Master = new PostgreSQLContainer<>("postgres:15-alpine")
            .withExposedPorts(5432)
            .withUsername(POSTGRES_USER)
            .withPassword(POSTGRES_PASSWORD)
            .withInitScript("db/init_devices.sql")
            .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                    new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(53683), new ExposedPort(5432)))
            ));

    @Container
    public static PostgreSQLContainer<?> shard1Replica = new PostgreSQLContainer<>("postgres:15-alpine")
            .withExposedPorts(5432)
            .withUsername(POSTGRES_USER)
            .withPassword(POSTGRES_PASSWORD)
            .withInitScript("db/init_devices.sql")
            .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                    new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(53690), new ExposedPort(5432)))
            ));

    @DynamicPropertySource
    protected static void overrideProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url.shard-master0", () -> getPostgresAddressWithDatabase(shard0Master));
        registry.add("spring.datasource.url.shard-replica0", () -> getPostgresAddressWithDatabase(shard0Replica));
        registry.add("spring.datasource.url.shard-master1", () -> getPostgresAddressWithDatabase(shard1Master));
        registry.add("spring.datasource.url.shard-replica1", () -> getPostgresAddressWithDatabase(shard1Replica));

        registry.add("spring.datasource.username.shard-master0", () -> POSTGRES_USER);
        registry.add("spring.datasource.username.shard-master1", () -> POSTGRES_USER);
        registry.add("spring.datasource.username.shard-replica0", () -> POSTGRES_USER);
        registry.add("spring.datasource.username.shard-replica1", () -> POSTGRES_USER);

        registry.add("spring.datasource.password.shard-master0", () -> POSTGRES_PASSWORD);
        registry.add("spring.datasource.password.shard-master1", () -> POSTGRES_PASSWORD);
        registry.add("spring.datasource.password.shard-replica0", () -> POSTGRES_PASSWORD);
        registry.add("spring.datasource.password.shard-replica1", () -> POSTGRES_PASSWORD);

        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", TestcontainersConfiguration::getAuthUrl);
    }

    protected static String getAuthUrl() {
        return keycloakContainer.getAuthServerUrl() + "/realms/out-platform";
    }

    protected static String getAuthOpenIdConnectTokenUrl() {
        return getAuthUrl() + "/protocol/openid-connect/token";
    }

    private static String getPostgresAddressWithDatabase(PostgreSQLContainer<?> shard) {
        return "jdbc:postgresql://" + shard.getHost() + ":" + shard.getFirstMappedPort() + "/" + shard.getDatabaseName();
    }
}
