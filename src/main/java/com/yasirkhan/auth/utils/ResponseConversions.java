package com.yasirkhan.auth.utils;

import com.yasirkhan.auth.models.dto.DriverDto;
import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.requests.DriverRequest;
import com.yasirkhan.auth.responses.DriverResponse;
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

    public static DriverResponse toDriverResponse(UserResponse userResponse, DriverDto driverDto) {
        return DriverResponse.builder()
                .id(userResponse.getId())
                .username(userResponse.getUsername())
                .isBlocked(userResponse.getIsBlocked())
                .role(userResponse.getRole())
                .name(driverDto.getName())
                .fatherName(driverDto.getFatherName())
                .email(driverDto.getEmail())
                .cnic(driverDto.getCnic())
                .phoneNo(driverDto.getPhoneNo())
                .address(driverDto.getAddress())
                .gender(driverDto.getGender())
                .licenseNo(driverDto.getLicenseNo())
                .licenseExpiry(driverDto.getLicenseExpiry())
                .build();
    }
}
