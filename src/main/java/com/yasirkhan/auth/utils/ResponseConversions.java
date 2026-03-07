package com.yasirkhan.auth.utils;

import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.responses.UserResponse;

public class ResponseConversions {

    public static UserResponse toUserResponse(User user){

        return UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .isBlocked(user.getIsBlocked())
                        .build();
    }
}
