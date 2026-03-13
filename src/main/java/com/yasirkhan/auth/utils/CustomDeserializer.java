package com.yasirkhan.auth.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yasirkhan.auth.models.dto.UserUpdateEventDto;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class CustomDeserializer implements Deserializer<Object> {

    private final ObjectMapper objectMapper;

    public CustomDeserializer() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    @Override
    public Object deserialize(String topic, byte[] data) {
        try {
            if (data == null) return null;

            return objectMapper.readValue(data, UserUpdateEventDto.class);
        } catch (Exception e) {
            throw new SerializationException("Error deserializing message", e);
        }
    }
}
