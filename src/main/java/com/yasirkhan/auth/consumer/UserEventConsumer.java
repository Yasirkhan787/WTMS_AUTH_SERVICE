package com.yasirkhan.auth.consumer;

import com.yasirkhan.auth.models.dtos.UserResponseEvent;
import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.repository.UserRepository;
import com.yasirkhan.auth.services.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class UserEventConsumer {

    private final UserService userService;

    private final UserRepository userRepository;

    public UserEventConsumer(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @KafkaListener(
            topics = "user-response-topic",
            groupId = "auth-group",
            containerFactory = "listenerContainerFactory"
    )
    @Transactional
    public void handleUserResponse(UserResponseEvent event) {
        UUID userId = event.getUserId();

        if ("FAILURE".equals(event.getStatus())) {
            if (userRepository.existsById(userId)) {
                userRepository.deleteById(userId);
                log.info("Saga Rollback Success: Deleted profile and associated actor for ID: {}", userId);
            }
        } else {
            // Success : Activate the account
            User user = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Profile not found for ID: " + userId));

            user.setIsBlocked(false);
            userRepository.save(user);
            log.info("Saga Transaction Finalized: User ID {} is now ACTIVE", userId);
        }
    }
}
