package com.innowise.authenticationService.filter;

import com.innowise.authenticationService.entity.UserCredentials;
import com.innowise.common.exception.AuthenticationCustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHelper {

    @Value("${jwt.secretKey}")
    private String rawSecretKey;

    @Value("${jwt.refreshSecretKey}")
    private String rawRefreshSecretKey;

    @Value("${jwt.refreshTokenExpiration}")
    private Long refreshTokenExpiration;

    @Value("${jwt.expiration}")
    private Long expiration;

    SecretKey secretKey;
    SecretKey refreshSecretKey;

    private final AuthenticationManager authenticationManager;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(
                rawSecretKey.getBytes(StandardCharsets.UTF_8)
        );
        this.refreshSecretKey = Keys.hmacShaKeyFor(
                rawRefreshSecretKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    private Claims parseClaims(String token, SecretKey key) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String createToken(Map<String, Object> claims, String subject) {
        Date expiryDate = Date.from(Instant.ofEpochMilli(System.currentTimeMillis() + expiration));
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(String subject) {
        Date expiryDate = Date.from(Instant.ofEpochMilli(System.currentTimeMillis() + refreshTokenExpiration));
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expiryDate)
                .signWith(refreshSecretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token, secretKey).getSubject();
    }

    public Date extractExpiration(String token) {
        return parseClaims(token, secretKey).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token, UserCredentials userCredentials) {

        final String userName = extractUsername(token);
        return userName.equals(userCredentials.getUsername()) && !isTokenExpired(token);
    }


    public String extractUsernameFromRefreshToken(String refreshToken) {
        return parseClaims(refreshToken, refreshSecretKey).getSubject();
    }

    public boolean validateRefreshToken(String refreshToken, UserCredentials userCredentials) {

        final String username = extractUsernameFromRefreshToken(refreshToken);
        boolean isTokenExpired = parseClaims(refreshToken, refreshSecretKey).getExpiration().before(new Date());

        return username.equals(userCredentials.getUsername()) && !isTokenExpired;
    }

    public void authenticateUser(String email, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (BadCredentialsException e) {
            throw new AuthenticationCustomException("Invalid username or password", e);
        }
    }
}
