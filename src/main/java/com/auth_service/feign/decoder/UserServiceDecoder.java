package com.auth_service.feign.decoder;

import com.auth_service.constant.ExceptionConstant;
import com.auth_service.dto.ErrorResponse;
import com.auth_service.exception.user_service.EmailAlreadyActivatedException;
import com.auth_service.exception.user_service.EmailConfirmationTokenExpirationException;
import com.auth_service.exception.user_service.UserCredentialsConflictException;
import com.auth_service.exception.user_service.UserEmailConfirmationNotFoundException;
import com.auth_service.exception.user_service.UserNotFoundException;
import com.auth_service.exception.user_service.UserServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserServiceDecoder implements ErrorDecoder {

    private final ObjectMapper mapper;

    @Override
    public Exception decode(String methodKey, Response response) {

        try(InputStream inputStream = response.body().asInputStream()){

            ErrorResponse error = mapper.readValue(inputStream, ErrorResponse.class);

            log.warn(
                    "UserService error: method={}, status={}, code={}",
                    methodKey,
                    response.status(),
                    error.getErrorCode()
            );

            return switch (error.getErrorCode()){
                case EMAIL_ALREADY_ACTIVATED -> new EmailAlreadyActivatedException(error.getMessage());
                case USER_NOT_FOUND -> new UserNotFoundException(error.getMessage());
                case EMAIL_ALREADY_EXIST, USERNAME_ALREADY_EXIST -> new UserCredentialsConflictException(error.getMessage());
                case USER_EMAIL_CONFIRMATION_NOT_FOUND -> new UserEmailConfirmationNotFoundException(error.getMessage());
                case USER_EMAIL_CONFIRMATION_TOKEN_EXPIRED -> new EmailConfirmationTokenExpirationException(error.getMessage());
                default -> new UserServiceException(error.getMessage());
            };
        } catch (Exception e){

            log.error("Failed to decode error from UserService", e);

            return new UserServiceException(ExceptionConstant.USER_SERVICE_UNEXPECTED);
        }
    }
}
