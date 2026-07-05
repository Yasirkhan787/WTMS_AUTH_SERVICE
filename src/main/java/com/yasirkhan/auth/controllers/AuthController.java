package com.yasirkhan.auth.controllers;

import com.yasirkhan.auth.models.entity.RefreshToken;
import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.requests.AuthRequest;
import com.yasirkhan.auth.requests.RefreshTokenRequest;
import com.yasirkhan.auth.responses.AuthResponse;
import com.yasirkhan.auth.responses.RefreshTokenResponse;
import com.yasirkhan.auth.responses.Response;
import com.yasirkhan.auth.services.AuthService;
import com.yasirkhan.auth.services.JwtService;
import com.yasirkhan.auth.services.RefreshTokenService;
import com.yasirkhan.auth.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// CHANGED: added @Slf4j and log statements at each endpoint. Spring Boot's Micrometer
// tracing bridge automatically injects the current traceId/spanId into the SLF4J MDC,
// and logging.pattern.console (see application.properties) prints them - so every line
// below is automatically correlated to a trace without needing to log IDs manually.
// Only non-sensitive identifiers (username/userId) are logged - never passwords or raw tokens.
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    public AuthController(AuthService authService, JwtService jwtService,
                          RefreshTokenService refreshTokenService, UserService userService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        log.info("Login request received for username={}", authRequest.getUsername());

        AuthResponse response = authService.login(authRequest);

        log.info("Login successful for username={}", authRequest.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest tokenRequest) {
        log.info("Access token refresh requested");

        RefreshToken refreshToken = refreshTokenService.findByToken(tokenRequest.getRefreshToken());
        refreshTokenService.validateRefreshToken(refreshToken);

        User userInfo = refreshToken.getUser();

        Map<String, Object> headers = Map.of(
                "role", userInfo.getRole().name(),
                "userId", userInfo.getId().toString(),
                "tokenVersion", userInfo.getTokenVersion()
        );

        String accessToken = jwtService.generateJwtToken(userInfo.getUsername(), headers);

        log.info("Access token refreshed for userId={}", userInfo.getId());

        return new ResponseEntity<>(
                RefreshTokenResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken.getToken())
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken)) {

            User user = (User) authentication.getPrincipal();
            log.info("Logout requested by userId={}, username={}", user.getId(), user.getUsername());

            if (userService.logoutUser(user)) {
                log.info("Logout completed for userId={}", user.getId());
                return ResponseEntity.status(HttpStatus.OK).build();
            }
        }

        log.warn("Logout attempted without a valid authenticated session");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/ping")
    public ResponseEntity<Response> ping() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken)) {

            User user = (User) authentication.getPrincipal();
            log.debug("Ping/session-check for userId={}", user.getId());

            Response response = Response.builder()
                    .userId(user.getId().toString())
                    .username(user.getUsername())
                    .role(user.getRole().name())
                    .build();

            return ResponseEntity.ok(response);
        }

        log.warn("Ping attempted without a valid authenticated session");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}