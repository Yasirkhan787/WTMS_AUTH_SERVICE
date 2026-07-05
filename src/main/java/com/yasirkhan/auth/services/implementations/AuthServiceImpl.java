package com.yasirkhan.auth.services.implementations;

import com.yasirkhan.auth.exceptions.BadCredentialsException;
import com.yasirkhan.auth.exceptions.UserNotFoundException;
import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.repository.UserRepository;
import com.yasirkhan.auth.requests.AuthRequest;
import com.yasirkhan.auth.responses.AuthResponse;
import com.yasirkhan.auth.services.AuthService;
import com.yasirkhan.auth.services.JwtService;
import com.yasirkhan.auth.services.RefreshTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(UserRepository userRepository, AuthenticationManager authenticationManager,
                           JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    // CHANGED: added @Transactional so the tokenVersion increment (userRepository.save) and
    // refresh-token persistence happen atomically - if refresh token generation fails, the
    // tokenVersion bump is rolled back instead of silently invalidating the user's other sessions.
    @Transactional
    public AuthResponse login(AuthRequest authRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new BadCredentialsException(e.getMessage());
        }

        if (!authentication.isAuthenticated()) {
            throw new UserNotFoundException("User Not Found Authentication failed!");
        }

        User user = (User) authentication.getPrincipal();

        if (!user.isAccountNonLocked()) {
            throw new BadCredentialsException("User is blocked by admin");
        }

        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        Map<String, Object> headers = Map.of(
                "role", user.getRole().name(),
                "userId", user.getId().toString(),
                "tokenVersion", user.getTokenVersion()
        );

        String accessToken = jwtService.generateJwtToken(user.getUsername(), headers);
        String refreshToken = refreshTokenService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}