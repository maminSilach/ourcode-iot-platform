package com.example.devicecollector.config;

import com.example.avro.DeviceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig extends AbstractConsumerConfig {

    @Value("${consumer.device.max-poll-records}")
    private final String deviceMaxPollRecords;

    @Value("${kafka.device.topic-dlq}")
    private final String deviceDlqTopicName;

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>> deviceEventListenerContainerFactory(
            KafkaTemplate<String, DeviceEvent> kafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        ConsumerFactory<String, Object> consumerFactory = consumerFactory(consumerConfigs());

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(
                configureDefaultErrorHandler(kafkaTemplate)
        );

        return factory;
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>> deviceEventBatchListenerContainerFactory(
            KafkaTemplate<String, DeviceEvent> kafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();

        Map<String, Object> batchConsumerProps = consumerConfigs();
        batchConsumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, deviceMaxPollRecords);
        ConsumerFactory<String, Object> batchConsumerFactory = consumerFactory(batchConsumerProps);

        factory.setConsumerFactory(batchConsumerFactory);
        factory.setBatchListener(true);
        factory.setCommonErrorHandler(
                configureDefaultErrorHandler(kafkaTemplate)
        );

        return factory;
    }

    @Override
    protected String getDlqTopicName() {
        return deviceDlqTopicName;
    }
}
