package com.yasirkhan.auth.configs;

import com.yasirkhan.auth.exceptions.BadCredentialsException;
import com.yasirkhan.auth.exceptions.TokenNotFoundException;
import com.yasirkhan.auth.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final HandlerExceptionResolver exceptionResolver;

    // 1. Inject the HandlerExceptionResolver
    public JwtAuthFilter(JwtService jwtService,
                         @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.jwtService = jwtService;
        this.exceptionResolver = exceptionResolver;
    }

    //
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/login") ||
                path.startsWith("/auth/refresh") ||
                path.startsWith("/auth/user/add") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui/");

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = "";
        String username = "";

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new TokenNotFoundException("Missing Access Token");
            }

            token = authHeader.substring(7);
            username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = jwtService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {

                    if (!userDetails.isAccountNonLocked()) {
                        throw new BadCredentialsException("User is blocked by admin");
                    }

                    // Set Spring SecurityContextHolder
                    UsernamePasswordAuthenticationToken authToken
                            = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            exceptionResolver.resolveException(request, response, null, e);
        }
    }
}