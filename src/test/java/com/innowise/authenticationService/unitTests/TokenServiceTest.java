package com.innowise.authenticationService.unitTests;


import com.innowise.authenticationService.dto.RefreshTokenRequest;
import com.innowise.authenticationService.dto.TokenRequest;
import com.innowise.authenticationService.dto.TokenResponse;
import com.innowise.authenticationService.dto.TokenValidationRequest;
import com.innowise.authenticationService.entity.UserCredentials;
import com.innowise.authenticationService.exception.AuthenticationCustomException;
import com.innowise.authenticationService.exception.TokenValidationCustomException;
import com.innowise.authenticationService.filter.JwtHelper;
import com.innowise.authenticationService.repository.UserCredentialsRepository;
import com.innowise.authenticationService.service.TokenService;
import com.innowise.authenticationService.service.UserCredentialsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest{

    @Mock
    private JwtHelper jwtHelper;

    @Mock
    private UserCredentialsService userCredentialsService;

    @Mock
    private UserCredentialsRepository userCredentialsRepository;

    @InjectMocks
    private TokenService tokenService;

    private TokenRequest validTokenRequest;
    private RefreshTokenRequest validRefreshRequest;
    private TokenValidationRequest validValidationRequest;
    private UserCredentials userCredentials;

    @BeforeEach
    void setUp() {
        validTokenRequest = new TokenRequest("user@example.com", "password");
        validRefreshRequest = new RefreshTokenRequest("refreshToken");
        validValidationRequest = new TokenValidationRequest("accessToken");
        userCredentials = new UserCredentials();
        userCredentials.setEmail("user@example.com");
        userCredentials.setRefreshToken("refreshToken");
    }

    @Test
    void authenticateShouldReturnsTokens() {
        jwtHelper.authenticateUser("user@example.com", "password");

        doNothing().when(jwtHelper).authenticateUser("user@example.com", "password");

        when(userCredentialsService.generateClaims("user@example.com"))
                .thenReturn(Map.of("role", "USER"));
        when(jwtHelper.createToken(anyMap(), eq("user@example.com")))
                .thenReturn("accessToken");
        when(jwtHelper.createRefreshToken("user@example.com"))
                .thenReturn("refreshToken");
        
        TokenResponse response = tokenService.authenticate(validTokenRequest);
        
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());

        verify(userCredentialsService).saveRefreshToken("refreshToken", "user@example.com");
    }

    @Test
    void authenticateNullRequestThrowsException() {
        assertThrows(AuthenticationCustomException.class, () -> tokenService.authenticate(null));
    }

    @Test
    void refreshTokenShouldReturnsNewTokens() {

        when(jwtHelper.extractUsernameFromRefreshToken("refreshToken"))
                .thenReturn("user@example.com");
        when(userCredentialsRepository.findByEmail("user@example.com"))
                .thenReturn(userCredentials);
        when(jwtHelper.validateRefreshToken("refreshToken", userCredentials))
                .thenReturn(true);
        when(jwtHelper.createRefreshToken("user@example.com"))
                .thenReturn("newRefreshToken");
        when(jwtHelper.createToken(anyMap(), eq("user@example.com")))
                .thenReturn("newAccessToken");

        TokenResponse response = tokenService.refreshToken(validRefreshRequest);

        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals("newRefreshToken", response.getRefreshToken());
    }

    @Test
    void refreshTokenMethodThrowsValidationException() {

        when(jwtHelper.extractUsernameFromRefreshToken("refreshToken"))
                .thenReturn("user@example.com");
        when(userCredentialsRepository.findByEmail("user@example.com"))
                .thenReturn(userCredentials);
        when(jwtHelper.validateRefreshToken("refreshToken", userCredentials))
                .thenReturn(false);

        assertThrows(TokenValidationCustomException.class, () -> tokenService.refreshToken(validRefreshRequest));
    }

    @Test
    void validateTokenMethodWithValidAccessTokenThrowsNoException() {

        when(jwtHelper.extractUsername("accessToken"))
                .thenReturn("user@example.com");
        when(userCredentialsService.loadUserByUsername("user@example.com"))
                .thenReturn(userCredentials);
        when(jwtHelper.validateToken("accessToken", userCredentials))
                .thenReturn(true);


        assertDoesNotThrow(() -> tokenService.validateToken(validValidationRequest));
    }

    @Test
    void validateTokenMethodWithValidRefreshTokenThrowsNoException() {

        when(jwtHelper.extractUsername("accessToken"))
                .thenThrow(new RuntimeException());

        when(jwtHelper.extractUsernameFromRefreshToken("accessToken"))
                .thenReturn("user@example.com");
        when(userCredentialsService.loadUserByUsername("user@example.com"))
                .thenReturn(userCredentials);
        when(jwtHelper.validateRefreshToken("accessToken", userCredentials))
                .thenReturn(true);

        assertDoesNotThrow(() -> tokenService.validateToken(validValidationRequest));
    }

    @Test
    void validateTokenMethodWithInvalidTokenThrowsException() {

        when(jwtHelper.extractUsername("accessToken"))
                .thenThrow(new RuntimeException());
        when(jwtHelper.extractUsernameFromRefreshToken("accessToken"))
                .thenThrow(new RuntimeException());

        assertThrows(TokenValidationCustomException.class, () -> tokenService.validateToken(validValidationRequest));
    }
}
