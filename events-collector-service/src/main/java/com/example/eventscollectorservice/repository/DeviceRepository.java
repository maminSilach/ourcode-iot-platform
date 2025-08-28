package com.example.eventscollectorservice.repository;

import com.example.eventscollectorservice.entity.Device;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.Optional;

public interface DeviceRepository extends CassandraRepository<Device, String> {

    Optional<Device> findByDeviceIdAndEventId(String deviceId, String eventId);

}
