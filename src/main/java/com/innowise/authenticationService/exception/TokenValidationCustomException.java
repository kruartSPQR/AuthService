package com.innowise.authenticationService.exception;

public class TokenValidationCustomException extends RuntimeException {
    public TokenValidationCustomException(String message) {
        super(message);
    }
    
    public TokenValidationCustomException(String message, Throwable cause) {
        super(message, cause);
    }
}
