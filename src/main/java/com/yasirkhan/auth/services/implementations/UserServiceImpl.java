package com.yasirkhan.auth.services.implementations;

import com.yasirkhan.auth.exceptions.DatabaseException;
import com.yasirkhan.auth.exceptions.UserAlreadyExistException;
import com.yasirkhan.auth.exceptions.UserNotFoundException;
import com.yasirkhan.auth.models.dtos.UserEventDto;
import com.yasirkhan.auth.models.dtos.UserResponseEvent;
import com.yasirkhan.auth.models.entity.RefreshToken;
import com.yasirkhan.auth.models.enums.EventStatus;
import com.yasirkhan.auth.models.enums.EventType;
import com.yasirkhan.auth.models.enums.Role;
import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.producers.UserEventProducer;
import com.yasirkhan.auth.repository.UserRepository;
import com.yasirkhan.auth.requests.SuperAdminReq;
import com.yasirkhan.auth.requests.UserRequest;
import com.yasirkhan.auth.responses.UserResponse;
import com.yasirkhan.auth.services.RefreshTokenService;
import com.yasirkhan.auth.services.UserService;
import com.yasirkhan.auth.utils.ResponseConversions;
import jakarta.transaction.Transactional;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserEventProducer userEventProducer;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final RedisTemplate<String, Object> redisTemplate; // Injected Redis

    public UserServiceImpl(UserRepository userRepository, UserEventProducer userEventProducer,
                           PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService,
                           RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.userEventProducer = userEventProducer;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public UserResponse addUser(UserRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistException("User with Username: " + request.getUsername() + " is already exist");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistException("User with Email: " + request.getEmail() + " is already exist");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setIsBlocked(true);

        User savedUser = userRepository.save(user);

        String redisKey = "wtms:auth:user:" + savedUser.getId();
        Map<String, Object> cacheData = new HashMap<>();
        cacheData.put("username", savedUser.getUsername());
        cacheData.put("email", savedUser.getEmail());
        cacheData.put("role", savedUser.getRole().name());
        cacheData.put("status", "PENDING");
        redisTemplate.opsForHash().putAll(redisKey, cacheData);

        UserEventDto event = UserEventDto.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .name(request.getName())
                .fatherName(request.getFatherName())
                .cnic(request.getCnic())
                .phoneNo(request.getPhoneNo())
                .address(request.getAddress())
                .gender(request.getGender())
                .dob(request.getDob())
                .licenseNo(request.getLicenseNo())
                .licenseExpiry(request.getLicenseExpiry())
                .status("PENDING")
                .build();

        if (request.getTehsilId() != null ){
            event.setTehsilId(UUID.fromString(request.getTehsilId()));
        }

        if (request.getYardId() != null ){
            event.setYardId(UUID.fromString(request.getYardId()));
        }

        userEventProducer.userCreateEvent(event);

        return ResponseConversions.toUserResponse(savedUser);
    }

    // Update User
    @Override
    @Transactional
    public void updateUser(Map<String, Object> updateRequest) {

        UUID userId = UUID.fromString(updateRequest.get("userId").toString());

        User dbUser =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "User Not Found with User ID: " + userId));

        /* TODO: Use MapConstruct */
        UserEventDto eventDto =
                UserEventDto
                        .builder()
                        .userId(userId)
                        .role(dbUser.getRole())
                        .build();

        updateRequest.forEach((key, value) ->
        {
            switch (key) {
                case "username" -> {
                    dbUser.setUsername((String) value);
                }
                case "role" -> {
                    dbUser.setRole(Role.valueOf(value.toString()));
                    eventDto.setRole(Role.valueOf(value.toString()));
                }
                case "email" -> {
                    dbUser.setEmail((String) value);
                    eventDto.setEmail((String) value);
                }
                case "name" -> eventDto.setName((String) value);
                case "fatherName" -> eventDto.setFatherName((String) value);
                case "cnic" -> eventDto.setCnic((String) value);
                case "gender" -> eventDto.setGender((String) value);
                case "phoneNo" -> eventDto.setPhoneNo((String) value);
                case "address" -> eventDto.setAddress((String) value);
                case "dob" -> {
                    DateTimeFormatter formatter =
                            DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    eventDto.setDob(LocalDate.parse((String) value, formatter));
                }
                case "tehsilId" -> eventDto.setTehsilId((UUID) value);
                case "licenseNo" -> eventDto.setLicenseNo((String) value);
                case "licenseExpiry" -> {
                    DateTimeFormatter formatter =
                            DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    eventDto.setLicenseExpiry(LocalDate.parse((String) value, formatter));
                }
                case "status" -> eventDto.setStatus((String) value);
            }
        });

        try {

            userRepository.save(dbUser);

            if (eventDto.getName() != null || eventDto.getFatherName() != null || eventDto.getCnic() != null
                    || eventDto.getPhoneNo() != null || eventDto.getAddress() != null || eventDto.getGender() != null
                    || eventDto.getDob() != null || eventDto.getTehsilId() != null || eventDto.getLicenseNo() != null || eventDto.getLicenseExpiry() != null) {

                userEventProducer.userUpdateEvent(eventDto);
            }
        } catch (Exception e) {
            throw new DatabaseException("Failed to save Admin Profile. Initiated Rollback. Error: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void blockUser(String id, Boolean blockStatus) {

        UUID userID = UUID.fromString(id);

        User dbUser = userRepository.findById(userID).orElseThrow(
                () -> new UserNotFoundException("User with ID: " + userID + " Not Found"));

        dbUser.setIsBlocked(blockStatus);

        if (blockStatus) {
            dbUser.setTokenVersion(dbUser.getTokenVersion() + 1);
        }

        User savedUser = userRepository.save(dbUser);

        String status = savedUser.getIsBlocked() ? "BLOCKED" : "ACTIVE";

        String redisKey = "wtms:auth:user:" + userID;
        redisTemplate.opsForHash().put(redisKey, "status", status);

        UserEventDto userData = UserEventDto.builder()
                .userId(userID)
                .role(savedUser.getRole())
                .status(status)
                .build();

        UserResponseEvent event = UserResponseEvent.builder()
                .eventTypeStatus(EventStatus.SUCCESS)
                .type(EventType.BLOCK)
                .userData(userData)
                .build();

        userEventProducer.sendUserStatusUpdateEvent(event);
    }

    @Override
    public List<UserResponse> getAllUser() {

        List<User> users =
                userRepository.findAll();

        if (users.isEmpty()) {
            throw new UserNotFoundException("No User Found in Database");
        }

        return users.stream()
                .map(ResponseConversions::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(String id) {

        UUID userID = UUID.fromString(id);
        User user =
                userRepository.findById(userID).orElseThrow(
                        () -> new UserNotFoundException("User with ID: " + id + " Not Found"));

        return ResponseConversions.toUserResponse(user);
    }


    @Override
    public User getUserByUsername(String username) {

        return
                userRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () -> new UserNotFoundException
                                        ("User with Username: " + username + " Not Found."));
    }

    @Override
    @Transactional
    public boolean logoutUser(User user) {

        Integer tokenVersion = user.getTokenVersion();
        user.setTokenVersion(tokenVersion + 1);

        RefreshToken refreshToken = user.getRefreshToken();

        if (refreshToken != null) {

            user.setRefreshToken(null);

            refreshTokenService.deleteRefreshToken(refreshToken.getToken());
        }

        userRepository.save(user);

        return true;
    }

    /// For Testing Purpose
    @Override
    public UserResponse addUser(SuperAdminReq request) {

        // Check if username is already exist
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistException("User with Username: " + request.getUsername() + " is already exist");
        }

        // Check if email is already exist
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistException("User with Email: " + request.getEmail() + " is already exist");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setIsBlocked(request.getIsBlocked());

        User savedUser = userRepository.save(user);

        return ResponseConversions.toUserResponse(savedUser);
    }
}
