package com.innowise.authenticationService.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCredentialsDto {
    @Email
    private String email;
    @NotBlank
    private String password;
}
