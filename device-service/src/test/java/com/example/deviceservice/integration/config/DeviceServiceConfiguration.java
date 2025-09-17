package com.example.deviceservice.integration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@WebAppConfiguration
@ActiveProfiles("test")
public abstract class DeviceServiceConfiguration extends TestcontainersConfiguration {

    protected static final JdbcTemplate shard0JdbcTemplate = new JdbcTemplate(
            DataSourceBuilder.create()
                    .url(shard0Master.getJdbcUrl())
                    .username(POSTGRES_USER)
                    .password(POSTGRES_PASSWORD)
                    .build()
    );


    protected static final JdbcTemplate shard1JdbcTemplate = new JdbcTemplate(
            DataSourceBuilder.create()
                    .url(shard1Master.getJdbcUrl())
                    .username(POSTGRES_USER)
                    .password(POSTGRES_PASSWORD)
                    .build()
    );

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

}
