package com.yasirkhan.auth.services;

import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.requests.*;
import com.yasirkhan.auth.responses.UserResponse;

import java.util.List;
import java.util.Map;

public interface UserService {

    UserResponse addUser(UserRequest request);

    List<UserResponse> getAllUser();

    UserResponse getUserById(String id);

    void updateUser(Map<String, Object> updateRequest);

    void blockUser(String id, Boolean blockStatus);

    User getUserByUsername(String username);

    boolean logoutUser(User user);

    UserResponse addUser(SuperAdminReq request);

    void changePassword(User user, ChangePasswordRequest request);

    String generateForgetPasswordToken(ForgetPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
