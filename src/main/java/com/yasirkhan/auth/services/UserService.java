package com.yasirkhan.auth.services;

import com.yasirkhan.auth.models.dto.UserUpdateEventDto;
import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.requests.UserRequest;
import com.yasirkhan.auth.responses.UserResponse;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse addUser(UserRequest userRequest);

    List<UserResponse> getAllUser();

    UserResponse getUserById(UUID id);

    UserResponse updateUser(UserUpdateEventDto updateEventDto);

    void blockUser(UUID id, Boolean blockStatus);

    User getUserByUsername(String username);
}
