package com.auth_service.util;

import com.google.protobuf.Any;
import com.user_service.generated.ErrorCode;
import com.user_service.generated.ErrorInfo;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;

public class GrpcTestUtil {

    public static StatusRuntimeException createStatusRuntimeException(
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
