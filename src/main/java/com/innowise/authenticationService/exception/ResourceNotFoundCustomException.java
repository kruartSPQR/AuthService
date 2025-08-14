package com.innowise.authenticationService.exception;

public class ResourceNotFoundCustomException extends RuntimeException {
    public ResourceNotFoundCustomException(String message) {
        super(message);
    }
}
