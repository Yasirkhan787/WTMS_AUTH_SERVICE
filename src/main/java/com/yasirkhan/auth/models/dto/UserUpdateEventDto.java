package com.yasirkhan.auth.models.dto;

import com.yasirkhan.auth.models.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateEventDto {

    private UUID userId;

    private String username;

    private String email;

    private String password;

    private Role role;

    private Boolean isBlocked;
}
