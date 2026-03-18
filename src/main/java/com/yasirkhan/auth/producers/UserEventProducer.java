package com.yasirkhan.auth.producers;

import com.yasirkhan.auth.models.dto.UserStatusEventDto;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventProducer {

    private final KafkaTemplate<String, Object> template;

    public UserEventProducer(KafkaTemplate<String, Object> template) {
        this.template = template;
    }

    public void sendUserCreatedStatusEvent(UserStatusEventDto event) {
        try {
            template.send("user-response-topic", event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send status event", e);
        }
    }
    public void sendUserStatusUpdateEvent(UserStatusEventDto event) {
        try {
            template.send("user-status-topic", event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send status event", e);
        }
    }
}