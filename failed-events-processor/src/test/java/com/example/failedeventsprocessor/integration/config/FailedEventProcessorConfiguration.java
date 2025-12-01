package com.example.failedeventsprocessor.integration.config;

import com.example.failedeventsprocessor.service.FileService;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Properties;

@SpringBootTest
@ActiveProfiles("test")
public abstract class FailedEventProcessorConfiguration extends TestcontainersConfiguration {

    protected static final String SCHEMA_REGISTRY_URL = "http://" + schemaRegistryContainer.getHost() + ":" + schemaRegistryContainer.getFirstMappedPort();

    @Autowired
    private S3Client s3Client;

    @MockitoSpyBean
    private FileService fileService;

    protected <T> KafkaProducer<String, T> getFailedEventKafkaProducer() {
        Properties producerProps = new Properties();

        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerProps.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);

        return new KafkaProducer<>(producerProps);
    }


    protected void createBucketIfNotExists(String bucketName) {
        try {
            s3Client.createBucket(builder -> builder.bucket(bucketName));
        } catch (S3Exception e) {
            if (!e.awsErrorDetails().errorCode().equals("BucketAlreadyExists")) {
                throw e;
            }
        }
    }
}
