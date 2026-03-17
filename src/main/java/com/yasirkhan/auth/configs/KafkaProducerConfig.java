package com.yasirkhan.auth.configs;

import com.yasirkhan.auth.utils.CustomSerializer;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    private final String BOOTSTRAP_SERVER;

    public KafkaProducerConfig(@Value("${kafka.bootstrap.server}") String bootstrapServer) {
        BOOTSTRAP_SERVER = bootstrapServer;
    }

    @Bean
    public NewTopic createUserStatusTopic(){
        return new NewTopic("user-status-topic", 2, (short) 1);
    }

    @Bean
    public NewTopic createUserResponseStatusTopic(){
        return new NewTopic("user-response-topic", 2, (short) 1);
    }

    @Bean
    public Map<String, Object> producerConfig(){

        Map<String, Object> properties
                = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CustomSerializer.class);

        return properties;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(){
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(){
        return new KafkaTemplate<>(producerFactory());
    }
}
