package com.yasirkhan.auth.services;

import com.yasirkhan.auth.models.entity.RefreshToken;
import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.requests.RefreshTokenRequest;

public interface RefreshTokenService {

    RefreshToken findByToken(String token);

    String generateRefreshToken(User user);

    RefreshToken validateRefreshToken(RefreshToken token);

    void deleteRefreshToken(String token);
}
