package com.innowise.authenticationService.service;

import com.innowise.authenticationService.dto.RefreshTokenRequest;
import com.innowise.authenticationService.dto.TokenRequest;
import com.innowise.authenticationService.dto.TokenResponse;
import com.innowise.authenticationService.dto.TokenValidationRequest;
import com.innowise.authenticationService.entity.UserCredentials;
import com.innowise.authenticationService.exception.AuthenticationCustomException;
import com.innowise.authenticationService.exception.ResourceNotFoundCustomException;
import com.innowise.authenticationService.exception.TokenValidationCustomException;
import com.innowise.authenticationService.filter.JwtHelper;
import com.innowise.authenticationService.repository.UserCredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class TokenService {

    JwtHelper jwtHelper;

    UserCredentialsService userCredentialsService;
    UserCredentialsRepository userCredentialsRepository;
     WebClient webClient;


    public TokenResponse authenticate(TokenRequest tokenRequest) {
        if(tokenRequest == null) {
            throw new AuthenticationCustomException("Invalid token request");
        }
        jwtHelper.authenticateUser(tokenRequest.getEmail(), tokenRequest.getPassword());

        Map<String,Object> claims = userCredentialsService.generateClaims(tokenRequest.getEmail());

        TokenResponse tokenResponse = new TokenResponse();
        String accessToken = jwtHelper.createToken(claims, tokenRequest.getEmail());
        tokenResponse.setAccessToken(accessToken);
        String refreshToken = jwtHelper.createRefreshToken(tokenRequest.getEmail());
        tokenResponse.setRefreshToken(refreshToken);
        userCredentialsService.saveRefreshToken(refreshToken,tokenRequest.getEmail());

        saveToUserService(accessToken,tokenRequest.getEmail());
        return tokenResponse;
    }
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest tokenRequest) {
        if(tokenRequest == null) {
            throw new TokenValidationCustomException("Invalid token request");
        }

        String username = jwtHelper.extractUsernameFromRefreshToken(tokenRequest.getRefreshToken());

        UserCredentials userCredentials = userCredentialsRepository.findByEmail(
                username);
        if (userCredentials == null) {
            throw new ResourceNotFoundCustomException("User not found: " + username);
        }
        if (!tokenRequest.getRefreshToken().equals(userCredentials.getRefreshToken())) {
            throw new TokenValidationCustomException("Invalid refresh token");
        }
        
        if (!jwtHelper.validateRefreshToken(tokenRequest.getRefreshToken(), userCredentials)) {
            throw new TokenValidationCustomException("Refresh token validation failed");
        }
        String refreshToken = jwtHelper.createRefreshToken(userCredentials.getEmail());
        userCredentialsService.saveRefreshToken(refreshToken,username);

        String accessToken = jwtHelper.createToken(
                userCredentialsService.generateClaims(userCredentials.getEmail()), userCredentials.getEmail());

        TokenResponse updatedTokensResponse = new TokenResponse();
        updatedTokensResponse.setAccessToken(accessToken);
        updatedTokensResponse.setRefreshToken(refreshToken);

        return updatedTokensResponse;
    }

    public void validateToken(TokenValidationRequest request) {
        if (request == null || request.getToken() == null) {
            throw new TokenValidationCustomException("Token is required");
        }

        try {
            String username = jwtHelper.extractUsername(request.getToken());
            UserCredentials userCredentials = userCredentialsService.loadUserByUsername(username);
            if (userCredentials == null || !jwtHelper.validateToken(request.getToken(), userCredentials)) {
                throw new TokenValidationCustomException("Access token validation failed");
            }
            return;
        } catch (ResourceNotFoundCustomException e) {
        throw new ResourceNotFoundCustomException("Access token validation failed");
        } catch (Exception ignored) {
        }
        
        try {
            String username = jwtHelper.extractUsernameFromRefreshToken(request.getToken());
            UserCredentials userCredentials = userCredentialsService.loadUserByUsername(username);
            if (userCredentials == null || !jwtHelper.validateRefreshToken(request.getToken(), userCredentials)) {
                throw new TokenValidationCustomException("Refresh token validation failed");
            }
        } catch (ResourceNotFoundCustomException e) {
            throw new TokenValidationCustomException("Token validation failed: user not found");
        } catch (Exception e) {
            throw new TokenValidationCustomException("Token validation failed", e);
        }
    }
    public void saveToUserService(String accessToken, String email) {
        webClient.post()
                .uri("/api/v1/users/add")
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(Map.of("email", email))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
