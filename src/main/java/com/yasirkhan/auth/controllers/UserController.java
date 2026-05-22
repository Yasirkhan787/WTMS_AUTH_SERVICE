package com.yasirkhan.auth.controllers;

import com.yasirkhan.auth.requests.SuperAdminReq;
import com.yasirkhan.auth.requests.UserRequest;
import com.yasirkhan.auth.responses.UserResponse;
import com.yasirkhan.auth.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<UserResponse> addUser(@Valid @RequestBody UserRequest request) {
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
}
