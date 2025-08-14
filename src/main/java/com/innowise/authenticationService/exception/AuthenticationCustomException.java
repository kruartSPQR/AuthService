package com.innowise.authenticationService.exception;

public class AuthenticationCustomException extends RuntimeException {
    public AuthenticationCustomException(String message) {
        super(message);
    }
    
    public AuthenticationCustomException(String message, Throwable cause) {
        super(message, cause);
    }
}
