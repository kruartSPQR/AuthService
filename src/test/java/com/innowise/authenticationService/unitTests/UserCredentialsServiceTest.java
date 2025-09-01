package com.innowise.authenticationService.unitTests;

import com.innowise.authenticationService.dto.UserCredentialsDto;
import com.innowise.authenticationService.entity.UserCredentials;
import com.innowise.authenticationService.exception.DuplicateResourceCustomException;
import com.innowise.authenticationService.exception.ResourceNotFoundCustomException;
import com.innowise.authenticationService.mapper.UserCredentialsDtoMapper;
import com.innowise.authenticationService.repository.UserCredentialsRepository;
import com.innowise.authenticationService.service.UserCredentialsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserCredentialsServiceTest {

    @Mock
    private UserCredentialsRepository userCredentialsRepository;

    @Mock
    private UserCredentialsDtoMapper userCredentialsDtoMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserCredentialsService userCredentialsService;

    private UserCredentialsDto userDto;
    private UserCredentials user;

    @BeforeEach
    void setUp() {
        userDto = new UserCredentialsDto("user@example.com", "password");
        user = new UserCredentials();
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");
        user.setRole("USER");
    }

    @Test
    void loadUserByUsernameMethodWithValidUserReturnsUser() {

        when(userCredentialsRepository.findByEmail("user@example.com"))
                .thenReturn(user);
        
        UserCredentials result = userCredentialsService.loadUserByUsername("user@example.com");
        
        assertNotNull(result);
        assertEquals("user@example.com", result.getEmail());
    }

    @Test
    void loadUserByUsernameMethodThrowsUserNotFoundException() {

        when(userCredentialsRepository.findByEmail("user@example.com"))
                .thenReturn(null);
        
        assertThrows(ResourceNotFoundCustomException.class, () -> userCredentialsService.loadUserByUsername("user@example.com"));
    }

    @Test
    void createUserReturnsUserDto() {

        when(userCredentialsRepository.findByEmail("user@example.com"))
                .thenReturn(null);
        when(passwordEncoder.encode("password"))
                .thenReturn("encodedPassword");
        when(userCredentialsDtoMapper.toDto(user))
                .thenReturn(userDto);
        when(userCredentialsRepository.save(any(UserCredentials.class)))
                .thenReturn(user);
        
        UserCredentialsDto result = userCredentialsService.createUser(userDto);
        
        assertNotNull(result);
        assertEquals("user@example.com", result.getEmail());
    }

    @Test
    void createUserThrowsDuplicateEmailException() {

        when(userCredentialsRepository.findByEmail("user@example.com"))
                .thenReturn(user);
        
        assertThrows(DuplicateResourceCustomException.class, () -> userCredentialsService.createUser(userDto));
    }

    @Test
    void generateClaimsMethodWithValidUserReturnsClaims() {

        when(userCredentialsRepository.findByEmail("user@example.com"))
                .thenReturn(user);
        
        Map<String, Object> claims = userCredentialsService.generateClaims("user@example.com");
        
        assertNotNull(claims);
        assertEquals("USER", claims.get("role"));
    }

    @Test
    void saveRefreshTokenMethodSavesToken() {

        when(userCredentialsRepository.findByEmail("user@example.com"))
                .thenReturn(user);
        
        userCredentialsService.saveRefreshToken("newRefreshToken", "user@example.com");
        
        assertEquals("newRefreshToken", user.getRefreshToken());
        verify(userCredentialsRepository).save(user);
    }
}
