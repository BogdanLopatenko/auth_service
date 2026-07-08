package com.auth_service.exception.handler;

import com.auth_service.dto.ErrorResponse;
import com.auth_service.enums.ErrorCode;
import com.auth_service.exception.InvalidRefreshTokenException;
import com.auth_service.exception.mail_service.EmailNotFoundException;
import com.auth_service.exception.user_service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshTokenException(InvalidRefreshTokenException exception) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(
                ErrorCode.INVALID_REFRESH_TOKEN,
                exception.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                Instant.now()));
    }

    @ExceptionHandler(UserCredentialsConflictException.class)
    public ResponseEntity<ErrorResponse> handleRegistrationDetailsConflictException(UserCredentialsConflictException exception) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                ErrorCode.USER_CREDENTIALS_CONFLICT,
                exception.getMessage(),
                HttpStatus.CONFLICT.value(),
                Instant.now()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                ErrorCode.USER_NOT_FOUND,
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                Instant.now()));
    }

    @ExceptionHandler(EmailConfirmationTokenExpirationException.class)
    public ResponseEntity<ErrorResponse> handleEmailConfirmationTokenExpirationException(EmailConfirmationTokenExpirationException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                ErrorCode.EMAIL_CONFIRMATION_TOKEN_EXPIRED,
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                Instant.now()));
    }

    @ExceptionHandler(EmailAlreadyActivatedException.class)
    public ResponseEntity<ErrorResponse> handleEmailIsAlreadyActivatedException(EmailAlreadyActivatedException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                ErrorCode.EMAIL_ALREADY_ACTIVATED,
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                Instant.now()));
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEEmailNotFoundException(EmailNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                ErrorCode.EMAIL_NOT_FOUND,
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                Instant.now()));
    }

    @ExceptionHandler(EmailConfirmationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserEmailConfirmationNotFoundException(EmailConfirmationNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                ErrorCode.EMAIL_CONFIRMATION_NOT_FOUND,
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                Instant.now()));
    }

    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<ErrorResponse> handleUserServiceException(UserServiceException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                ErrorCode.USER_SERVICE_UNEXPECTED_ERROR,
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                Instant.now()));
    }
}
