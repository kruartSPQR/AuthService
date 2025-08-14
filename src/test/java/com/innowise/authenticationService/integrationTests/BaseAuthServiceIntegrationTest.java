package com.innowise.authenticationService.integrationTests;

import com.innowise.authenticationService.dto.UserCredentialsDto;
import com.innowise.authenticationService.service.TokenService;
import com.innowise.authenticationService.service.UserCredentialsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseAuthServiceIntegrationTest {
    public String accessToken = "eyJhbGciOiJIUzM4NCJ9.eyJyb2xlIjoiVVNFUiIsInN1YiI6ImJvYkBnbWFpbC5jb20iLCJpYXQiOjE3NTUxNjMyMTAsImV4cCI6MTc4MTA4MzIxMH0.O-uk8bYTHz0h6KXZ06_oIltXK8sGVfUTB9MFpU81g768jUKdBigsm-miLRLyOjsG";
    public String refreshToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJib2JAZ21haWwuY29tIiwiaWF0IjoxNzU1MTYzMjEwLCJleHAiOjE3ODEwODMyMTB9.SK0_RlNqO-zU2L6yCgVUpPoi53WsHRsC92vMRUXB7VC0z_kZbGjmQxc8OurKtls-lgNWU5Ku-loUOz-TXTK-XQ";

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testDb")
            .withUsername("testUser")
            .withPassword("testPassword");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    public UserCredentialsDto createUserCredentialsDto() {
        UserCredentialsDto userCredentialsDto = new UserCredentialsDto();
        userCredentialsDto.setEmail("bob@gmail.com");
        userCredentialsDto.setPassword("password");

        return userCredentialsDto;
    }

}
