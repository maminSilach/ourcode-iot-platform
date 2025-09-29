package com.example.failedeventsprocessor.integration.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
abstract class TestcontainersConfiguration {

    private static final String MINIO_ADMIN_ACCESS_KEY = "admin";
    private static final String MINIO_ADMIN_SECRET_KEY = "admin1234";
    private static final String MINIO_REGION_STATIC = "us-west-1";

    private static final Network NETWORK = Network.newNetwork();

    @Container
    public static KafkaContainer kafkaContainer =
            new KafkaContainer(DockerImageName.parse("apache/kafka-native:latest"))
                    .withListener("kafka:19092")
                    .withNetwork(NETWORK);

    @Container
    public static MinIOContainer minIOContainer =
            new MinIOContainer(DockerImageName.parse("minio/minio:latest"))
                    .withUserName(MINIO_ADMIN_ACCESS_KEY)
                    .withPassword(MINIO_ADMIN_SECRET_KEY)
                    .withEnv("MINIO_REGION_NAME", MINIO_REGION_STATIC)
                    .withCommand("server /data");

    @Container
    public static GenericContainer schemaRegistryContainer =
            new GenericContainer(DockerImageName.parse("confluentinc/cp-schema-registry:7.5.0"))
                    .withNetwork(NETWORK)
                    .withExposedPorts(8081)
                    .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                    .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:19092")
                    .dependsOn(kafkaContainer);


    @DynamicPropertySource
    protected static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("kafka.server", kafkaContainer::getBootstrapServers);

        registry.add("aws.host", () -> "http://" + minIOContainer.getHost() + ":" + minIOContainer.getFirstMappedPort());
        registry.add("aws.secret-key", () -> MINIO_ADMIN_SECRET_KEY);
        registry.add("aws.access-key-id", () -> MINIO_ADMIN_ACCESS_KEY);
        registry.add("aws.region-static", () -> MINIO_REGION_STATIC);


        registry.add("schema.registry.url",
                () -> "http://" + schemaRegistryContainer.getHost() + ":" + schemaRegistryContainer.getMappedPort(8081));
    }
}
