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

    // CHANGED: extracted topic names into constants instead of magic strings scattered
    // across the @Bean methods, and renamed the injected field to camelCase.
    private static final String USER_STATUS_TOPIC = "user-status-topic";
    private static final String USER_CREATED_TOPIC = "user-created-topic";
    private static final String USER_UPDATED_TOPIC = "user-updated-topic";
    private static final String USER_RESPONSE_DLT = "user-response-topic-dlt";
    private static final int PARTITIONS = 2;
    private static final short REPLICATION_FACTOR = 1;

    private final String bootstrapServer;

    public KafkaProducerConfig(@Value("${kafka.bootstrap.server}") String bootstrapServer) {
        this.bootstrapServer = bootstrapServer;
    }

    @Bean
    public NewTopic createUserStatusTopic() {
        return new NewTopic(USER_STATUS_TOPIC, PARTITIONS, REPLICATION_FACTOR);
    }

    @Bean
    public NewTopic createUserCreatedTopic() {
        return new NewTopic(USER_CREATED_TOPIC, PARTITIONS, REPLICATION_FACTOR);
    }

    @Bean
    public NewTopic createUserUpdatedTopic() {
        return new NewTopic(USER_UPDATED_TOPIC, PARTITIONS, REPLICATION_FACTOR);
    }

    @Bean
    public NewTopic userResponseDLT() {
        return new NewTopic(USER_RESPONSE_DLT, PARTITIONS, REPLICATION_FACTOR);
    }

    @Bean
    public Map<String, Object> producerConfig() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
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