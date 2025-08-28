package com.example.eventscollectorservice.integration.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;


@Testcontainers
abstract class TestcontainersConfiguration {

    private static final Network NETWORK = Network.newNetwork();

    @Container
    protected static KafkaContainer kafkaContainer =
            new KafkaContainer(DockerImageName.parse("apache/kafka-native:latest"))
                    .withListener("kafka:19092")
                    .withNetwork(NETWORK);

    @Container
    protected static GenericContainer cassandraContainer =
            new CassandraContainer(DockerImageName.parse("cassandra:3.11.2"))
                    .withInitScript("init.cql")
                    .withExposedPorts(9042);

    @Container
    protected static GenericContainer schemaRegistryContainer =
            new GenericContainer(DockerImageName.parse("confluentinc/cp-schema-registry:7.5.0"))
                    .withNetwork(NETWORK)
                    .withExposedPorts(8081)
                    .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                    .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:19092")
                    .dependsOn(kafkaContainer);

    @DynamicPropertySource
    protected static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("kafka.server", kafkaContainer::getBootstrapServers);

        registry.add("spring.cassandra.contact-points",() -> cassandraContainer.getHost());
        registry.add("spring.cassandra.port", () -> cassandraContainer.getMappedPort(9042));
        registry.add("spring.cassandra.local-datacenter", () -> "datacenter1");
        registry.add("keyspace-name", () -> "devicekeyspace");

        registry.add("schema.registry.url",
                () -> "http://" + schemaRegistryContainer.getHost() + ":" + schemaRegistryContainer.getMappedPort(8081));
    }
}
