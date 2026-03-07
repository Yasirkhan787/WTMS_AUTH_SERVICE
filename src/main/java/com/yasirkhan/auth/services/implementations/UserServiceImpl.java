package com.yasirkhan.auth.services.implementations;

import com.yasirkhan.auth.exceptions.UserAlreadyExistException;
import com.yasirkhan.auth.exceptions.UserNotFoundException;
import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.repository.UserRepository;
import com.yasirkhan.auth.requests.UserRequest;
import com.yasirkhan.auth.responses.UserResponse;
import com.yasirkhan.auth.services.UserService;
import com.yasirkhan.auth.utils.ResponseConversions;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse addUser(UserRequest userRequest) {

        // Check if username is already exist
        if(userRepository.existsByUsername(userRequest.getUsername())){
            throw new UserAlreadyExistException("User with Username: " + userRequest.getUsername() + " is already exist");
        }

        // Check if email is already exist
        if(userRepository.existsByEmail(userRequest.getEmail())){
            throw new UserAlreadyExistException("User with Email: " + userRequest.getUsername() + " is already exist");
        }

        // Converting UserRequest to User Entity
        User user =  new User();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setRole(userRequest.getRole());
        user.setIsBlocked(false);

        // Save to the DB
        User savedUser = userRepository.save(user);

        return ResponseConversions.toUserResponse(savedUser);
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
    public UserResponse updateUser(UUID id, UserRequest updateRequest) {

        User dbUser =
                userRepository.findById(id).orElseThrow(
                        () -> new UserNotFoundException("User with ID: " + id + " Not Found"));

        dbUser.setUsername(updateRequest.getUsername());
        dbUser.setEmail(updateRequest.getEmail());
        dbUser.setRole(updateRequest.getRole());

        // Save to the DB
        User updatedUser =
                userRepository.save(dbUser);

        return ResponseConversions.toUserResponse(updatedUser);
    }

    @Override
    public void blockUser(UUID id) {

        User dbUser =
                userRepository.findById(id).orElseThrow(
                        () -> new UserNotFoundException("User with ID: " + id + " Not Found"));

        dbUser.setIsBlocked(true);
    }


}
