package com.todo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // Make sure your secret is long enough (at least 512 bits)
    private final SecretKey secretKey = Keys.hmacShaKeyFor(
            "your-very-long-secret-key-here-at-least-512-bits-long-for-security".getBytes()
    );

    private final long EXPIRATION = 86400000L; // 1 day

    // Generate JWT token
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)                 // subject = username
                .setIssuedAt(new Date())              // issued now
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION)) // expires
                .signWith(secretKey)                  // sign with secret
                .compact();                           // build token
    }

    // Extract all claims from token
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)             // set signing key
                .build()
                .parseClaimsJws(token)                // parse signed JWT
                .getBody();                           // get Claims object
    }

    // Extract username from token
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    // Validate token
    public boolean isValid(String token, String username) {
        final String tokenUsername = extractUsername(token);
        final Date expiration = extractClaims(token).getExpiration();
        return tokenUsername.equals(username) && expiration.after(new Date());
    }
}
