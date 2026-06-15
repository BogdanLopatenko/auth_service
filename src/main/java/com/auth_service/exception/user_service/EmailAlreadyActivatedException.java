package com.auth_service.exception.user_service;

public class EmailAlreadyActivatedException extends RuntimeException {
    public EmailAlreadyActivatedException(String message) {
        super(message);
    }
}
