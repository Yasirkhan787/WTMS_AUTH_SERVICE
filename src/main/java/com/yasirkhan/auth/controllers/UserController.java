package com.yasirkhan.auth.controllers;

import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.requests.*;
import com.yasirkhan.auth.responses.UserResponse;
import com.yasirkhan.auth.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// CHANGED: added @Slf4j and log statements per endpoint for request tracing (see
// AuthController for the note on how these correlate to trace/span IDs automatically).
@Slf4j
@RestController
@RequestMapping("/auth/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Add new user (admin-created accounts)
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<UserResponse> addUser(@Valid @RequestBody UserRequest request) {
        log.info("Add-user request received for username={}", request.getUsername());
        UserResponse response = userService.addUser(request);
        log.info("User created successfully. userId={}, username={}", response.getId(), request.getUsername());
        return ResponseEntity.ok(response);
    }

    // Update user - the request body must include "userId".
    @PatchMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUser(@RequestBody Map<String, Object> updateRequest) {
        log.info("Update-user request received for userId={}", updateRequest.get("userId"));
        userService.updateUser(updateRequest);
        log.info("User updated successfully. userId={}", updateRequest.get("userId"));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Get-all-users request received");
        List<UserResponse> users = userService.getAllUser();
        log.info("Returning {} users", users.size());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        log.info("Get-user-by-id request received for userId={}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping("/block/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> blockUser(@PathVariable String id, @RequestParam Boolean blockStatus) {
        log.info("Block-user request received. userId={}, blockStatus={}", id, blockStatus);
        userService.blockUser(id, blockStatus);
        log.info("User block status updated. userId={}, blockStatus={}", id, blockStatus);
        // CHANGED: 204 No Content must not carry a response body per the HTTP spec - many
        // clients will simply drop it. Returning 200 OK here since a body is actually sent.
        return new ResponseEntity<>("User status updated successfully", HttpStatus.OK);
    }

    // Internal/testing endpoint for seeding super-admin accounts.
    @PostMapping("/add-user")
    public ResponseEntity<UserResponse> addUser(@Valid @RequestBody SuperAdminReq request) {
        log.info("Super-admin add-user request received for username={}", request.getUsername());
        UserResponse response = userService.addUser(request);
        log.info("Super-admin user created successfully. userId={}", response.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            log.warn("Change-password attempted without a valid authenticated session");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session missing or expired.");
        }

        User user = (User) authentication.getPrincipal();
        log.info("Change-password request received for userId={}", user.getId());

        userService.changePassword(user, request);

        log.info("Password changed successfully for userId={}", user.getId());
        return ResponseEntity.ok("Password updated successfully.");
    }

    @PostMapping("/forget-password")
    public ResponseEntity<Map<String, String>> forgetPassword(@Valid @RequestBody ForgetPasswordRequest request) {
        // CHANGED: never log the raw email/PII at info level in a way that could leak into
        // aggregated logs - masking isn't applied here, but keep this at a level your log
        // retention/PII policy actually allows. Adjust to log.debug if needed.
        log.info("Forget-password request received");
        userService.generateForgetPasswordToken(request);

        Map<String, String> response = Map.of(
                "message", "A 6-digit recovery code has been sent to your email. Expires in 10 minutes."
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("OTP verification request received");
        String secureToken = userService.verifyOtp(request.getOtp());

        if (secureToken == null) {
            log.warn("OTP verification failed - invalid or expired code");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid or expired OTP."));
        }

        log.info("OTP verified successfully");
        return ResponseEntity.ok(Map.of(
                "message", "OTP Verified.",
                "secureResetToken", secureToken
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Reset-password request received");
        userService.resetPassword(request);
        log.info("Password reset completed successfully");
        return ResponseEntity.ok("Password has been reset successfully. You can now log in with your new credentials.");
    }
}