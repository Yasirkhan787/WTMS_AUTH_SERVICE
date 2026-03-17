package com.yasirkhan.auth.services;

import com.yasirkhan.auth.models.dto.UserEventDto;
import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.requests.UserRequest;
import com.yasirkhan.auth.responses.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse addUser(UserEventDto userCreateEventDto);

    List<UserResponse> getAllUser();

    UserResponse getUserById(UUID id);

    void updateUser(UserEventDto updateEventDto);

    void blockUser(UUID id, Boolean blockStatus);

    User getUserByUsername(String username);
}
