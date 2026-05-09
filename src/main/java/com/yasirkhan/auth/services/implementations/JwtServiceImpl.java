package com.yasirkhan.auth.services.implementations;

import com.yasirkhan.auth.exceptions.SessionExpiredException;
import com.yasirkhan.auth.exceptions.TokenExpiredException;
import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.services.JwtService;
import com.yasirkhan.auth.services.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    private final Long EXPIRATION_TIME = 1000*60*1L;  // 15 minutes

    private final String SECRET;

    private final UserDetailsServiceImpl userDetailsService;

    public JwtServiceImpl(@Value("${jwt.secret}") String SECRET, UserDetailsServiceImpl userDetailsService) {
        this.SECRET = SECRET;
        this.userDetailsService = userDetailsService;
    }

    //
    @Override
    public String generateJwtToken(String username, Map<String, Object> headers) {

        return Jwts
                .builder()
                .setSubject(username)
                .claim("role", headers.get("role"))
                .claim("userId", headers.get("userId"))
                .claim("tokenVersion", headers.get("tokenVersion"))
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

        String username = extractUsername(token);

        if(!username.equals(userDetails.getUsername())){
            throw new UsernameNotFoundException("User Not Found with username: " + username);
        }

        if(isTokenExpired(token)){
            throw new TokenExpiredException("Token has expired");
        }

        // Token Version
        Integer tokenVersion = extractClaim(token,
                (claims) -> claims.get("tokenVersion", Integer.class));

        User user = (User) userDetails;

        if (!tokenVersion.equals(user.getTokenVersion())){
            throw new SessionExpiredException("Session Expired: Logged in from another device.");
        }

        return true;
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

