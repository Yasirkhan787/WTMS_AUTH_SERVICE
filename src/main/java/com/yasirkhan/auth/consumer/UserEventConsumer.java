package com.yasirkhan.auth.consumer;

import com.yasirkhan.auth.models.dto.UserUpdateEventDto;
import com.yasirkhan.auth.services.UserService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventConsumer {

    private final UserService userService;

    public UserEventConsumer(UserService userService) {
        this.userService = userService;
    }

    @KafkaListener(
            topics = "user-updation-topic",
            groupId = "auth-group",
            containerFactory = "listenerContainerFactory"
    )
    public void consumeUserUpdationEvent(UserUpdateEventDto updateEventDto){
        userService.updateUser(updateEventDto);
    }
}
