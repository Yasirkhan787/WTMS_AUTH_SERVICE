package com.yasirkhan.auth.services.implementations;

import com.yasirkhan.auth.exceptions.TokenExpiredException;
import com.yasirkhan.auth.exceptions.TokenNotFoundException;
import com.yasirkhan.auth.exceptions.UserNotFoundException;
import com.yasirkhan.auth.models.entity.RefreshToken;
import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.repository.RefreshTokenRepository;
import com.yasirkhan.auth.repository.UserRepository;
import com.yasirkhan.auth.requests.RefreshTokenRequest;
import com.yasirkhan.auth.services.RefreshTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
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

    //
    @Override
    @Transactional
    public String generateRefreshToken(User user) {

        // Grab the token directly from the user object in memory! (NO DB QUERY)
        RefreshToken refreshToken = user.getRefreshToken();

        if (refreshToken != null) {
            // Token exists. Check expiration.
            if (refreshToken.getExpirationDate().before(new Date())) {
                // Expired → create new token string and extend time
                refreshToken.setToken(UUID.randomUUID().toString());
                refreshToken.setExpirationDate(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION));
            }
            // If it's not expired, it just keeps the existing data
        } else {
            // No token exists for this user yet. Build a new one.
            refreshToken = RefreshToken.builder()
                    .token(UUID.randomUUID().toString())
                    .expirationDate(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                    .user(user)
                    .build();

            // VERY IMPORTANT: Link the new token back to the user object in memory
            user.setRefreshToken(refreshToken);
        }

        // Save the token (This executes 1 highly efficient UPDATE or INSERT)
        refreshTokenRepository.save(refreshToken);

        return refreshToken.getToken();
    }

    @Override
    public RefreshToken findByToken(String token) {

        return
                refreshTokenRepository
                        .findByToken(token)
                        .orElseThrow(() -> new TokenNotFoundException("Invalid Token"));

    }

    //
    @Override
    public RefreshToken validateRefreshToken(RefreshToken token) {

        // token expired
        if(token.getExpirationDate().before(new Date())) {

            refreshTokenRepository.delete(token);

            throw new TokenExpiredException(
                    "Refresh token is expired. Please make a new login..!"
            );
        }

        // token valid
        return token;
    }

    @Override
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}
