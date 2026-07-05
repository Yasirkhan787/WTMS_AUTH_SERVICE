package com.yasirkhan.auth.configs;

import com.yasirkhan.auth.exceptions.BadCredentialsException;
import com.yasirkhan.auth.exceptions.TokenNotFoundException;
import com.yasirkhan.auth.exceptions.UnauthorizedException;
import com.yasirkhan.auth.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    // CHANGED: removed the "" default. A blank secret would otherwise let a request through
    // with an empty X-Gateway-Secret header if the property was ever misconfigured/missing.
    // Now Spring fails fast at startup if app.security.internal-secret isn't set.
    @Value("${app.security.internal-secret}")
    private String gatewaySecret;

    private final JwtService jwtService;
    private final HandlerExceptionResolver exceptionResolver;

    public JwtAuthFilter(JwtService jwtService,
                         @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.jwtService = jwtService;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // CHANGED: now reuses SecurityConfig.PUBLIC_ENDPOINTS instead of a second,
        // independently-maintained list of the same paths.
        return Arrays.stream(SecurityConfig.PUBLIC_ENDPOINTS)
                .map(pattern -> pattern.replace("/**", ""))
                .anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            validateGatewaySecret(request);

            String token = extractBearerToken(request);
            String username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = jwtService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {
                    if (!userDetails.isAccountNonLocked()) {
                        throw new BadCredentialsException("User is blocked by admin");
                    }

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            exceptionResolver.resolveException(request, response, null, e);
        }
    }

    // CHANGED: extracted from doFilterInternal for readability/single-responsibility.
    private void validateGatewaySecret(HttpServletRequest request) {
        String incomingSecret = request.getHeader("X-Gateway-Secret");
        if (!StringUtils.hasText(incomingSecret) || !incomingSecret.equals(gatewaySecret)) {
            throw new UnauthorizedException("Direct access blocked: Request must come through API Gateway");
        }
    }

    // CHANGED: extracted from doFilterInternal for readability/single-responsibility.
    private String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new TokenNotFoundException("Missing Access Token");
        }
        return authHeader.substring(7);
    }
}