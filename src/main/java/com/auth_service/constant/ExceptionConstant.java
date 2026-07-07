package com.auth_service.constant;

public final class ExceptionConstant {


    public static final String INVALID_REFRESH_TOKEN = "Invalid refresh token.";

    public static final String USER_SERVICE_UNEXPECTED = "User service unexpected exception";

    public static final String FIELD_CANT_BE_NULL = "Field can't be null.";

    public static final String NO_RPC_STATUS = "Rpc returned no status.";

    public static final String FAILED_TO_UNPACK = "Failed to unpack error details.";

    public static final String GRPC_RESPONSE_TIMEOUT = "Grpc Response timeout";


    private ExceptionConstant() {
    }
}
