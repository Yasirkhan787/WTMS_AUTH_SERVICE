package com.yasirkhan.auth.producers;

import com.yasirkhan.auth.models.dtos.UserEventDto;
import com.yasirkhan.auth.models.dtos.UserResponseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserEventProducer {

    private final KafkaTemplate<String, Object> template;

    public UserEventProducer(KafkaTemplate<String, Object> template) {
        this.template = template;
    }

    public void userCreateEvent(UserEventDto event) {
        // 🚨 Attach .whenComplete() to handle the background thread's result
        template.send("user-created-topic", event).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("SUCCESS: User created event sent for ID: {} (Partition: {}, Offset: {})",
                        event.getUserId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("FAILED to send User Create event for ID: {}. Reason: {}",
                        event.getUserId(),
                        ex.getMessage());
            }
        });
    }

    public void userUpdateEvent(UserEventDto event) {
        template.send("user-updated-topic", event).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("SUCCESS: User Updated event sent for ID: {} (Partition: {}, Offset: {})",
                        event.getUserId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("FAILED to send User Update event for ID: {}. Reason: {}",
                        event.getUserId(),
                        ex.getMessage());
            }
        });
    }

    public void sendUserStatusUpdateEvent(UserResponseEvent event) {
        template.send("user-status-topic", event).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("SUCCESS: User Status event sent for ID: {} (Partition: {}, Offset: {})",
                        event.getUserId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("FAILED to send User Status event for ID: {}. Reason: {}",
                        event.getUserId(),
                        ex.getMessage());
            }
        });
    }
}