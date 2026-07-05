package com.yasirkhan.auth.consumer;

import com.yasirkhan.auth.models.dtos.UserResponseEvent;
import com.yasirkhan.auth.models.enums.EventStatus;
import com.yasirkhan.auth.models.enums.EventType;
import com.yasirkhan.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Slf4j
public class UserEventConsumer {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public UserEventConsumer(UserRepository userRepository, RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(
            topics = "user-response-topic",
            groupId = "auth-group",
            containerFactory = "listenerContainerFactory"
    )
    @Transactional
    public void handleUserResponse(UserResponseEvent event) {
        UUID userId = event.getUserData().getUserId();
        EventType eventType = event.getType();
        EventStatus status = event.getEventTypeStatus();

        log.info("Received user-response event. userId={}, type={}, status={}", userId, eventType, status);

        if (EventStatus.SUCCESS.equals(status)) {
            handleSuccessScenarios(userId, eventType);
        } else if (EventStatus.FAILURE.equals(status)) {
            handleFailureScenarios(userId, eventType);
        } else {
            log.warn("Received unknown event status for User ID: {}", userId);
        }
    }

    private void handleSuccessScenarios(UUID userId, EventType eventType) {
        switch (eventType) {
            case CREATE -> // SAGA SUCCESS: activate the pending user
                    userRepository.findById(userId).ifPresentOrElse(user -> {
                        user.setIsBlocked(false);
                        userRepository.save(user);

                        String redisKey = "wtms:auth:user:" + userId;
                        redisTemplate.opsForHash().put(redisKey, "status", "ACTIVE");
                        log.info("Saga Completed: User {} successfully activated.", userId);
                    }, () -> log.error("Saga Success Error: User not found in Auth DB for ID: {}", userId));

            case UPDATE ->
                    log.info("Saga Completed: User {} successfully updated in downstream services.", userId);

            case BLOCK, DELETE ->
                    log.info("Saga Completed: User {} status change synced successfully.", userId);

            default -> log.warn("Unhandled SUCCESS event type: {}", eventType);
        }
    }

    private void handleFailureScenarios(UUID userId, EventType eventType) {
        switch (eventType) {
            case CREATE -> {
                if (userRepository.existsById(userId)) {
                    userRepository.deleteById(userId);
                    log.info("Saga Rollback Success: Deleted profile and associated actor for ID: {}", userId);
                } else {
                    log.warn("Saga Rollback Warning: User already deleted or not found for ID: {}", userId);
                }
            }
            // TODO: implement logic to revert the Auth DB to its previous state on UPDATE failure
            case UPDATE -> log.error(
                    "Saga Rollback Required: Update failed in User Service for ID: {}. Revert local Auth DB state if necessary.",
                    userId);

            default -> log.error("Unhandled FAILURE event type: {} for User ID: {}", eventType, userId);
        }
    }
}