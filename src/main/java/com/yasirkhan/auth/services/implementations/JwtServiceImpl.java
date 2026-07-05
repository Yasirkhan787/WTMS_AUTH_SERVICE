package com.yasirkhan.auth.services.implementations;

import com.yasirkhan.auth.exceptions.SessionExpiredException;
import com.yasirkhan.auth.exceptions.TokenExpiredException;
import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    private static final long EXPIRATION_TIME_MS = TimeUnit.HOURS.toMillis(1);

    private static final String REDIS_TOKEN_KEY_PREFIX = "user:token:";

    @Value("${jwt.private-key.path}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key.path}")
    private Resource publicKeyResource;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    private final UserDetailsServiceImpl userDetailsService;
    private final StringRedisTemplate redisTemplate;

    public JwtServiceImpl(UserDetailsServiceImpl userDetailsService, StringRedisTemplate redisTemplate) {
        this.userDetailsService = userDetailsService;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() throws Exception {
        this.privateKey = loadPrivateKey(privateKeyResource);
        this.publicKey = loadPublicKey(publicKeyResource);
    }

    private PrivateKey loadPrivateKey(Resource resource) throws Exception {
        String pem = new String(resource.getInputStream().readAllBytes())
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(pem);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    private PublicKey loadPublicKey(Resource resource) throws Exception {
        String pem = new String(resource.getInputStream().readAllBytes())
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(pem);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
    }

    @Override
    public String generateJwtToken(String username, Map<String, Object> headers) {
        String userId = String.valueOf(headers.get("userId"));
        String tokenVersion = String.valueOf(headers.get("tokenVersion"));

        String token = Jwts.builder()
                .subject(username)
                .claim("role", headers.get("role"))
                .claim("userId", userId)
                .claim("tokenVersion", headers.get("tokenVersion"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .signWith(privateKey)
                .compact();

        redisTemplate.opsForValue().set(
                REDIS_TOKEN_KEY_PREFIX + userId,
                tokenVersion,
                EXPIRATION_TIME_MS,
                TimeUnit.MILLISECONDS
        );

        return token;
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);

        if (!username.equals(userDetails.getUsername())) {
            throw new UsernameNotFoundException("User Not Found with username: " + username);
        }

        if (isTokenExpired(token)) {
            throw new TokenExpiredException("Token has expired");
        }

        Integer tokenVersion = extractClaim(token, claims -> claims.get("tokenVersion", Integer.class));
        User user = (User) userDetails;

        if (!tokenVersion.equals(user.getTokenVersion())) {
            throw new SessionExpiredException("Session Expired: Logged in from another device.");
        }

        return true;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        Date expirationDate = extractClaim(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userDetailsService.loadUserByUsername(username);
    }
}