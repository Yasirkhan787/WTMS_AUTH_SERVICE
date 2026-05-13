package com.yasirkhan.auth.producers;

import com.yasirkhan.auth.models.dto.UserEventDto;
import com.yasirkhan.auth.models.dto.UserResponseEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventProducer {

    private final KafkaTemplate<String, Object> template;

    public UserEventProducer(KafkaTemplate<String, Object> template) {
        this.template = template;
    }

    public void userCreateEvent(UserEventDto event) {
        try {
            template.send("user-created-topic", event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send status event", e);
        }
    }

    public void userUpdateEvent(UserEventDto event) {
        try {
            template.send("user-updated-topic", event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send status event", e);
        }
    }

    public void sendUserStatusUpdateEvent(UserResponseEvent event) {
        try {
            template.send("user-status-topic", event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send status event", e);
        }
    }


}