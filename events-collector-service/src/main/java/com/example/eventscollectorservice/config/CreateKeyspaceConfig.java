package com.example.eventscollectorservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;
import org.springframework.data.cassandra.core.cql.keyspace.SpecificationBuilder;

import java.util.List;

@Configuration
@Profile("!test")
public class CreateKeyspaceConfig extends AbstractCassandraConfiguration {

    @Value("${spring.cassandra.keyspace-name}")
    private String keySpaceName;

    @Value("${spring.cassandra.model-path}")
    private String entityModelPath;

    @Override
    protected String getKeyspaceName() {
        return keySpaceName;
    }

    @Override
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        CreateKeyspaceSpecification specification =
                SpecificationBuilder.createKeyspace(keySpaceName)
                        .ifNotExists()
                        .with(KeyspaceOption.DURABLE_WRITES, true);

        return List.of(specification);
    }

    @Override
    public String[] getEntityBasePackages() {
        return new String[] {entityModelPath};
    }
}
