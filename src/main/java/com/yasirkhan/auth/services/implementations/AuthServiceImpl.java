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

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(UserRepository userRepository, AuthenticationManager authenticationManager, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public AuthResponse login(AuthRequest authRequest) {
        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            throw new BadCredentialsException(e.getMessage());
        }

        if (authentication.isAuthenticated()) {
            // Get user by username
            User user = userRepository.findByUsername(authRequest.getUsername())
                    .orElseThrow(() -> new UserNotFoundException("User not found with Username: " + authRequest.getUsername()));

            // Check if user is blocked
            if (user.getIsBlocked()) {
                throw new BadCredentialsException("User is blocked by admin");
            }

            // Get role as string
            String role = user.getRole().name();

            // Get userId as String
            String userId = user.getId().toString();

            Map<String, String> headers = new HashMap<>();
            headers.put("role", role);
            headers.put("userId", userId);

            // Generate JWT Access Token
            String accessToken
                    = jwtService.generateJwtToken(user.getUsername(), headers);

            // Generate Refresh Token (longer expiry)
            String refreshToken
                    = refreshTokenService.generateRefreshToken(user.getUsername());

            // Build response
            AuthResponse response =
                    AuthResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .build();

            return response;
        } else {
            throw new UserNotFoundException("User Not Found Authentication failed!");
        }
    }
}
