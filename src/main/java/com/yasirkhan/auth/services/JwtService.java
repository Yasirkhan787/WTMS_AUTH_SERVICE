package com.yasirkhan.auth.services;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateJwtToken(String username, String role);

    String extractUsername(String token);

    Boolean isTokenValid(String token, UserDetails userDetails);

    UserDetails loadUserByUsername(String username);
}
