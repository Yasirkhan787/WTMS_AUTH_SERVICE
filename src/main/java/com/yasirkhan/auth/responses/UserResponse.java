package com.yasirkhan.auth.responses;


import com.yasirkhan.auth.models.entity.Role;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;

    private String username;

    private String email;

    private Role role;

    private Boolean isBlocked;

}