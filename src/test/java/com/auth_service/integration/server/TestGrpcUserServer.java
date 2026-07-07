package com.auth_service.integration.server;

import com.auth_service.util.UserTestBuilder;
import com.google.protobuf.Empty;
import com.user_service.generated.*;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class TestGrpcUserServer extends UserServiceGrpc.UserServiceImplBase {

    public static volatile ConfirmationToken lastReceivedToken;


    private static final AtomicReference<Throwable> forcedError = new AtomicReference<>(null);
    private static final AtomicBoolean forcedTimeout = new AtomicBoolean(false);

    public static void setForcedError(Throwable status) {
        forcedError.set(status);
    }

    public static void setForcedTimeout(boolean timeout) {
        forcedTimeout.set(timeout);
    }


    public static void reset() {
        forcedError.set(null);
        forcedTimeout.set(false);
    }

    @Override
    public void getByUsername(Username request, StreamObserver<UserAuthDto> responseObserver) {

        if (forcedTimeout.get()) {

            createTimeoutDelay();
            return;
        }

        Throwable throwable = forcedError.get();
        if (throwable != null) {
            responseObserver.onError(throwable);
            return;
        }

        UserAuthDto userAuthDto = new UserTestBuilder().withUsername(request.getUsername()).buildProtoAuthDto();

        responseObserver.onNext(userAuthDto);
        responseObserver.onCompleted();
    }

    @Override
    public void getUserByConfirmationToken(ConfirmationToken request, StreamObserver<com.user_service.generated.UserResponseDto> responseObserver) {

        Throwable throwable = forcedError.get();
        if (throwable != null) {
            responseObserver.onError(throwable);
            return;
        }

        UserResponseDto userResponseDto = new UserTestBuilder().buildProtoResponseDto();

        responseObserver.onNext(userResponseDto);
        responseObserver.onCompleted();
    }

    @Override
    public void create(UserRequestDto request,
                       StreamObserver<com.user_service.generated.UserResponseDto> responseObserver) {

        Throwable throwable = forcedError.get();
        if (throwable != null) {
            responseObserver.onError(throwable);
            return;
        }

        UserResponseDto userResponseDto = new UserTestBuilder().buildProtoResponseDto();

        responseObserver.onNext(userResponseDto);
        responseObserver.onCompleted();
    }

    @Override
    public void generateEmailConfirmationToken(UserId request, StreamObserver<ConfirmationToken> responseObserver) {

        Throwable throwable = forcedError.get();
        if (throwable != null) {
            responseObserver.onError(throwable);
            return;
        }

        ConfirmationToken validToken = ConfirmationToken.newBuilder().setToken("validToken").build();

        responseObserver.onNext(validToken);
        responseObserver.onCompleted();
    }

    @Override
    public void confirmUserEmail(ConfirmationToken request, StreamObserver<Empty> responseObserver) {

        Throwable throwable = forcedError.get();
        if (throwable != null) {
            responseObserver.onError(throwable);
            return;
        }

        lastReceivedToken = request;

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    private void createTimeoutDelay() {

        try {

            Thread.sleep(5000);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }
    }
}
