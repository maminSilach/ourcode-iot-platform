package com.example.eventscollectorservice.integration.device;

import com.example.avro.DeviceEvent;
import com.example.eventscollectorservice.entity.Device;
import com.example.eventscollectorservice.integration.config.EventsCollectorServiceConfiguration;
import com.example.eventscollectorservice.repository.DeviceRepository;
import com.example.eventscollectorservice.service.DeviceCacheService;
import com.example.eventscollectorservice.utils.SchemaRegistryUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static com.example.eventscollectorservice.utils.DeviceData.DEVICE_IDS;
import static com.example.eventscollectorservice.utils.DeviceData.DEVICE_LAPTOP_EVENT;
import static com.example.eventscollectorservice.utils.DeviceData.DEVICE_PHONE_EVENT;
import static com.example.eventscollectorservice.utils.DeviceData.LAPTOP_DEVICE_ID;
import static com.example.eventscollectorservice.utils.DeviceData.LAPTOP_EVENT_ID;
import static com.example.eventscollectorservice.utils.DeviceData.PHONE_DEVICE_ID;
import static com.example.eventscollectorservice.utils.DeviceData.PHONE_EVENT_ID;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeviceTest extends EventsCollectorServiceConfiguration {

    private static final String EVENT_TOPIC = "events";
    private static final String AVRO_OBJECT_TYPE = EVENT_TOPIC + "-value";

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceCacheService deviceCacheService;

    @Value("${producer.deviceId.topic}")
    private String deviceIdTopic;

    @BeforeAll
    public static void beforeAll() {
        SchemaRegistryUtils.registerSchema(
                SCHEMA_REGISTRY_URL, AVRO_OBJECT_TYPE
        );
    }

    @Test
    void device_eventProcessing_singleEvent() {
        try (KafkaProducer<String, DeviceEvent> producer = getDeviceEventKafkaProducer()) {
            producer.send(new ProducerRecord<>(EVENT_TOPIC, DEVICE_PHONE_EVENT));

            await()
                    .untilAsserted(() -> {
                                Optional<Device> savedDevice = deviceRepository.findByDeviceIdAndEventId(PHONE_DEVICE_ID, PHONE_EVENT_ID);
                                assertTrue(assertionsDevice(savedDevice.orElseThrow(), DEVICE_PHONE_EVENT));
                            }
                    );
        }

        try (KafkaConsumer<String, String> consumer = getDeviceIdEventKafkaConsumer()) {

            consumer.subscribe(List.of(deviceIdTopic));

            await()
                    .until(() -> {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));

                        return StreamSupport.stream(
                                        records.spliterator(), false).allMatch(record -> PHONE_DEVICE_ID.equals(record.value())
                        );

                            }
                    );
        }

        deviceCacheService.invalidateDeviceIds();
    }

    @Test
    void device_eventProcessing_multipleEventsWithDuplicates() {
        try (KafkaProducer<String, DeviceEvent> producer = getDeviceEventKafkaProducer()) {
            ProducerRecord<String,DeviceEvent> producerRecordLaptop = new ProducerRecord<>(EVENT_TOPIC, DEVICE_LAPTOP_EVENT);
            ProducerRecord<String,DeviceEvent> producerRecordPhone = new ProducerRecord<>(EVENT_TOPIC, DEVICE_PHONE_EVENT);

            producer.send(producerRecordLaptop);
            producer.send(producerRecordPhone);
            producer.send(producerRecordLaptop);

            await()
                    .untilAsserted(() -> {
                                Optional<Device> savedPhone = deviceRepository.findByDeviceIdAndEventId(PHONE_DEVICE_ID, PHONE_EVENT_ID);
                                Optional<Device> savedLaptop = deviceRepository.findByDeviceIdAndEventId(LAPTOP_DEVICE_ID, LAPTOP_EVENT_ID);

                                boolean devicesIsPresent = savedPhone.isPresent() && savedLaptop.isPresent();
                                assertTrue(devicesIsPresent
                                        && assertionsDevice(savedPhone.orElseThrow(), DEVICE_PHONE_EVENT)
                                        && assertionsDevice(savedLaptop.orElseThrow(), DEVICE_LAPTOP_EVENT)
                                );
                            }
                    );
        }

        try (KafkaConsumer<String, String> consumer = getDeviceIdEventKafkaConsumer()) {

            consumer.subscribe(List.of(deviceIdTopic));

            await()
                    .until(() -> {
                                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));

                                return records.count() == 2
                                        && StreamSupport.stream(records.spliterator(), false)
                                        .map(ConsumerRecord::value)
                                        .toList()
                                        .containsAll(DEVICE_IDS);
                            }
                    );
        }

        deviceCacheService.invalidateDeviceIds();
    }

    private boolean assertionsDevice(Device source, DeviceEvent target) {
        return source.getDeviceId().equals(target.getDeviceId())
                && source.getEventId().equals(target.getEventId())
                && source.getTimestamp().equals(target.getTimestamp())
                && source.getType().equals(target.getType())
                && source.getPayload().equals(target.getPayload());
    }
}
