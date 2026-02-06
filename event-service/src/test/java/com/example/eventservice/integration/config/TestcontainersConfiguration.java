package com.example.eventservice.integration.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.cassandra.CassandraContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
abstract class TestcontainersConfiguration {

    @Container
    protected static GenericContainer<?> cassandraContainer =
            new CassandraContainer(DockerImageName.parse("cassandra:3.11.2"))
                    .withInitScript("init.cql")
                    .withExposedPorts(9042);

    @DynamicPropertySource
    protected static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cassandra.contact-points",() -> cassandraContainer.getHost());
        registry.add("spring.cassandra.port", () -> cassandraContainer.getMappedPort(9042));
        registry.add("spring.cassandra.local-datacenter", () -> "datacenter1");
    }
}
