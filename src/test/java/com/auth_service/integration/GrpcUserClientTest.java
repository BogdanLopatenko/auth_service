package com.auth_service.integration;

import com.auth_service.client.UserClient;
import com.auth_service.config.GrpcTestConfig;
import com.auth_service.dto.UserAuthDto;
import com.auth_service.dto.UserRequestDto;
import com.auth_service.dto.UserResponseDto;
import com.auth_service.exception.TimeoutException;
import com.auth_service.exception.user_service.EmailAlreadyExistException;
import com.auth_service.exception.user_service.EmailConfirmationNotFoundException;
import com.auth_service.exception.user_service.EmailConfirmationTokenExpirationException;
import com.auth_service.exception.user_service.UserNotFoundException;
import com.auth_service.integration.server.TestGrpcUserServer;
import com.auth_service.util.UserTestBuilder;
import com.google.protobuf.Any;
import com.google.rpc.Code;
import com.user_service.generated.ErrorCode;
import com.user_service.generated.ErrorInfo;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ActiveProfiles("test")
@SpringBootTest
@Import(GrpcTestConfig.class)
public class GrpcUserClientTest {

    @Autowired
    private UserClient client;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        TestGrpcUserServer.reset();
    }


    @Test
    void shouldGetUserByUsername() {

        UserResponseDto user = new UserTestBuilder().buildResponseDto();

        UserAuthDto result = client.getByUsername(user.getUsername());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getUsername(), user.getUsername());
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserNotExistByUsername() {

        TestGrpcUserServer.setForcedError(createForcedGrpcException(
                Code.NOT_FOUND,
                ErrorCode.USER_NOT_FOUND,
                "UserNot Found in db",
                "User not found by username"
        ));

        assertThrows(UserNotFoundException.class, () -> {

            client.getByUsername("userNotFound");
        });
    }

    @Test
    void shouldThrowTimeoutExceptionWhenMethodExecutionTakesMuchTime() {

        TestGrpcUserServer.setForcedTimeout(true);

        assertThrows(TimeoutException.class, () -> {

            client.getByUsername("timeoutUser");
        });
    }

    @Test
    void shouldReturnUserByConfirmationToken() {

        UserResponseDto validUser = client.getUserByConfirmationToken("some valid token");

        assertNotNull(validUser);
    }

    @Test
    void shouldThrowEmailConfirmationNotFoundByTokenWhenNotExistInDb() {

        String invalidToken = "invalidToken";

        TestGrpcUserServer.setForcedError(createForcedGrpcException(
                Code.NOT_FOUND,
                ErrorCode.USER_EMAIL_CONFIRMATION_NOT_FOUND,
                "Email confirmation not found",
                "Email confirmation Not found ny token" + invalidToken
        ));

        assertThrows(EmailConfirmationNotFoundException.class, () -> {
            client.getUserByConfirmationToken(invalidToken);
        });
    }

    @Test
    void shouldCreateUserAndReturnResponseDto() {

        UserRequestDto request = new UserTestBuilder().buildRequestDto();

        UserResponseDto response = client.create(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(request.getUsername(), response.getUsername());
    }

    @Test
    void shouldThrowEmailAlreadyExistExceptionWhenTryingToCreate() {

        UserRequestDto request = new UserTestBuilder().buildRequestDto();

        TestGrpcUserServer.setForcedError(createForcedGrpcException(
                Code.ALREADY_EXISTS,
                ErrorCode.EMAIL_ALREADY_EXIST,
                "Email already exist in db",
                "Email already exist " + request.getEmail()
        ));

        assertThrows(EmailAlreadyExistException.class, () -> {

            client.create(request);
        });
    }

    @Test
    void shouldReturnEmailConfirmationTokenOnSuccess() {

        String validToken = client.generateEmailConfirmationToken(123L);

        assertNotNull(validToken);
        assertNotEquals("", validToken);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserNotExistInDb() {

        Long userId = 9999999L;

        TestGrpcUserServer.setForcedError(createForcedGrpcException(
                Code.NOT_FOUND,
                ErrorCode.USER_NOT_FOUND,
                "User not found",
                "User not found by id " + userId
        ));

        assertThrows(UserNotFoundException.class, () -> {

            client.generateEmailConfirmationToken(userId);
        });
    }

    @Test
    void shouldConfirmUserEmailWithoutErrors() {

        TestGrpcUserServer.lastReceivedToken = null;

        String validToken = "validToken";

        assertDoesNotThrow(() -> {

            client.confirmUserEmail(validToken);
        });

        assertNotNull(TestGrpcUserServer.lastReceivedToken);
        assertEquals(validToken, TestGrpcUserServer.lastReceivedToken.getToken());
    }

    @Test
    void shouldThrowEmailConfirmationTokenExpirationException() {

        TestGrpcUserServer.setForcedError(createForcedGrpcException(
                Code.UNAUTHENTICATED,
                ErrorCode.USER_EMAIL_CONFIRMATION_TOKEN_EXPIRED,
                "Token had been expired",
                "Token had been expired"
        ));

        assertThrows(EmailConfirmationTokenExpirationException.class, () -> {

            client.confirmUserEmail("sometoken");
        });
    }


    private StatusRuntimeException createForcedGrpcException(
            com.google.rpc.Code grpcCode,
            ErrorCode errorCode,
            String grpcMessage,
            String errorMessage
    ) {

        ErrorInfo errorInfo = ErrorInfo.newBuilder()
                .setErrorCode(errorCode)
                .setMessage(errorMessage)
                .build();

        com.google.rpc.Status rpcStatus = com.google.rpc.Status.newBuilder()
                .setCode(grpcCode.getNumber())
                .setMessage(grpcMessage)
                .addDetails(Any.pack(errorInfo))
                .build();

        return StatusProto.toStatusRuntimeException(rpcStatus);
    }


}
