package com.example.devicecollector.config;

import com.example.avro.DeviceEvent;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.AvroRuntimeException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class AbstractConsumerConfig {

    @Value("${kafka.server}")
    protected String kafkaServer;

    @Value("${schema.registry.url}")
    protected String schemaRegistryUrl;

    @Value("${kafka.error.retry-interval}")
    protected int kafkaDefaultErrorRetryInterval;

    @Value("${kafka.error.attempts}")
    protected int kafkaDefaultErrorAttempts;


    protected abstract String getDlqTopicName();

    protected Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        return props;
    }

    protected ConsumerFactory<String, Object> consumerFactory(Map<String, Object> configs) {
        return new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(), new KafkaAvroDeserializer());
    }

    protected DefaultErrorHandler configureDefaultErrorHandler(KafkaTemplate<String, DeviceEvent> kafkaTemplate) {
        DefaultErrorHandler defaultErrorHandler =  new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (rec, ex) -> {
                            log.error("Message processing failed after all retry attempts with exception: ", ex);
                            return new TopicPartition(getDlqTopicName(), rec.partition());
                        }
                ),
                new FixedBackOff(kafkaDefaultErrorRetryInterval, kafkaDefaultErrorAttempts)
        );

        defaultErrorHandler.addNotRetryableExceptions(AvroRuntimeException.class);
        defaultErrorHandler.addRetryableExceptions(SQLException.class, IOException.class);

        return defaultErrorHandler;
    }
}
