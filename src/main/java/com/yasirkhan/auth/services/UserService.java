package com.yasirkhan.auth.services;

import com.yasirkhan.auth.models.dtos.UserEventDto;
import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.requests.SuperAdminReq;
import com.yasirkhan.auth.requests.UserRequest;
import com.yasirkhan.auth.responses.UserResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface UserService {

    UserResponse addUser(UserRequest request);

    List<UserResponse> getAllUser();

    UserResponse getUserById(String id);

    void updateUser(Map<String, Object> updateRequest);

    void blockUser(String id, Boolean blockStatus);

    User getUserByUsername(String username);

    boolean logoutUser(User user);

    UserResponse addUser(SuperAdminReq request);
}
