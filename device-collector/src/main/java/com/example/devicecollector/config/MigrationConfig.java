package com.example.devicecollector.config;

import com.example.devicecollector.dto.MigrationSource;
import lombok.Data;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "flyway")
public class MigrationConfig {

    private List<MigrationSource> dataSources;

    private String location;

    @Bean
    public FlywayMigrationInitializer flywayMigrationInitializer() {
        return new FlywayMigrationInitializer(
                Flyway.configure().load(),
                _ -> dataSources.forEach(this::migrateSource)
        );
    }

    private void migrateSource(MigrationSource source) {
        Flyway.configure()
                .dataSource(source.url(), source.username(), source.password())
                .locations(location)
                .load()
                .migrate();
    }
}
