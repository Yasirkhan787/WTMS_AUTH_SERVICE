package com.yasirkhan.auth.services;

import com.yasirkhan.auth.models.entity.RefreshToken;
import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.requests.RefreshTokenRequest;

public interface RefreshTokenService {

    RefreshToken findByToken(String token);

    String generateRefreshToken(String email);

    RefreshToken validateRefreshToken(RefreshToken token);

    User getUserFromRefreshToken(String token);
}
