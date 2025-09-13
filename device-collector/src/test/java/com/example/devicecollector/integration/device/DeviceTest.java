package com.example.devicecollector.integration.device;

import com.example.avro.DeviceEvent;
import com.example.devicecollector.dto.DeviceResponse;
import com.example.devicecollector.entity.Device;
import com.example.devicecollector.integration.config.DeviceCollectorServiceConfiguration;
import com.example.devicecollector.service.DeviceService;
import com.example.devicecollector.utils.SchemaRegistryUtils;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;
import java.util.List;
import java.util.stream.StreamSupport;

import static com.example.devicecollector.utils.DeviceData.DEVICES;
import static com.example.devicecollector.utils.DeviceData.DEVICE_LAPTOP_EVENT;
import static com.example.devicecollector.utils.DeviceData.DEVICE_PHONE_EVENT;
import static com.example.devicecollector.utils.DeviceData.LAPTOP_DEVICE_ID;
import static com.example.devicecollector.utils.DeviceData.PHONE_DEVICE_ID;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DeviceTest extends DeviceCollectorServiceConfiguration {

    private static final String DEVICE_TOPIC = "device-id";
    private static final String DEVICE_DLQ_TOPIC = DEVICE_TOPIC + ".dlq";
    private static final String AVRO_OBJECT_TYPE = DEVICE_TOPIC + "-value";

    private static final String GET_DEVICES = """
            SELECT *
                FROM devices
           WHERE device_id = ?
           """;

    private static final String EXISTS_DEVICES = """
            SELECT EXISTS(
                SELECT 1
                FROM devices
                WHERE device_id = ?
            )
           """;

    private static final String COUNT_DEVICES = """
             SELECT count(*)
                FROM devices
             WHERE device_id = ?
           """;

    @Autowired
    private DeviceService deviceService;

    @BeforeAll
    public static void beforeAll() {
        SchemaRegistryUtils.registerSchema(
                SCHEMA_REGISTRY_URL, AVRO_OBJECT_TYPE
        );
    }

    @Test
    @Order(1)
    @Sql(value = "classpath:db/clear-devices.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void device_deviceProcessing_singleDeviceShardingToMaster0() {
        DeviceResponse deviceResponse = deviceService.createOrUpdateDeviceById(DEVICE_PHONE_EVENT);
        assertionsDeviceResponseByEvent(deviceResponse, DEVICE_PHONE_EVENT);

        int expectedShard = Math.abs(PHONE_DEVICE_ID.hashCode()) % 2;
        assertEquals(0, expectedShard);

        Device savedDeviceFromShard0 = shard0JdbcTemplate.queryForObject(GET_DEVICES, new Object[] {PHONE_DEVICE_ID}, BeanPropertyRowMapper.newInstance(Device.class));
        assertNotNull(savedDeviceFromShard0);
        assertionsDeviceByEvent(savedDeviceFromShard0, DEVICE_PHONE_EVENT);

        Boolean savedDeviceFromShard1 = shard1JdbcTemplate.queryForObject(EXISTS_DEVICES, new Object[] {PHONE_DEVICE_ID}, Boolean.class);
        assertFalse(savedDeviceFromShard1);

        DeviceResponse secondDeviceResponse = deviceService.createOrUpdateDeviceById(DEVICE_PHONE_EVENT);
        assertionsDeviceResponseByEvent(secondDeviceResponse, DEVICE_PHONE_EVENT);
        Integer savedDeviceCount = shard0JdbcTemplate.queryForObject(COUNT_DEVICES, new Object[] {PHONE_DEVICE_ID}, Integer.class);
        assertEquals(1, savedDeviceCount);
    }

    @Test
    @Order(2)
    @Sql(value = "classpath:db/clear-devices.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void device_deviceProcessing_singleDeviceShardingToMaster1() {
        DeviceResponse deviceResponse = deviceService.createOrUpdateDeviceById(DEVICE_LAPTOP_EVENT);
        assertionsDeviceResponseByEvent(deviceResponse, DEVICE_LAPTOP_EVENT);

        int expectedShard = Math.abs(LAPTOP_DEVICE_ID.hashCode()) % 2;
        assertEquals(1, expectedShard);

        Device savedDeviceFromShard0 = shard1JdbcTemplate.queryForObject(GET_DEVICES, new Object[] {LAPTOP_DEVICE_ID}, BeanPropertyRowMapper.newInstance(Device.class));
        assertNotNull(savedDeviceFromShard0);
        assertionsDeviceByEvent(savedDeviceFromShard0, DEVICE_LAPTOP_EVENT);

        Boolean savedDeviceFromShard1 = shard0JdbcTemplate.queryForObject(EXISTS_DEVICES, new Object[] {LAPTOP_DEVICE_ID}, Boolean.class);
        assertFalse(savedDeviceFromShard1);

        DeviceResponse secondDeviceResponse = deviceService.createOrUpdateDeviceById(DEVICE_PHONE_EVENT);
        assertionsDeviceResponseByEvent(secondDeviceResponse, DEVICE_LAPTOP_EVENT);
        Integer savedDeviceCount = shard1JdbcTemplate.queryForObject(COUNT_DEVICES, new Object[] {LAPTOP_DEVICE_ID}, Integer.class);
        assertEquals(1, savedDeviceCount);
    }

    @Test
    @Order(3)
    @Sql(value = "classpath:db/clear-devices.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void device_deviceProcessing_batchDeviceShardingToMaster() {
        int [][] batchResult = deviceService.createOrUpdateDevicesById(DEVICES);
        assertArrayEquals(new int[][] {{1,1}}, batchResult);

        int expectedPhoneShard = Math.abs(PHONE_DEVICE_ID.hashCode()) % 2;
        assertEquals(0, expectedPhoneShard);
        int expectedLaptopShard = Math.abs(LAPTOP_DEVICE_ID.hashCode()) % 2;
        assertEquals(1, expectedLaptopShard);

        Device savedPhoneDeviceFromShard0 = shard0JdbcTemplate.queryForObject(GET_DEVICES, new Object[] {PHONE_DEVICE_ID}, BeanPropertyRowMapper.newInstance(Device.class));
        assertNotNull(savedPhoneDeviceFromShard0);
        assertionsDeviceByEvent(savedPhoneDeviceFromShard0, DEVICE_PHONE_EVENT);

        Boolean existsLaptopDeviceShard0 = shard0JdbcTemplate.queryForObject(EXISTS_DEVICES, new Object[] {LAPTOP_DEVICE_ID}, Boolean.class);
        assertFalse(existsLaptopDeviceShard0);

        Device savedPhoneDeviceFromShard1 = shard1JdbcTemplate.queryForObject(GET_DEVICES, new Object[] {LAPTOP_DEVICE_ID}, BeanPropertyRowMapper.newInstance(Device.class));
        assertNotNull(savedPhoneDeviceFromShard1);
        assertionsDeviceByEvent(savedPhoneDeviceFromShard1, DEVICE_PHONE_EVENT);

        Boolean existsLaptopDeviceShard1 = shard1JdbcTemplate.queryForObject(EXISTS_DEVICES, new Object[] {PHONE_DEVICE_ID}, Boolean.class);
        assertFalse(existsLaptopDeviceShard1);

        int [][] secondBatchResult = deviceService.createOrUpdateDevicesById(DEVICES);
        assertArrayEquals(new int[][] {{1,1}}, secondBatchResult);

        Integer savedPhoneDeviceCount = shard0JdbcTemplate.queryForObject(COUNT_DEVICES, new Object[] {PHONE_DEVICE_ID}, Integer.class);
        assertEquals(1, savedPhoneDeviceCount);

        Integer savedLaptopDeviceCount = shard1JdbcTemplate.queryForObject(COUNT_DEVICES, new Object[] {LAPTOP_DEVICE_ID}, Integer.class);
        assertEquals(1, savedLaptopDeviceCount);
    }

    @Test
    @Order(4)
    void device_deviceProcessing_singleDeviceShardingToMasterFailed()  {
        int expectedShard = Math.abs(LAPTOP_DEVICE_ID.hashCode()) % 2;
        assertEquals(1, expectedShard);

        shard1Master.stop();

        try (KafkaProducer<String, DeviceEvent> producer = getDeviceEventKafkaProducer()) {
            producer.send(new ProducerRecord<>(DEVICE_TOPIC, DEVICE_LAPTOP_EVENT));
        }

        try (KafkaConsumer<String, DeviceEvent> consumer = getDeviceIdDlqEventKafkaConsumer()) {

            consumer.subscribe(List.of(DEVICE_DLQ_TOPIC));

            await()
                    .atMost(1, MINUTES)
                    .until(() -> {
                                ConsumerRecords<String, DeviceEvent> records = consumer.poll(Duration.ofSeconds(2));

                                return records.count() == 1
                                        && StreamSupport.stream(records.spliterator(), false)
                                        .allMatch(errorDevices -> errorDevices.value().equals(DEVICE_LAPTOP_EVENT));
                            }
                    );
        }
    }


    private boolean assertionsDeviceResponseByEvent(DeviceResponse deviceResponse, DeviceEvent deviceEvent) {
        return deviceResponse.deviceId().equals(deviceEvent.getDeviceId())
                && deviceResponse.deviceType().equals(deviceEvent.getDeviceType())
                && deviceResponse.createdAt().equals(deviceEvent.getCreatedAt())
                && deviceResponse.meta().equals(deviceEvent.getMeta());
    }


    private boolean assertionsDeviceByEvent(Device device, DeviceEvent deviceEvent) {
        return device.getDeviceId().equals(deviceEvent.getDeviceId())
                && device.getDeviceType().equals(deviceEvent.getDeviceType())
                && device.getCreatedAt().equals(deviceEvent.getCreatedAt())
                && device.getMeta().equals(deviceEvent.getMeta());
    }
}
