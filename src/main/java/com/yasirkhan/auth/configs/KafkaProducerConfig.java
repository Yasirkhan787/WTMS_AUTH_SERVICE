package com.yasirkhan.auth.configs;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    private final String BOOTSTRAP_SERVER;

    public KafkaProducerConfig(@Value("${kafka.bootstrap.server}") String bootstrapServer) {
        BOOTSTRAP_SERVER = bootstrapServer;
    }

    // --- Outbound Topics ---
    @Bean
    public NewTopic createUserStatusTopic() {
        return new NewTopic("user-status-topic", 2, (short) 1);
    }

    @Bean
    public NewTopic createUserCreatedTopic() {
        return new NewTopic("user-created-topic", 2, (short) 1);
    }

    @Bean
    public NewTopic createUserUpdatedTopic() {
        return new NewTopic("user-updated-topic", 2, (short) 1);
    }

    // Topic to Store DeadLetter Messages
    @Bean
    public NewTopic userStatusDLT() {
        return new NewTopic("user-status-topic.DLT", 2, (short) 1);
    }
    // --- Producer Configuration ---
    @Bean
    public Map<String, Object> producerConfig() {
        Map<String, Object> properties = new HashMap<>();

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);

        // Using .getName() avoids IDE generic type warnings
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class.getName());

        // 🚨 Production Standard: Tells Kafka to embed the DTO class name in the headers
        // so the Notification Service knows exactly what object it is receiving.
        properties.put(JacksonJsonSerializer.ADD_TYPE_INFO_HEADERS, true);

        return properties;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}