package com.yasirkhan.auth.services.implementations;

import com.yasirkhan.auth.exceptions.UserAlreadyExistException;
import com.yasirkhan.auth.exceptions.UserNotFoundException;
import com.yasirkhan.auth.models.dto.UserEventDto;
import com.yasirkhan.auth.models.dto.UserStatusEventDto;
import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.producers.UserEventProducer;
import com.yasirkhan.auth.repository.UserRepository;
import com.yasirkhan.auth.responses.UserResponse;
import com.yasirkhan.auth.services.UserService;
import com.yasirkhan.auth.utils.ResponseConversions;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;

    private final UserEventProducer userEventProducer;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserEventProducer userEventProducer, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.userEventProducer = userEventProducer;
        this.passwordEncoder = passwordEncoder;
    }

    // Add User
    @Override
    @Transactional
    public UserResponse addUser(UserEventDto userCreateEventDto) {

        // Check if username is already exist
        if(userRepository.existsByUsername(userCreateEventDto.getUsername())){
            throw new UserAlreadyExistException("User with Username: " + userCreateEventDto.getUsername() + " is already exist");
        }

        // Check if email is already exist
        if(userRepository.existsByEmail(userCreateEventDto.getEmail())){
            throw new UserAlreadyExistException("User with Email: " + userCreateEventDto.getEmail() + " is already exist");
        }

        // Converting UserRequest to User Entity
        User user =  new User();
        user.setId(userCreateEventDto.getUserId());
        user.setUsername(userCreateEventDto.getUsername());
        user.setEmail(userCreateEventDto.getEmail());
        user.setPassword(passwordEncoder.encode(userCreateEventDto.getPassword()));
        user.setRole(userCreateEventDto.getRole());
        user.setIsBlocked(userCreateEventDto.getIsBlocked());

        // Save to the DB
        User savedUser = userRepository.save(user);

        try {
            UserStatusEventDto event =
                    UserStatusEventDto
                            .builder()
                            .id(savedUser.getId())
                            .userStatus("SUCCESS")
                            .build();
        } catch (Exception e) {

            UserStatusEventDto event =
                    UserStatusEventDto
                            .builder()
                            .id(savedUser.getId())
                            .userStatus("FAILURE")
                            .build();
        }


        return ResponseConversions.toUserResponse(savedUser);
    }

    // Update User
    @Override
    @Transactional
    public void updateUser(UserEventDto updateEventDto) {

        User dbUser =
                userRepository.findById(updateEventDto.getUserId()).orElseThrow(
                        () -> new UserNotFoundException("User with ID: " + updateEventDto.getUserId() + " Not Found"));

        dbUser.setUsername(updateEventDto.getUsername());
        dbUser.setEmail(updateEventDto.getEmail());
        dbUser.setRole(updateEventDto.getRole());

        // Save to the DB
        User updatedUser =
                userRepository.save(dbUser);

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

        UserStatusEventDto event =
                UserStatusEventDto
                        .builder()
                        .id(id)
                        .userStatus(status)
                        .build();

        userEventProducer.sendUserStatusUpdateEvent(event);
    }

    @Override
    public List<UserResponse> getAllUser() {

        List<User> users =
                userRepository.findAll();

        if(users.isEmpty()){
            throw new UserNotFoundException("No User Found in Database");
        }

        return users.stream()
                .map(ResponseConversions::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(UUID id) {

        User user =
                userRepository.findById(id).orElseThrow(
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
}
