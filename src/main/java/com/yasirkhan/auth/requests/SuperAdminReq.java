package com.yasirkhan.auth.requests;

import com.yasirkhan.auth.models.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminReq {

    private String username;

    private String email;

    private String password;

    private Role role;

    private Boolean isBlocked;
}
