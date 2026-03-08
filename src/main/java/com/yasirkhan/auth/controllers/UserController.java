package com.yasirkhan.auth.controllers;

import com.yasirkhan.auth.requests.UserRequest;
import com.yasirkhan.auth.responses.UserResponse;
import com.yasirkhan.auth.services.DriverService;
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
    private final DriverService driverService;

    public UserController(UserService userService, DriverService driverService){
        this.userService = userService;
        this.driverService = driverService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAll(){

        return
                ResponseEntity.ok(userService.getAllUser());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id){

        return
                ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/update/{id}")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @RequestBody UserRequest updateRequest){

        return
                ResponseEntity.ok(userService.updateUser(id, updateRequest));
    }

    @PutMapping("/block/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> blockUser(@PathVariable UUID id, @RequestParam Boolean blockStatus ){

        driverService.toggleDriverStatus(id, blockStatus);

        return new ResponseEntity<>("User with ID:" + id + "Blocked Successfully", HttpStatus.NO_CONTENT);
    }

    @PostMapping("/add")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<UserResponse> addUser(@RequestBody UserRequest addRequest){

        return new ResponseEntity<>(userService.addUser(addRequest), HttpStatus.CREATED);
    }
}
