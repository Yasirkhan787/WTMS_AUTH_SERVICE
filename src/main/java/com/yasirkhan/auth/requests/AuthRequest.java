package com.yasirkhan.auth.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    private String username;

    private String password;
}
