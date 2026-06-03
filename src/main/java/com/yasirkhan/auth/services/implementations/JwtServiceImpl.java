package com.yasirkhan.auth.services.implementations;

import com.yasirkhan.auth.exceptions.SessionExpiredException;
import com.yasirkhan.auth.exceptions.TokenExpiredException;
import com.yasirkhan.auth.models.entity.User;
import com.yasirkhan.auth.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
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
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    private final Long EXPIRATION_TIME = 1000 * 60 * 20L;  // 20 minutes

    @Value("${jwt.private-key.path}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key.path}")
    private Resource publicKeyResource;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    private final UserDetailsServiceImpl userDetailsService;

    public JwtServiceImpl(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostConstruct
    public void init() throws Exception {
        // 1. Load Private Key for SIGNING tokens
        byte[] privKeyBytes = privateKeyResource.getInputStream().readAllBytes();
        String privKeyString = new String(privKeyBytes)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decodedPrivKey = Base64.getDecoder().decode(privKeyString);
        PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(decodedPrivKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.privateKey = keyFactory.generatePrivate(privKeySpec);

        // 2. Load Public Key for VERIFYING tokens locally if needed
        byte[] pubKeyBytes = publicKeyResource.getInputStream().readAllBytes();
        String pubKeyString = new String(pubKeyBytes)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decodedPubKey = Base64.getDecoder().decode(pubKeyString);
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(decodedPubKey);
        this.publicKey = keyFactory.generatePublic(pubKeySpec);
    }

    @Override
    public String generateJwtToken(String username, Map<String, Object> headers) {
        return Jwts.builder()
                .subject(username)
                .claim("role", headers.get("role"))
                .claim("userId", headers.get("userId"))
                .claim("tokenVersion", headers.get("tokenVersion"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(privateKey)
                .compact();
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

        Integer tokenVersion = extractClaim(token, (claims) -> claims.get("tokenVersion", Integer.class));
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