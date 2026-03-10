package com.yasirkhan.auth.services;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface JwtService {

    String generateJwtToken(String username, Map<String, String> headers);

    String extractUsername(String token);

    Boolean isTokenValid(String token, UserDetails userDetails);

    UserDetails loadUserByUsername(String username);
}
