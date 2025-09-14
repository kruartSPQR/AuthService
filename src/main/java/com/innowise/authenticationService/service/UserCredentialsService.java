package com.innowise.authenticationService.service;

import com.innowise.authenticationService.dto.UserCredentialsDto;
import com.innowise.authenticationService.entity.UserCredentials;
import com.innowise.authenticationService.mapper.UserCredentialsDtoMapper;
import com.innowise.authenticationService.repository.UserCredentialsRepository;
import com.innowise.common.exception.DuplicateResourceCustomException;
import com.innowise.common.exception.ResourceNotFoundCustomException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@AllArgsConstructor

public class UserCredentialsService implements UserDetailsService {

    UserCredentialsRepository  userCredentialsRepository;
    UserCredentialsDtoMapper  userCredentialsDtoMapper;
    PasswordEncoder passwordEncoder;


    @Override
    public UserCredentials loadUserByUsername(String username) {
        UserCredentials user = userCredentialsRepository.findByEmail(username);
        if (user == null) {
            throw new ResourceNotFoundCustomException("User not found: " + username);
        }
        return user;
    }
    public UserCredentialsDto createUser(UserCredentialsDto request)
    {
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setEmail(request.getEmail());

        if (userCredentialsRepository.findByEmail(request.getEmail()) != null) {
            throw new DuplicateResourceCustomException("Email already in use: " + request.getEmail());
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        userCredentials.setPassword(hashedPassword);
        userCredentials.setRole("USER");

        userCredentialsRepository.save(userCredentials);
        return userCredentialsDtoMapper.toDto(userCredentials);
    }

    public Map<String, Object> generateClaims(String email) {
        UserCredentials userCredentials = userCredentialsRepository.findByEmail(email);
        if (userCredentials == null) {
            throw new ResourceNotFoundCustomException("User not found: " + email);
        }
        Map<String, Object> claims = Map.of(
                "role" ,userCredentials.getRole()

        );
        return claims;
    }
    public void saveRefreshToken(String refreshToken, String email) {
        UserCredentials userCredentials = userCredentialsRepository.findByEmail(email);
        if (userCredentials == null) {
            throw new ResourceNotFoundCustomException("User not found: " + email);
        }
        userCredentials.setRefreshToken(refreshToken);
        userCredentialsRepository.save(userCredentials);
    }
}
