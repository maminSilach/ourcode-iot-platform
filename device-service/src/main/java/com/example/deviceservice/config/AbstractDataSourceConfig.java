package com.example.deviceservice.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;

public abstract class AbstractDataSourceConfig {

    @Value("${spring.datasource.pool-size}")
    protected int poolSize;

    @Value("${spring.datasource.timeout.connection}")
    protected int connectionTimeout;

    @Value("${spring.datasource.timeout.idle}")
    protected int poolIdleTimeout;

    @Value("${spring.datasource.max-life-time}")
    protected int poolMaxLifetime;


    protected DataSource configureBaseDataSource(String url, String username, String password) {
        HikariDataSource dataSource = new HikariDataSource();

        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMaximumPoolSize(poolSize);
        dataSource.setConnectionTimeout(connectionTimeout);
        dataSource.setIdleTimeout(poolIdleTimeout);
        dataSource.setMaxLifetime(poolMaxLifetime);

        return dataSource;
    }
}
