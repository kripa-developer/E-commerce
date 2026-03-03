package com.novacart.auth.service;

import com.novacart.common.config.JwtProperties;
import com.novacart.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtProperties.accessTokenExpirationMs())))
                .signWith(signingKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("uid", user.getId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtProperties.refreshTokenExpirationMs())))
                .signWith(signingKey())
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValidToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration() != null && claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    public long getAccessExpirationMs() {
        return jwtProperties.accessTokenExpirationMs();
    }

    private SecretKey signingKey() {
        String secret = jwtProperties.secret();
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException ex) {
            keyBytes = secret.getBytes();
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
