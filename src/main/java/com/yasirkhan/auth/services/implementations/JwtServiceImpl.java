package com.yasirkhan.auth.services.implementations;

import com.yasirkhan.auth.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    private final Long EXPIRATION_TIME = 1000*60*60L;  // 1 hour

    private final String SECRET;

    private final UserDetailsServiceImpl userDetailsService;

    public JwtServiceImpl(@Value("${jwt.secret}") String SECRET, UserDetailsServiceImpl userDetailsService) {
        this.SECRET = SECRET;
        this.userDetailsService = userDetailsService;
    }

    //
    @Override
    public String generateJwtToken(String username, Map<String, String> headers) {

        return Jwts
                .builder()
                .setSubject(username)
                .claim("role", headers.get("role"))
                .claim("userId", headers.get("userId"))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    //
    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Boolean isTokenValid(String token, UserDetails userDetails) {
        String username =
                extractUsername(token);
        return ((username.equals(userDetails.getUsername())) && (!isTokenExpired(token)));
    }

    //
    private Key getSignKey(){
        byte[] key = SECRET.getBytes();
        return Keys.hmacShaKeyFor(key);
    }

    //
    private Claims extractAllClaims(String token){
        return Jwts
                .parser()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    //
    private boolean isTokenExpired(String token) {
        Date expirationDate = extractClaim(token, Claims::getExpiration);
        return (expirationDate.before(new Date()));
    }

    //
    @Override
    public UserDetails loadUserByUsername(String username) {

        return
                userDetailsService.loadUserByUsername(username);
    }
}

