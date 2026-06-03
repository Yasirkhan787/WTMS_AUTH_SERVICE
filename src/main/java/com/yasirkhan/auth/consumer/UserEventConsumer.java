package com.yasirkhan.auth.consumer;

import com.yasirkhan.auth.models.dtos.UserResponseEvent;
import com.yasirkhan.auth.models.enums.EventStatus;
import com.yasirkhan.auth.models.enums.EventType;
import com.yasirkhan.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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

        if (EventStatus.FAILURE.equals(event.getEventTypeStatus()) && EventType.CREATE.equals(event.getType())) {
            if (userRepository.existsById(userId)) {
                userRepository.deleteById(userId);
                log.info("Saga Rollback Success: Deleted profile and associated actor for ID: {}", userId);
            }
        } else if (EventStatus.SUCCESS.equals(event.getEventTypeStatus()) && EventType.CREATE.equals(event.getType())){

            // SAGA SUCCESS: Activate the pending user
            userRepository.findById(userId).ifPresent(user -> {
                user.setIsBlocked(false); // Unblock!
                userRepository.save(user);

                // Update Auth Redis Cache safely
                String redisKey = "wtms:auth:user:" + userId;
                redisTemplate.opsForHash().put(redisKey, "status", "ACTIVE");
                log.info("Saga Completed: User {} successfully activated.", userId);
            });
        } else  {
            log.info("Saga Rollback Failure: User not found for ID: {}", userId);
        }
    }
}
