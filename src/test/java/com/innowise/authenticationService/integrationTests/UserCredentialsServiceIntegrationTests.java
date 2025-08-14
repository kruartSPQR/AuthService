package com.innowise.authenticationService.integrationTests;


import com.innowise.authenticationService.dto.RefreshTokenRequest;
import com.innowise.authenticationService.dto.TokenRequest;
import com.innowise.authenticationService.dto.TokenResponse;
import com.innowise.authenticationService.dto.UserCredentialsDto;
import com.innowise.authenticationService.entity.UserCredentials;
import com.innowise.authenticationService.service.TokenService;
import com.innowise.authenticationService.service.UserCredentialsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

@Component
public class UserCredentialsServiceIntegrationTests extends BaseAuthServiceIntegrationTest {
    @Autowired
    TokenService tokenService;
    @Autowired
    UserCredentialsService userCredentialsService;

    @Test
    @DirtiesContext
    public void shouldCreateUserAndLoadUserCredentialsByUsername() {
        userCredentialsService.createUser(createUserCredentialsDto());

        UserCredentials user = userCredentialsService
                .loadUserByUsername("bob@gmail.com");

        Assertions.assertNotNull(user);
        Assertions.assertEquals("bob@gmail.com", user.getEmail());

    }
    @Test
    @DirtiesContext
    public void shouldGenerateClaims() {
        userCredentialsService.createUser(createUserCredentialsDto());

        Map<String,Object> claims = userCredentialsService
                .generateClaims("bob@gmail.com");

        Assertions.assertNotNull(claims);
        Assertions.assertNotNull(claims.get("role"));
        Assertions.assertEquals("USER", claims.get("role"));


    }
    @Test
    @DirtiesContext
    public void shouldSaveRefreshToken() {
        userCredentialsService.createUser(createUserCredentialsDto());

        userCredentialsService.saveRefreshToken(refreshToken
                ,"bob@gmail.com");

        UserCredentials user = userCredentialsService
                .loadUserByUsername("bob@gmail.com");
        String token = user.getRefreshToken();

        Assertions.assertNotNull(token);
        Assertions.assertEquals(refreshToken, token);
    }

}
