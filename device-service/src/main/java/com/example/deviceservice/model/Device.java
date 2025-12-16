package com.example.deviceservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "devices")
public class Device {

    @Id
    @Column(name = "device_id")
    private String id;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "meta")
    private String meta;

    @Column(name = "target_version")
    private String targetVersion;

    @Column(name = "idempotence_key")
    private String idempotenceKey;
}
