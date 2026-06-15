package com.auth_service.client.grpc;

import com.auth_service.client.UserClient;
import com.auth_service.dto.UserAuthDto;
import com.auth_service.dto.UserRequestDto;
import com.auth_service.dto.UserResponseDto;
import com.auth_service.exception.handler.GrpcExceptionHandler;
import com.auth_service.mapper.UserProtoMapper;
import com.user_service.generated.ConfirmationToken;
import com.user_service.generated.UserId;
import com.user_service.generated.UserServiceGrpc;
import com.user_service.generated.Username;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class GrpcUserClient implements UserClient {

    private final UserServiceGrpc.UserServiceBlockingStub userStub;

    private final UserProtoMapper userMapper;

    private final GrpcExceptionHandler exceptionHandler;


    @Override
    public UserAuthDto getByUsername(String username) {

        return execute(() -> {

            Username request = Username.newBuilder().setUsername(username).build();

            com.user_service.generated.UserAuthDto response = userStub.getByUsername(request);

            return userMapper.toAuthDtoFromProto(response);
        });
    }

    @Override
    public UserResponseDto getUserByConfirmationToken(String token) {

        return execute(() -> {

            ConfirmationToken request = ConfirmationToken.newBuilder().setToken(token).build();

            com.user_service.generated.UserResponseDto response = userStub.getUserByConfirmationToken(request);

            return userMapper.toResponseDtoFromProto(response);
        });
    }

    @Override
    public UserResponseDto create(UserRequestDto userRequestDto) {

        return execute(() -> {

            com.user_service.generated.UserRequestDto request = userMapper.toProtoRequestDto(userRequestDto);

            com.user_service.generated.UserResponseDto response = userStub.create(request);

            return userMapper.toResponseDtoFromProto(response);
        });
    }

    @Override
    public String generateEmailConfirmationToken(Long userId) {

        return execute(() -> {

            UserId request = UserId.newBuilder().setId(userId).build();

            ConfirmationToken confirmationToken = userStub.generateEmailVerificationToken(request);

            return confirmationToken.getToken();
        });
    }

    @Override
    public void verifyUserEmail(String token) {

        execute(() -> {

            ConfirmationToken request = ConfirmationToken.newBuilder().setToken(token).build();

            return userStub.verifyUserEmail(request);
        });
    }

    private <T> T execute(Supplier<T> call) {

        try {

            return call.get();

        } catch (StatusRuntimeException e) {

            throw exceptionHandler.handleGrpcException(e);
        }
    }
}
