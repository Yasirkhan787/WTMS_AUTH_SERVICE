package com.yasirkhan.auth.utils;

import com.yasirkhan.auth.requests.DriverRequest;
import com.yasirkhan.auth.requests.UserRequest;

public class RequestConversion {

    public static UserRequest toUserRequest(DriverRequest request){

        return UserRequest
                .builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(request.getRole())
                .build();
    }
}
