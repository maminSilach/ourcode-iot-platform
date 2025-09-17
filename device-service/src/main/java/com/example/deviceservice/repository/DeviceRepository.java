package com.example.deviceservice.repository;

import com.example.deviceservice.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> getDeviceById(String id);

    boolean existsDeviceById(String id);

    void deleteDeviceById(String id);
}
