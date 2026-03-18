package com.yasirkhan.auth.controllers;

import com.yasirkhan.auth.requests.UserRequest;
import com.yasirkhan.auth.responses.UserResponse;
import com.yasirkhan.auth.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auth/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        return
                ResponseEntity.ok(userService.getAllUser());
    }

    @PutMapping("/block/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> blockUser(@PathVariable UUID id, @RequestParam Boolean blockStatus ){
        userService.blockUser(id, blockStatus);
        return new
                ResponseEntity<>("User with ID:" + id + "Blocked Successfully", HttpStatus.NO_CONTENT);
    }

    // Method to update password
}
