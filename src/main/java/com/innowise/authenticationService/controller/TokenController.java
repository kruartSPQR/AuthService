package com.innowise.authenticationService.controller;

import com.innowise.authenticationService.dto.RefreshTokenRequest;
import com.innowise.authenticationService.dto.TokenResponse;
import com.innowise.authenticationService.dto.TokenValidationRequest;
import com.innowise.authenticationService.service.TokenService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth/token")
@AllArgsConstructor
public class TokenController {

    private TokenService tokenService;

    @PostMapping("/validate")
    public ResponseEntity<Void> validateToken(@Valid @RequestBody TokenValidationRequest tokenValidationRequest) {
        tokenService.validateToken(tokenValidationRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest tokenRequest) {
        return new ResponseEntity<>(tokenService.refreshToken(tokenRequest), HttpStatus.OK);
    }
}

