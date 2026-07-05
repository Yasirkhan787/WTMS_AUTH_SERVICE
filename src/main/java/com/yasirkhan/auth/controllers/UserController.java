package com.yasirkhan.auth.controllers;

import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.requests.*;
import com.yasirkhan.auth.responses.UserResponse;
import com.yasirkhan.auth.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /// Add new User
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<UserResponse> addUser(@RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.addUser(request));
    }

    /*
        * Update User
        * Body: Must Add userId in Request Body
     */
    @PatchMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUser(@RequestBody Map<String, Object> updateRequest) {
        userService.updateUser(updateRequest);
        return ResponseEntity.noContent().build();
    }

    /// Get All Users
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUser());
    }

    /// Get User By ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /// Block User by ID
    @PatchMapping("/block/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> blockUser(@PathVariable String id, @RequestParam Boolean blockStatus) {
        userService.blockUser(id, blockStatus);
        return new ResponseEntity<>("User status updated successfully", HttpStatus.NO_CONTENT);
    }

    // TODO: Method to update password

    /*
        * NOTE: Testing
    */
    @PostMapping("/add-user")
    public ResponseEntity<?> addUser(@RequestBody SuperAdminReq request){
        return
                ResponseEntity.ok(userService.addUser(request));
    }

    // CHANGE PASSWORD (Authenticated Link)
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session missing or expired.");
        }

        User user = (User) authentication.getPrincipal();
        userService.changePassword(user, request);

        return ResponseEntity.ok("Password updated successfully.");
    }

    // REQUEST OTP
    @PostMapping("/forget-password")
    public ResponseEntity<Map<String, String>> forgetPassword(@Valid @RequestBody ForgetPasswordRequest request) {
        userService.generateForgetPasswordToken(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "A 6-digit recovery code has been sent to your email. Expires in 10 minutes.");
        return ResponseEntity.ok(response);
    }

    // VERIFY OTP & GET SECURE TOKEN
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        String secureToken = userService.verifyOtp(request.getOtp());

        if (secureToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid or expired OTP."));
        }

        return ResponseEntity.ok(Map.of(
                "message", "OTP Verified.",
                "secureResetToken", secureToken
        ));
    }

    // SUBMIT NEW PASSWORD
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok("Password has been reset successfully. You can now log in with your new credentials.");
    }
}
