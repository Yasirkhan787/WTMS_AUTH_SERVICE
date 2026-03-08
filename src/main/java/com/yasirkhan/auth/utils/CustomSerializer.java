package com.yasirkhan.auth.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class CustomSerializer implements Serializer<Object> {

    private final ObjectMapper objectMapper;

    public CustomSerializer() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    @Override
    public byte[] serialize(String topic, Object data) {
        try {
            if (data == null){
                throw new RuntimeException("Error serializing Driver Event");
            }
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new SerializationException("Error serializing Driver Event", e);
        }
    }
}
