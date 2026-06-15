package com.auth_service.exception.user_service;

public class UserCredentialsConflictException extends RuntimeException {
    public UserCredentialsConflictException(String message) {
        super(message);
    }
}
