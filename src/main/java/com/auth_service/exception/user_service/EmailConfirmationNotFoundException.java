package com.auth_service.exception.user_service;

public class EmailConfirmationNotFoundException extends RuntimeException {
    public EmailConfirmationNotFoundException(String message) {
        super(message);
    }
}
