package com.auth_service.exception.user_service;

public class UserEmailConfirmationNotFoundException extends RuntimeException {
    public UserEmailConfirmationNotFoundException(String message) {
        super(message);
    }
}
