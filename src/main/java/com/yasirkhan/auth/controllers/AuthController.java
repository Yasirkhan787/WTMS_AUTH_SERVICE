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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    private final UserService userService;

    public AuthController(AuthService authService, JwtService jwtService, RefreshTokenService refreshTokenService, UserService userService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest){

        return
                ResponseEntity.ok(authService.login(authRequest));
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest tokenRequest){

        RefreshToken token = refreshTokenService.findByToken(tokenRequest.getRefreshToken());

        RefreshToken refreshedToken =
                refreshTokenService.validateRefreshToken(token);

        RefreshToken refreshToken =
                refreshTokenService.validateRefreshToken(refreshedToken);

        User userInfo =
                refreshTokenService.getUserFromRefreshToken(refreshToken.getToken());

        Map<String, String> headers = new HashMap<>();
        headers.put("role", userInfo.getRole().name());
        headers.put("userId",userInfo.getId().toString());

        String accessToken = jwtService.generateJwtToken(userInfo.getUsername(), headers);

        return
                new ResponseEntity<>(RefreshTokenResponse
                        .builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshedToken.getToken())
                        .build(),HttpStatus.OK);
    }

    @GetMapping("/ping")
    public ResponseEntity<Response> ping() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken)) {

            User user = userService.getUserByUsername(authentication.getName());

            Response response = Response.builder()
                    .userId(user.getId().toString())
                    .username(user.getUsername())
                    .role(user.getRole().name())
                    .build();

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
