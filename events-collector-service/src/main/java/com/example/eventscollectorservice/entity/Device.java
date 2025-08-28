package com.example.eventscollectorservice.entity;

import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@Table("device_events_by_device")
public class Device {

    @PrimaryKeyColumn(name = "device_id", type = PrimaryKeyType.PARTITIONED)
    private String deviceId;

    @PrimaryKeyColumn(name = "event_id", type = PrimaryKeyType.CLUSTERED)
    private String eventId;

    @Column("timestamp")
    private Long timestamp;

    @Column("type")
    private String type;

    @Column("payload")
    private String payload;
}
