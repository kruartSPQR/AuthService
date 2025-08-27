package com.innowise.authenticationService.controller;

import com.innowise.authenticationService.dto.TokenRequest;
import com.innowise.authenticationService.dto.TokenResponse;
import com.innowise.authenticationService.dto.UserCredentialsDto;
import com.innowise.authenticationService.service.TokenService;
import com.innowise.authenticationService.service.UserCredentialsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/auth")
public class AuthController {

    private UserCredentialsService userCredentialsService;
    private TokenService tokenService;

    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<UserCredentialsDto> saveUserCredentials(@Valid @RequestBody UserCredentialsDto userCredentialsDto) {
        return new ResponseEntity<>(userCredentialsService.createUser(userCredentialsDto), HttpStatus.CREATED);
    }
    
    @PostMapping("/signin")
    public ResponseEntity<TokenResponse> signIn(@Valid @RequestBody TokenRequest tokenRequest) {
        return new ResponseEntity<>(tokenService.authenticate(tokenRequest), HttpStatus.OK);

    }
}
