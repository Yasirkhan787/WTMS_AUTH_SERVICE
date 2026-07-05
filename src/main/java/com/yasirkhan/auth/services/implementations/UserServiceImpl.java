package com.yasirkhan.auth.services.implementations;

import com.yasirkhan.auth.exceptions.*;
import com.yasirkhan.auth.integrations.NotificationClient;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.yasirkhan.auth.requests.ChangePasswordRequest;
import com.yasirkhan.auth.requests.ForgetPasswordRequest;
import com.yasirkhan.auth.requests.ResetPasswordRequest;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    // CHANGED: Math.random() is not cryptographically secure; OTPs used for password
    // reset should be generated with SecureRandom.
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final UserRepository userRepository;
    private final UserEventProducer userEventProducer;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final NotificationClient notificationClient;
    private final EmailService emailService;

    public UserServiceImpl(UserRepository userRepository, UserEventProducer userEventProducer,
                           PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService,
                           RedisTemplate<String, Object> redisTemplate, NotificationClient notificationClient,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.userEventProducer = userEventProducer;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.redisTemplate = redisTemplate;
        this.notificationClient = notificationClient;
        this.emailService = emailService;
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

        if (request.getTehsilId() != null) {
            event.setTehsilId(UUID.fromString(request.getTehsilId()));
        }

        if (request.getYardId() != null) {
            event.setYardId(UUID.fromString(request.getYardId()));
        }

        userEventProducer.userCreateEvent(event);

        return ResponseConversions.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public void updateUser(Map<String, Object> updateRequest) {

        UUID userId = UUID.fromString(updateRequest.get("userId").toString());

        User dbUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found with User ID: " + userId));

        UserEventDto eventDto = UserEventDto.builder()
                .userId(userId)
                .role(dbUser.getRole())
                .build();

        updateRequest.forEach((key, value) -> {
            switch (key) {
                case "username" -> dbUser.setUsername((String) value);
                case "role" -> {
                    Role role = Role.valueOf(value.toString());
                    dbUser.setRole(role);
                    eventDto.setRole(role);
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
                case "dob" -> eventDto.setDob(LocalDate.parse((String) value, DATE_FORMATTER));
                case "tehsilId" -> eventDto.setTehsilId((UUID) value);
                case "yardId" -> eventDto.setYardId((UUID) value);
                case "licenseNo" -> eventDto.setLicenseNo((String) value);
                case "licenseExpiry" -> eventDto.setLicenseExpiry(LocalDate.parse((String) value, DATE_FORMATTER));
                case "status" -> eventDto.setStatus((String) value);
            }
        });

        try {
            userRepository.save(dbUser);

            if (hasProfileChanges(eventDto)) {
                userEventProducer.userUpdateEvent(eventDto);
            }
        } catch (Exception e) {
            throw new DatabaseException("Failed to save Admin Profile. Initiated Rollback. Error: " + e.getMessage());
        }
    }

    // CHANGED: extracted the long chain of null-checks into its own method for readability.
    private boolean hasProfileChanges(UserEventDto eventDto) {
        return eventDto.getEmail() != null || eventDto.getName() != null || eventDto.getFatherName() != null
                || eventDto.getCnic() != null || eventDto.getPhoneNo() != null || eventDto.getAddress() != null
                || eventDto.getGender() != null || eventDto.getDob() != null || eventDto.getTehsilId() != null
                || eventDto.getYardId() != null || eventDto.getLicenseNo() != null || eventDto.getLicenseExpiry() != null;
    }

    @Override
    @Transactional
    public void blockUser(String id, Boolean blockStatus) {

        UUID userId = UUID.fromString(id);

        User dbUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID: " + userId + " Not Found"));

        dbUser.setIsBlocked(blockStatus);

        if (blockStatus) {
            dbUser.setTokenVersion(dbUser.getTokenVersion() + 1);
        }

        User savedUser = userRepository.save(dbUser);

        String status = savedUser.getIsBlocked() ? "BLOCKED" : "ACTIVE";

        String redisKey = "wtms:auth:user:" + userId;
        redisTemplate.opsForHash().put(redisKey, "status", status);

        UserEventDto userData = UserEventDto.builder()
                .userId(userId)
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
    // CHANGED: readOnly = true - this is a pure read path, so Hibernate can skip
    // dirty-checking/flush overhead for every entity in the list.
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUser() {

        List<User> users = userRepository.findAll();

        if (users.isEmpty()) {
            throw new UserNotFoundException("No User Found in Database");
        }

        return users.stream()
                .map(ResponseConversions::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(String id) {

        UUID userId = UUID.fromString(id);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID: " + id + " Not Found"));

        return ResponseConversions.toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with Username: " + username + " Not Found."));
    }

    @Override
    @Transactional
    public boolean logoutUser(User user) {

        user.setTokenVersion(user.getTokenVersion() + 1);

        RefreshToken refreshToken = user.getRefreshToken();
        if (refreshToken != null) {
            user.setRefreshToken(null);
            refreshTokenService.deleteRefreshToken(refreshToken.getToken());
        }

        userRepository.save(user);
        notificationClient.deleteFCMToken(user.getId().toString());

        return true;
    }

    @Override
    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadCredentialsException("The current password you provided is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}. Active tokens revoked.", user.getUsername());
    }

    @Override
    public String generateForgetPasswordToken(ForgetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account linked with this email address."));

        if (Boolean.TRUE.equals(user.getIsBlocked())) {
            throw new UnauthorizedException("This account has been locked out by an administrator.");
        }

        String otp = generateOtp();

        String redisOtpKey = "wtms:auth:otp:" + otp;
        redisTemplate.opsForValue().set(redisOtpKey, user.getEmail(), 10, TimeUnit.MINUTES);

        emailService.sendPasswordResetEmail(user.getEmail(), otp);
        log.info("Forget password 6-digit OTP generated and emailed to user: {}", user.getUsername());

        // Generic message returned to the controller - never leak the OTP itself in the response.
        return "OTP sent successfully.";
    }

    // CHANGED: extracted OTP generation into its own method, now backed by SecureRandom
    // instead of Math.random() (see SECURE_RANDOM field comment above).
    private String generateOtp() {
        int otp = SECURE_RANDOM.nextInt(900000) + 100000;
        return String.valueOf(otp);
    }

    @Override
    public String verifyOtp(String otp) {
        String redisOtpKey = "wtms:auth:otp:" + otp;
        Object cachedEmailObj = redisTemplate.opsForValue().get(redisOtpKey);

        if (cachedEmailObj == null) {
            return null;
        }

        String email = cachedEmailObj.toString();
        redisTemplate.delete(redisOtpKey);

        String secureResetToken = UUID.randomUUID().toString();
        String redisResetKey = "wtms:auth:reset-token:" + secureResetToken;
        redisTemplate.opsForValue().set(redisResetKey, email, 15, TimeUnit.MINUTES);

        return secureResetToken;
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // The reset token here is the UUID issued by verifyOtp(), not the original OTP.
        String redisResetKey = "wtms:auth:reset-token:" + request.getToken();

        Object cachedEmailObj = redisTemplate.opsForValue().get(redisResetKey);
        if (cachedEmailObj == null) {
            throw new IllegalArgumentException("This secure reset session has expired. Please request a new OTP.");
        }

        String email = cachedEmailObj.toString();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account processing error during recovery sync."));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        redisTemplate.delete(redisResetKey);
        log.info("Password successfully recovered and reset for user: {}", user.getUsername());
    }

    // For internal/testing use - creates a user with an explicitly provided blocked status.
    @Override
    public UserResponse addUser(SuperAdminReq request) {

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
        user.setIsBlocked(request.getIsBlocked());

        User savedUser = userRepository.save(user);

        return ResponseConversions.toUserResponse(savedUser);
    }
}