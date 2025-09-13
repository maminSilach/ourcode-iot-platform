package com.example.devicecollector.config;

import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class ShardingDataSourceConfig extends AbstractDataSourceConfig {

    @Value("${spring.datasource.url.shard-master0}")
    protected String shardMaster0Url;

    @Value("${spring.datasource.url.shard-master1}")
    protected String shardMaster1Url;

    @Value("${spring.datasource.url.shard-replica0}")
    protected String shardReplica0Url;

    @Value("${spring.datasource.url.shard-replica1}")
    protected String shardReplica1Url;

    @Value("${spring.datasource.username.shard-master0}")
    protected String shardMaster0Username;

    @Value("${spring.datasource.username.shard-master1}")
    protected String shardMaster1Username;

    @Value("${spring.datasource.password.shard-master0}")
    protected String shardMaster0Password;

    @Value("${spring.datasource.password.shard-master1}")
    protected String shardMaster1Password;

    @Value("${spring.datasource.username.shard-replica0}")
    protected String shardReplica0Username;

    @Value("${spring.datasource.username.shard-replica1}")
    protected String shardReplica1Username;

    @Value("${spring.datasource.password.shard-replica0}")
    protected String shardReplica0Password;

    @Value("${spring.datasource.password.shard-replica1}")
    protected String shardReplica1Password;

    @Value("${spring.datasource.shard.table}")
    protected String shardTableName;

    @Value("${spring.datasource.shard.column}")
    protected String shardColumnName;

    @Value("${spring.datasource.shard.algorithm}")
    protected String shardAlgorithms;

    @Value("${spring.datasource.shard.type}")
    protected String shardType;


    @Bean
    public DataSource dataSource() throws SQLException {
        Map<String, DataSource> dataSourceMap = configureShardingDataSources();

        ShardingRuleConfiguration shardingRuleConfig = getShardingRuleConfiguration();

        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Collections.singleton(shardingRuleConfig), new Properties());
    }

    private Map<String, DataSource> configureShardingDataSources() {
        Map<String, DataSource> dataSourceMap = new HashMap<>();

        dataSourceMap.put("shard0", configureBaseDataSource(shardMaster0Url, shardMaster0Username, shardMaster0Password));
        dataSourceMap.put("shard1", configureBaseDataSource(shardMaster1Url, shardMaster1Username, shardMaster1Password));
        dataSourceMap.put("shard0_replica", configureBaseDataSource(shardReplica0Url, shardReplica0Username, shardReplica0Password));
        dataSourceMap.put("shard1_replica", configureBaseDataSource(shardReplica1Url, shardReplica1Username, shardReplica1Password));

        return dataSourceMap;
    }

    private ShardingRuleConfiguration getShardingRuleConfiguration() {
        ShardingTableRuleConfiguration tableRuleConfig = getTableRuleConfig();

        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(tableRuleConfig);

        Properties dbShardingProps = getDbShardingProps();
        shardingRuleConfig.getShardingAlgorithms().put("deviceid_hash_mod", new AlgorithmConfiguration(shardType, dbShardingProps));

        return shardingRuleConfig;
    }

    private ShardingTableRuleConfiguration getTableRuleConfig() {
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration(shardTableName, "shard${0..1}." + shardTableName);

        tableRuleConfig.setDatabaseShardingStrategy(
                new StandardShardingStrategyConfiguration(shardColumnName, "deviceid_hash_mod")
        );

        return tableRuleConfig;
    }

    private Properties getDbShardingProps() {
        Properties dbShardingProps = new Properties();

        dbShardingProps.setProperty("algorithm-expression", "shard${" + shardAlgorithms + "}");

        return dbShardingProps;
    }

}
