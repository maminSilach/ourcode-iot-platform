package com.example.failedeventsprocessor.integration.event;

import com.example.events.FailedEventRecord;
import com.example.failedeventsprocessor.integration.config.FailedEventProcessorConfiguration;
import com.example.failedeventsprocessor.service.FileService;
import com.example.failedeventsprocessor.utils.SchemaRegistryUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.List;

import static com.example.failedeventsprocessor.utils.FailedEventData.FAILED_EVENT_REASON;
import static com.example.failedeventsprocessor.utils.FailedEventData.FAILED_EVENT_RECORD;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FailedEventTest extends FailedEventProcessorConfiguration {

    private static final String FAILED_EVENT_TOPIC = "failed-events";
    private static final String AVRO_OBJECT_TYPE = FAILED_EVENT_TOPIC + "-value";

    @Autowired
    private FileService fileService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${event.failed-bucket-name}")
    private String failedBucketName;

    @BeforeAll
    public static void beforeAll() {
        SchemaRegistryUtils.registerSchema(
                SCHEMA_REGISTRY_URL, AVRO_OBJECT_TYPE
        );
    }

    @Test
    @Order(1)
    void failedEvent_processFailed_successful() {
        createBucketIfNotExists(failedBucketName);

        try (KafkaProducer<String, FailedEventRecord> producer = getFailedEventKafkaProducer()) {
            producer.send(new ProducerRecord<>(FAILED_EVENT_TOPIC, FAILED_EVENT_RECORD));

            await()
                    .untilAsserted(() -> {
                                List<S3Object> s3Objects = fileService.loadFilesByPrefix(FAILED_EVENT_REASON, failedBucketName).contents();
                                assertEquals(1, s3Objects.size());

                                String key = s3Objects.getFirst().key();
                                byte[] errorContent = fileService.loadFile(key, failedBucketName);
                                FailedEventRecord failedEventRecord = objectMapper.readValue(errorContent, FailedEventRecord.class);

                                assertNotNull(failedEventRecord);
                            }
                    );
        }
    }

    @Test
    @Order(2)
    void failedEvent_processFailed_failed() {
        minIOContainer.stop();

        ArgumentCaptor<String> resourceArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bucketNameArgumentCaptor = ArgumentCaptor.forClass(String.class);

        try (KafkaProducer<String, FailedEventRecord> producer = getFailedEventKafkaProducer()) {
            producer.send(new ProducerRecord<>(FAILED_EVENT_TOPIC, FAILED_EVENT_RECORD));

            await()
                    .untilAsserted(() -> {
                                verify(fileService, times(3)).uploadJsonResourceWithRetry(
                                        resourceArgumentCaptor.capture(), keyArgumentCaptor.capture(), bucketNameArgumentCaptor.capture()
                                );

                                String [] splitKey = keyArgumentCaptor.getValue().split("_");

                                assertEquals(FAILED_EVENT_REASON, splitKey[0]);
                                assertEquals(resourceArgumentCaptor.getValue(), FAILED_EVENT_RECORD.toString());
                                assertEquals(bucketNameArgumentCaptor.getValue(), failedBucketName);
                            }
                    );
        }
    }
}
