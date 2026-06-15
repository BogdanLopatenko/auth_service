package com.auth_service.exception.user_service;

public class EmailConfirmationTokenExpirationException extends RuntimeException {
    public EmailConfirmationTokenExpirationException(String message) {
        super(message);
    }
}
