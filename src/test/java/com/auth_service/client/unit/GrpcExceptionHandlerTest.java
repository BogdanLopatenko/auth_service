package com.auth_service.client.unit;

import com.auth_service.exception.NoRpcStatusException;
import com.auth_service.exception.handler.GrpcExceptionHandler;
import com.auth_service.exception.user_service.UserNotFoundException;
import com.auth_service.exception.user_service.UserServiceException;
import com.google.protobuf.Any;
import com.user_service.generated.ErrorInfo;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.user_service.generated.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@ExtendWith(MockitoExtension.class)
public class GrpcExceptionHandlerTest {

    @InjectMocks
    private GrpcExceptionHandler handler;

    @Test
    void shouldReturnNoRpcStatusExceptionWhenRpcStatusIsNull() {

        StatusRuntimeException exception =
                Status.INTERNAL.asRuntimeException();

        RuntimeException result =
                handler.handleGrpcException(exception);

        assertInstanceOf(NoRpcStatusException.class, result);
    }

    @Test
    void shouldMapUserNotFoundException() {

        ErrorInfo errorInfo = ErrorInfo.newBuilder()
                .setErrorCode(USER_NOT_FOUND)
                .setMessage("User not found")
                .build();

        com.google.rpc.Status status =
                com.google.rpc.Status.newBuilder()
                        .addDetails(Any.pack(errorInfo))
                        .build();

        StatusRuntimeException grpcException =
                StatusProto.toStatusRuntimeException(status);

        RuntimeException result =
                handler.handleGrpcException(grpcException);

        assertInstanceOf(UserNotFoundException.class, result);
        assertEquals("User not found", result.getMessage());
    }

    @Test
    void shouldReturnValidationException() {

        com.google.rpc.BadRequest badRequest =
                com.google.rpc.BadRequest.newBuilder()
                        .build();

        com.google.rpc.Status status =
                com.google.rpc.Status.newBuilder()
                        .addDetails(Any.pack(badRequest))
                        .build();

        StatusRuntimeException grpcException =
                StatusProto.toStatusRuntimeException(status);

        RuntimeException result =
                handler.handleGrpcException(grpcException);

        assertInstanceOf(ValidationException.class, result);
    }

    @Test
    void shouldReturnUserServiceExceptionForUnknownErrorCode() {

        ErrorInfo errorInfo = ErrorInfo.newBuilder()
                .setErrorCode(ERROR_CODE_UNSPECIFIED)
                .build();

        com.google.rpc.Status status =
                com.google.rpc.Status.newBuilder()
                        .addDetails(Any.pack(errorInfo))
                        .build();

        StatusRuntimeException grpcException =
                StatusProto.toStatusRuntimeException(status);

        RuntimeException result =
                handler.handleGrpcException(grpcException);

        assertInstanceOf(UserServiceException.class, result);
    }


}
