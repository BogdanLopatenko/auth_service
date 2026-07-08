package com.auth_service.exception.handler;

import com.auth_service.constant.ExceptionConstant;
import com.auth_service.exception.NoRpcStatusException;
import com.auth_service.exception.TimeoutException;
import com.auth_service.exception.UnpackException;
import com.auth_service.exception.user_service.*;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.user_service.generated.ErrorInfo;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class GrpcExceptionHandler {

    public RuntimeException handleGrpcException(StatusRuntimeException exception) {

        if (exception.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {

            return new TimeoutException(ExceptionConstant.GRPC_RESPONSE_TIMEOUT);
        }

        com.google.rpc.Status rpcStatus = StatusProto.fromThrowable(exception);

        if (rpcStatus == null) {

            return new NoRpcStatusException(ExceptionConstant.NO_RPC_STATUS);
        }

        for (Any any : rpcStatus.getDetailsList()) {

            if (any.is(ErrorInfo.class)) {

                ErrorInfo info = unpack(any, ErrorInfo.class);

                return mapByErrorCode(info);
            } else if (any.is(com.google.rpc.BadRequest.class)) {
                com.google.rpc.BadRequest badRequest =
                        unpack(any, com.google.rpc.BadRequest.class);

                return new ValidationException(badRequest.toString());
            }
        }

        return new NoRpcStatusException(ExceptionConstant.NO_RPC_STATUS);
    }

    private <T extends Message> T unpack(Any any, Class<T> clazz) {

        try {
            return any.unpack(clazz);
        } catch (Exception e) {

            throw new UnpackException(ExceptionConstant.FAILED_TO_UNPACK);
        }
    }

    private RuntimeException mapByErrorCode(ErrorInfo info) {

        switch (info.getErrorCode()) {

            case USER_NOT_FOUND:
                return new UserNotFoundException(info.getMessage());
            case EMAIL_ALREADY_EXIST:
                return new EmailAlreadyExistException(info.getMessage());
            case EMAIL_ALREADY_ACTIVATED:
                return new EmailAlreadyActivatedException(info.getMessage());
            case USERNAME_ALREADY_EXIST:
                return new UsernameAlreadyExistException(info.getMessage());
            case EMAIL_CONFIRMATION_NOT_FOUND:
                return new EmailConfirmationNotFoundException(info.getMessage());
            case EMAIL_CONFIRMATION_TOKEN_EXPIRED:
                return new EmailConfirmationTokenExpirationException(info.getMessage());
            case TIMEOUT:
                return new TimeoutException(info.getMessage());
            default:
                return new UserServiceException(ExceptionConstant.USER_SERVICE_UNEXPECTED);
        }
    }
}
