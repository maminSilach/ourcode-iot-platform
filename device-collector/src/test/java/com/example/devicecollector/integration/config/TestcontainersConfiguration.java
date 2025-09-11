package com.example.devicecollector.integration.config;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
abstract class TestcontainersConfiguration {

    protected static final String POSTGRES_USER = "postgres";
    protected static final String POSTGRES_PASSWORD = "postgres";

    private static final Network NETWORK = Network.newNetwork();

    @Container
    public static KafkaContainer kafkaContainer =
            new KafkaContainer(DockerImageName.parse("apache/kafka-native:latest"))
                    .withListener("kafka:19092")
                    .withNetwork(NETWORK);

    @Container
    public static GenericContainer schemaRegistryContainer =
            new GenericContainer(DockerImageName.parse("confluentinc/cp-schema-registry:7.5.0"))
                    .withNetwork(NETWORK)
                    .withExposedPorts(8081)
                    .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                    .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:19092")
                    .dependsOn(kafkaContainer);

    @Container
    public static PostgreSQLContainer<?> shard0Master = new PostgreSQLContainer<>("postgres:15-alpine")
            .withExposedPorts(5432)
            .withUsername(POSTGRES_USER)
            .withPassword(POSTGRES_PASSWORD)
            .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                    new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(53676), new ExposedPort(5432)))
            ));

    @Container
    public static PostgreSQLContainer<?> shard0Replica = new PostgreSQLContainer<>("postgres:15-alpine")
            .withExposedPorts(5432)
            .withUsername(POSTGRES_USER)
            .withPassword(POSTGRES_PASSWORD)
            .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                    new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(53692), new ExposedPort(5432)))
            ));

    @Container
    public static PostgreSQLContainer<?> shard1Master = new PostgreSQLContainer<>("postgres:15-alpine")
            .withExposedPorts(5432)
            .withUsername(POSTGRES_USER)
            .withPassword(POSTGRES_PASSWORD)
            .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                    new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(53683), new ExposedPort(5432)))
            ));

    @Container
    public static PostgreSQLContainer<?> shard1Replica = new PostgreSQLContainer<>("postgres:15-alpine")
            .withExposedPorts(5432)
            .withUsername(POSTGRES_USER)
            .withPassword(POSTGRES_PASSWORD)
            .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                    new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(53690), new ExposedPort(5432)))
            ));

    @DynamicPropertySource
    protected static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("kafka.server", kafkaContainer::getBootstrapServers);

        registry.add("spring.datasource.url.shard-master0", () -> getPostgresAddressWithDatabase(shard0Master));
        registry.add("spring.datasource.url.shard-replica0", () -> getPostgresAddressWithDatabase(shard0Replica));
        registry.add("spring.datasource.url.shard-master1", () -> getPostgresAddressWithDatabase(shard1Master));
        registry.add("spring.datasource.url.shard-replica1", () -> getPostgresAddressWithDatabase(shard1Replica));

        registry.add("flyway.dataSources[0].url", () -> getPostgresAddressWithDatabase(shard0Master));
        registry.add("flyway.dataSources[1].url", () -> getPostgresAddressWithDatabase(shard0Replica));
        registry.add("flyway.dataSources[2].url", () -> getPostgresAddressWithDatabase(shard1Master));
        registry.add("flyway.dataSources[3].url", () -> getPostgresAddressWithDatabase(shard1Replica));

        registry.add("flyway.dataSources[0].username", () -> POSTGRES_USER);
        registry.add("flyway.dataSources[1].username", () -> POSTGRES_USER);
        registry.add("flyway.dataSources[2].username", () -> POSTGRES_USER);
        registry.add("flyway.dataSources[3].username", () -> POSTGRES_USER);

        registry.add("flyway.dataSources[0].password", () -> POSTGRES_PASSWORD);
        registry.add("flyway.dataSources[1].password", () -> POSTGRES_PASSWORD);
        registry.add("flyway.dataSources[2].password", () -> POSTGRES_PASSWORD);
        registry.add("flyway.dataSources[3].password", () -> POSTGRES_PASSWORD);

        registry.add("spring.datasource.username.shard-master0", () -> POSTGRES_USER);
        registry.add("spring.datasource.username.shard-master1", () -> POSTGRES_USER);
        registry.add("spring.datasource.username.shard-replica0", () -> POSTGRES_USER);
        registry.add("spring.datasource.username.shard-replica1", () -> POSTGRES_USER);

        registry.add("spring.datasource.password.shard-master0", () -> POSTGRES_PASSWORD);
        registry.add("spring.datasource.password.shard-master1", () -> POSTGRES_PASSWORD);
        registry.add("spring.datasource.password.shard-replica0", () -> POSTGRES_PASSWORD);
        registry.add("spring.datasource.password.shard-replica1", () -> POSTGRES_PASSWORD);


        registry.add("schema.registry.url",
                () -> "http://" + schemaRegistryContainer.getHost() + ":" + schemaRegistryContainer.getMappedPort(8081));
    }

    private static String getPostgresAddressWithDatabase(PostgreSQLContainer<?> shard) {
        return "jdbc:postgresql://" + shard.getHost() + ":" + shard.getFirstMappedPort() + "/" + shard.getDatabaseName();
    }
}
