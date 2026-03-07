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
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<UserResponse> addUser(@RequestBody UserRequest addRequest){

        UserResponse response
                = userService.addUser(addRequest);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAll(){

        return
                ResponseEntity.ok(userService.getAllUser());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id){

        return
                ResponseEntity.ok(userService.getUserById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @RequestBody UserRequest updateRequest){

        return
                ResponseEntity.ok(userService.updateUser(id, updateRequest));
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<String> blockUser(@PathVariable UUID id){

        return new ResponseEntity<>("User with ID:" + id + "Blocked Successfully", HttpStatus.NO_CONTENT);
    }
}
