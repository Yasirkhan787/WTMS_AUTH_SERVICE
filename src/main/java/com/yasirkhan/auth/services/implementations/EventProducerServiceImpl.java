package com.yasirkhan.auth.services.implementations;

import com.yasirkhan.auth.models.dto.DriverDto;
import com.yasirkhan.auth.models.dto.DriverStatusChangedEventDto;
import com.yasirkhan.auth.services.EventProducerService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class EventProducerServiceImpl implements EventProducerService {

    private final KafkaTemplate<String, Object> template;

    public EventProducerServiceImpl(KafkaTemplate<String, Object> template) {
        this.template = template;
    }

    @Override
    public void sendDriverCreationEvent(DriverDto event) {
        try {
            template.send("user-creation-driver-topic", event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send creation event", e);
        }
    }

    @Override
    public void sendDriverStatusEvent(DriverStatusChangedEventDto event) {
        try {
            template.send("user-status-driver-topic", event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send status event", e);
        }
    }
}