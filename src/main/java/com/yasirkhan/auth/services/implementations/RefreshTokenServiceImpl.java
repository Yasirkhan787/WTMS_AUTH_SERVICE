package com.yasirkhan.auth.services.implementations;

import com.yasirkhan.auth.exceptions.TokenExpiredException;
import com.yasirkhan.auth.exceptions.TokenNotFoundException;
import com.yasirkhan.auth.models.entity.RefreshToken;
import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.repository.RefreshTokenRepository;
import com.yasirkhan.auth.services.RefreshTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final long REFRESH_EXPIRATION_MS = TimeUnit.DAYS.toMillis(7);

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    @Transactional
    public String generateRefreshToken(User user) {

        RefreshToken refreshToken = user.getRefreshToken();
        String newToken = UUID.randomUUID().toString();
        Date newExpiry = new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_MS);

        if (refreshToken != null) {
            refreshToken.setToken(newToken);
            refreshToken.setExpirationDate(newExpiry);
        } else {
            refreshToken = RefreshToken.builder()
                    .token(newToken)
                    .expirationDate(newExpiry)
                    .user(user)
                    .build();

            user.setRefreshToken(refreshToken);
        }

        refreshTokenRepository.save(refreshToken);

        return refreshToken.getToken();
    }

    @Override
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("Invalid Token"));
    }

    @Override
    public RefreshToken validateRefreshToken(RefreshToken token) {
        if (token.getExpirationDate().before(new Date())) {
            refreshTokenRepository.delete(token);
            throw new TokenExpiredException("Refresh token is expired. Please make a new login..!");
        }
        return token;
    }

    @Override
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}