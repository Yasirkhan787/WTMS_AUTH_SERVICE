package com.yasirkhan.auth.services;

import com.yasirkhan.auth.requests.UserRequest;
import com.yasirkhan.auth.responses.UserResponse;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse addUser(UserRequest userRequest);

    List<UserResponse> getAllUser();

    UserResponse getUserById(UUID id);

    UserResponse updateUser(UUID id, UserRequest updateRequest);

    void blockUser(UUID id);
}
