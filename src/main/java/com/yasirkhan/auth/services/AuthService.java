package com.yasirkhan.auth.services;

import com.yasirkhan.auth.requests.AuthRequest;
import com.yasirkhan.auth.responses.AuthResponse;
import org.jspecify.annotations.Nullable;

public interface AuthService {
    AuthResponse login(AuthRequest authRequest);
}
