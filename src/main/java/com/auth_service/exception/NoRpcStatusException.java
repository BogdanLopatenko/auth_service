package com.auth_service.exception;

public class NoRpcStatusException extends RuntimeException {
    public NoRpcStatusException(String message) {
        super(message);
    }
}
