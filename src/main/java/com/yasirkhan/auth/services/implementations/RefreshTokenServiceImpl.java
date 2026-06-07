package com.yasirkhan.auth.services.implementations;

import com.yasirkhan.auth.exceptions.TokenExpiredException;
import com.yasirkhan.auth.exceptions.TokenNotFoundException;
import com.yasirkhan.auth.models.entity.RefreshToken;
import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.repository.RefreshTokenRepository;
import com.yasirkhan.auth.repository.UserRepository;
import com.yasirkhan.auth.services.RefreshTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final Long REFRESH_EXPIRATION = 1000 * 60 * 60 * 24 * 7L; // 7 days
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenServiceImpl(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    @Transactional
    public String generateRefreshToken(User user) {

        RefreshToken refreshToken = user.getRefreshToken();

        if (refreshToken != null) {
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpirationDate(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION));
        } else {
            refreshToken = RefreshToken.builder()
                    .token(UUID.randomUUID().toString())
                    .expirationDate(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
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
        if(token.getExpirationDate().before(new Date())) {
            refreshTokenRepository.delete(token);
            throw new TokenExpiredException(
                    "Refresh token is expired. Please make a new login..!"
            );
        }
        return token;
    }

    @Override
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}