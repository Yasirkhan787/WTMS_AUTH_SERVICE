package com.yasirkhan.auth.requests;

import com.yasirkhan.auth.models.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    private String username;

    private String email;

    private String password;

    private Role role;

}
