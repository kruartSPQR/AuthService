package com.innowise.authenticationService.integrationTests;

import com.innowise.authenticationService.dto.RefreshTokenRequest;
import com.innowise.authenticationService.dto.TokenRequest;
import com.innowise.authenticationService.dto.TokenResponse;
import com.innowise.authenticationService.dto.TokenValidationRequest;
import com.innowise.authenticationService.entity.UserCredentials;
import com.innowise.authenticationService.service.TokenService;
import com.innowise.authenticationService.service.UserCredentialsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@Component
public class TokenServiceIntegrationTests extends BaseAuthServiceIntegrationTest{

    @Autowired
    UserCredentialsService userCredentialsService;

    @MockitoSpyBean
    TokenService tokenService;

    @Test
    @DirtiesContext
    public void shouldAuthenticateUser() {

        userCredentialsService.createUser(createUserCredentialsDto());
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setEmail("bob@gmail.com");
        tokenRequest.setPassword("password");

        doNothing().when(tokenService).saveToUserService(anyString(), anyString()); //ignoring relation with UserSvc

        TokenResponse tokenResponse = tokenService.authenticate(tokenRequest);

        Assertions.assertNotNull(tokenResponse);
        Assertions.assertNotNull(tokenResponse.getAccessToken());
        Assertions.assertNotNull(tokenResponse.getRefreshToken());
    }
    @Test
    @DirtiesContext
    public void shouldRefreshToken() {

        userCredentialsService.createUser(createUserCredentialsDto());
        UserCredentials user = userCredentialsService
                .loadUserByUsername("bob@gmail.com");
        user.setRefreshToken(refreshToken);
        userCredentialsService.saveRefreshToken(user.getRefreshToken(), user.getEmail());

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken(user.getRefreshToken());

        TokenResponse tokenResponse = tokenService.refreshToken(refreshTokenRequest);

        Assertions.assertNotNull(tokenResponse.getRefreshToken());
        Assertions.assertNotNull(tokenResponse.getAccessToken());
        Assertions.assertNotEquals(refreshToken, tokenResponse.getRefreshToken());
        Assertions.assertNotEquals(accessToken, tokenResponse.getAccessToken());

    }
    @Test
    @DirtiesContext
    public void shouldValidateToken() {
        userCredentialsService.createUser(createUserCredentialsDto());
        TokenValidationRequest accessTokenValidationRequest = new TokenValidationRequest();
        TokenValidationRequest refreshTokenValidationRequest = new TokenValidationRequest();
        accessTokenValidationRequest.setToken(accessToken);
        refreshTokenValidationRequest.setToken(refreshToken);

        tokenService.validateToken(accessTokenValidationRequest);

        tokenService.validateToken(refreshTokenValidationRequest);
    }

}
