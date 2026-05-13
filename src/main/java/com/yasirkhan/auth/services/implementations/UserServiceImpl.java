package com.yasirkhan.auth.services.implementations;

import com.yasirkhan.auth.exceptions.DatabaseException;
import com.yasirkhan.auth.exceptions.UserAlreadyExistException;
import com.yasirkhan.auth.exceptions.UserNotFoundException;
import com.yasirkhan.auth.models.dto.UserEventDto;
import com.yasirkhan.auth.models.dto.UserResponseEvent;
import com.yasirkhan.auth.models.entity.RefreshToken;
import com.yasirkhan.auth.models.entity.Role;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public UserServiceImpl(UserRepository userRepository, UserEventProducer userEventProducer, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.userEventProducer = userEventProducer;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    // Add User
    @Override
    @Transactional
    public UserResponse addUser(UserRequest request) {

        // Check if username is already exist
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistException("User with Username: " + request.getUsername() + " is already exist");
        }

        // Check if email is already exist
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistException("User with Email: " + request.getEmail() + " is already exist");
        }

        // Converting UserRequest to User Entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setIsBlocked(true);

        // Save to the DB
        User savedUser = userRepository.save(user);

        // Send Event to Kafka
        UserEventDto event =
                UserEventDto
                        .builder()
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
                        .age(request.getAge())
                        .licenseNo(request.getLicenseNo())
                        .licenseExpiry(request.getLicenseExpiry())
                        .status("PENDING")
                        .build();

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
                case "age" -> eventDto.setAge((int) value);
                case "licenseNo" -> eventDto.setLicenseNo((String) value);
                case "licenseExpiry" -> eventDto.setLicenseExpiry(LocalDate.parse((String) value));
                case "status" -> eventDto.setStatus((String) value);
            }
        });

        try {

            // Save to the DB
            userRepository.save(dbUser);

            if (eventDto.getName() != null || eventDto.getFatherName() != null || eventDto.getCnic() != null
                    || eventDto.getPhoneNo() != null || eventDto.getAddress() != null || eventDto.getGender() != null
                    || eventDto.getAge() != null || eventDto.getLicenseNo() != null || eventDto.getLicenseExpiry() != null) {

                userEventProducer.userUpdateEvent(eventDto);
            }
        } catch (Exception e) {
            throw new DatabaseException("Failed to save Admin Profile. Initiated Rollback. Error: " + e.getMessage());
        }
    }

    // Block User
    @Override
    @Transactional
    public void blockUser(UUID id, Boolean blockStatus) {

        User dbUser =
                userRepository.findById(id).orElseThrow(
                        () -> new UserNotFoundException(
                                "User with ID: " + id + " Not Found"));

        dbUser.setIsBlocked(blockStatus);

        User savedUser = userRepository.save(dbUser);

        String status = savedUser.getIsBlocked() ? "BLOCKED" : "ACTIVE";

        UserResponseEvent event =
                UserResponseEvent
                        .builder()
                        .userId(id)
                        .status(status)
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
    public boolean logoutUser(User user) {

        Integer tokenVersion = user.getTokenVersion();
        user.setTokenVersion(tokenVersion + 1);
        userRepository.save(user);

        RefreshToken refreshToken = user.getRefreshToken();
        refreshTokenService.deleteRefreshToken(refreshToken.getToken());

        return true;
    }

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
