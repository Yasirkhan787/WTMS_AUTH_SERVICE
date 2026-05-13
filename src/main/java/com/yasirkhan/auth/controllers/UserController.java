package com.yasirkhan.auth.controllers;

import com.yasirkhan.auth.models.dto.UserEventDto;
import com.yasirkhan.auth.requests.SuperAdminReq;
import com.yasirkhan.auth.requests.UserRequest;
import com.yasirkhan.auth.responses.UserResponse;
import com.yasirkhan.auth.services.UserService;
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

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<UserResponse> addUser(@RequestBody UserRequest request) {

        return
                ResponseEntity.ok(userService.addUser(request));
    }

    /*
     * Body: Must Add userId and role in Request Body
     */
    @PatchMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUser(
            @RequestBody Map<String, Object> updateRequest) {

        userService.updateUser(updateRequest);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return
                ResponseEntity.ok(userService.getAllUser());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return
                ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/block/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> blockUser(@PathVariable UUID id, @RequestParam Boolean blockStatus) {
        userService.blockUser(id, blockStatus);
        return new
                ResponseEntity<>("User with ID:" + id + "Blocked Successfully", HttpStatus.NO_CONTENT);
    }
    // Method to update password

    // Testing
    @PostMapping("/add-user")
    public ResponseEntity<?> addUser(@RequestBody SuperAdminReq request){
        return
                ResponseEntity.ok(userService.addUser(request));
    }
}
