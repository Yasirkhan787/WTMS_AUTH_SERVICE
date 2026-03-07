package com.yasirkhan.auth.responses;


import com.yasirkhan.auth.models.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;

    private String username;

    private String email;

    private Role role;

    private Boolean isBlocked;

}