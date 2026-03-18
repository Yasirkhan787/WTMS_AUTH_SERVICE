package com.yasirkhan.auth.consumer;

import com.yasirkhan.auth.models.dto.UserEventDto;
import com.yasirkhan.auth.requests.UserRequest;
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
            topics = "user-created-topic",
            groupId = "auth-group",
            containerFactory = "listenerContainerFactory"
    )
    public void consumeUserCreationEvent(UserEventDto createUserEventDto){
        userService.addUser(createUserEventDto);
    }

    @KafkaListener(
            topics = "user-updated-topic",
            groupId = "auth-group",
            containerFactory = "listenerContainerFactory"
    )
    public void consumeUserUpdationEvent(UserEventDto updateUserEventDto){
        userService.updateUser(updateUserEventDto);
    }
}
