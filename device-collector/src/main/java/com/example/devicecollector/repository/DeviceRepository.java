package com.example.devicecollector.repository;

import com.example.devicecollector.entity.Device;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DeviceRepository {

    private static final String IDEMPOTENCE_SAVE_DEVICE = """
               INSERT INTO devices (device_id, device_type, created_at, meta)
                    VALUES (?, ?, ?, ?)
                    ON CONFLICT (device_id)
                    DO UPDATE SET
                        device_type = EXCLUDED.device_type,
                        created_at = EXCLUDED.created_at,
                        meta = EXCLUDED.meta
            """;

    private static final String IDEMPOTENCE_SAVE_DEVICE_WITH_RETURNING = IDEMPOTENCE_SAVE_DEVICE + " RETURNING *";

    private final JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.device.batch-size}")
    private final int batchSize;


    public Device saveOrUpdateDeviceById(Device device) {
        return jdbcTemplate.queryForObject(IDEMPOTENCE_SAVE_DEVICE_WITH_RETURNING,
                new Object[] {device.getDeviceId(), device.getDeviceType(), device.getCreatedAt(), device.getMeta()},
                BeanPropertyRowMapper.newInstance(Device.class)
        );
    }

    public int[][] saveOrUpdateDevicesById(List<Device> devices) {
        return jdbcTemplate.batchUpdate(IDEMPOTENCE_SAVE_DEVICE, devices, batchSize, (ps, device) -> {
                    ps.setString(1, device.getDeviceId());
                    ps.setString(2, device.getDeviceType());
                    ps.setLong(3, device.getCreatedAt());
                    ps.setString(4, device.getMeta());
                }
        );
    }
}
