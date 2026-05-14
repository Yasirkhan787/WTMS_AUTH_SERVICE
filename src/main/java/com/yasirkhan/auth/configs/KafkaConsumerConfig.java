package com.yasirkhan.auth.configs;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer; // ✅ Modern Spring Deserializer
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    private final String BOOTSTRAP_SERVER;
    private final String CONSUMER_GROUP;

    public KafkaConsumerConfig(@Value("${kafka.bootstrap.server}") String bootstrapServer,
                               @Value("${kafka.consumer.group}") String consumerGroup) {
        BOOTSTRAP_SERVER = bootstrapServer;
        CONSUMER_GROUP = consumerGroup;
    }

    // --- Consumer Configuration ---
    @Bean
    public Map<String, Object> consumerConfig() {
        Map<String, Object> properties = new HashMap<>();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP);

        // Using .getName() avoids IDE generic type warnings
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class.getName());

        // 🚨 Production Standard: Trust all internal microservice packages
        properties.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "*");

        return properties;
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfig());
    }

    // --- Resiliency & Error Handling ---
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> listenerContainerFactory(
            DefaultErrorHandler errorHandler) { // 👈 Inject the safety net

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());
        factory.setCommonErrorHandler(errorHandler); // Attach the safety net

        return factory;
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaOperations<Object, Object> template) {
        /*
         * Self-Healing Logic:
         * 1. Retry processing 3 times, waiting 2 seconds between each attempt.
         * 2. If it still fails, use the KafkaTemplate to push the failed message
         * to 'original-topic.DLT' so Eco Savvy doesn't lose the data.
         */
        return new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(template),
                new FixedBackOff(2000L, 3)
        );
    }
}