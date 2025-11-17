package com.example.demo.security;

import com.example.demo.entities.Credential;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    //set this in application.properties
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration.ms}")
    private long expirationTimeMs;

    //methods for validation ---

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // Can log e.getMessage() for debugging
            return false;
        }
    }

    public String getRoleFromToken(String token) {
        return Jwts.parser().verifyWith(getKey()).build()
                .parseSignedClaims(token).getPayload()
                .get("role", String.class);
    }

    public String generateToken(Credential user) { // Changed from User to Credential
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
        claims.put("sub", user.getUsername()); // sub (subject) is standard
        claims.put("userId", user.getUserId());

        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTimeMs);

        // Generate a secure key
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }
}