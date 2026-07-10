package com.auth_service.unit;

import com.auth_service.config.properties.GrpcConfigurationProperties;
import com.auth_service.enums.UserStatus;
import com.auth_service.exception.handler.GrpcExceptionHandler;
import com.auth_service.grpc.GrpcUserClient;
import com.auth_service.mapper.UserProtoMapper;
import com.auth_service.util.UserTestBuilder;
import com.google.protobuf.Empty;
import com.user_service.generated.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class GrpcUserClientTest {

    @Mock
    private UserServiceGrpc.UserServiceBlockingStub userStub;

    @Mock
    private UserProtoMapper userMapper;

    @Mock
    private GrpcExceptionHandler exceptionHandler;

    private final GrpcConfigurationProperties grpcConfigurationProperties = new GrpcConfigurationProperties((short) 1);

    private GrpcUserClient grpcUserClient;

    @BeforeEach
    void setUp() {

        this.grpcUserClient = new GrpcUserClient(
                userStub,
                userMapper,
                exceptionHandler,
                grpcConfigurationProperties
        );

        when(userStub.withDeadlineAfter(anyLong(), any(TimeUnit.class)))
                .thenReturn(userStub);
    }

    @Test
    void shouldReturnAuthDtoWhenUserExists() {

        UserAuthDto userProtoAuthDto = new UserTestBuilder().buildProtoAuthDto();

        com.auth_service.dto.UserAuthDto userAuthDto = new UserTestBuilder().buildAuthDto();

        when(userStub.getByUsername(any()))
                .thenReturn(userProtoAuthDto);

        when(userMapper.toAuthDtoFromProto(userProtoAuthDto))
                .thenReturn(userAuthDto);

        com.auth_service.dto.UserAuthDto someusername = grpcUserClient.getByUsername("someusername");

        assertEquals(someusername.getUsername(), userProtoAuthDto.getUsername());

        verify(exceptionHandler, never()).handleGrpcException(any());
        verify(userStub).getByUsername(any());
        verify(userMapper)
                .toAuthDtoFromProto(userProtoAuthDto);
    }

    @Test
    void shouldBuildCorrectUsernameRequestWhenGetByUsername() {

        when(userStub.getByUsername(any()))
                .thenReturn(
                        com.user_service.generated.UserAuthDto
                                .getDefaultInstance()
                );

        when(userMapper.toAuthDtoFromProto(any()))
                .thenReturn(new com.auth_service.dto.UserAuthDto());

        grpcUserClient.getByUsername("john");

        ArgumentCaptor<Username> captor =
                ArgumentCaptor.forClass(Username.class);

        verify(userStub).getByUsername(captor.capture());

        Username request = captor.getValue();

        assertEquals("john", request.getUsername());
    }

    @Test
    void shouldHandleGrpcExceptionWhenGetByUsernameFails() {

        StatusRuntimeException grpcException =
                new StatusRuntimeException(Status.NOT_FOUND);

        RuntimeException mappedException =
                new RuntimeException("User not found");

        when(userStub.getByUsername(any()))
                .thenThrow(grpcException);

        when(exceptionHandler.handleGrpcException(grpcException))
                .thenReturn(mappedException);

        RuntimeException result = assertThrows(
                RuntimeException.class,
                () -> grpcUserClient.getByUsername("john")
        );

        assertEquals("User not found", result.getMessage());

        verify(exceptionHandler)
                .handleGrpcException(grpcException);

        verify(userMapper, never())
                .toAuthDtoFromProto(any());
    }

    @Test
    void shouldReturnUserResponseDtoWhenConfirmationTokenExists() {

        UserResponseDto userProtoResponseDto = new UserTestBuilder().buildProtoResponseDto();
        com.auth_service.dto.UserResponseDto userResponseDto = new UserTestBuilder().buildResponseDto();
        ConfirmationToken confirmationToken = ConfirmationToken.newBuilder().build();

        when(userStub.getUserByConfirmationToken(confirmationToken))
                .thenReturn(userProtoResponseDto);

        when(userMapper.toResponseDtoFromProto(userProtoResponseDto))
                .thenReturn(userResponseDto);

        com.auth_service.dto.UserResponseDto someUser = grpcUserClient.getUserByConfirmationToken(confirmationToken.getToken());

        assertEquals(someUser.getId(), userResponseDto.getId());

        verify(userMapper).toResponseDtoFromProto(userProtoResponseDto);
        verify(userStub).getUserByConfirmationToken(confirmationToken);
        verify(exceptionHandler, never()).handleGrpcException(any());
    }

    @Test
    void shouldBuildCorrectConfirmationTokenRequestWhenGetUserByConfirmationToken() {

        String token = "some token";

        when(userStub.getUserByConfirmationToken(any()))
                .thenReturn(UserResponseDto.getDefaultInstance());

        when(userMapper.toResponseDtoFromProto(any()))
                .thenReturn(new com.auth_service.dto.UserResponseDto());

        grpcUserClient.getUserByConfirmationToken(token);

        ArgumentCaptor<ConfirmationToken> captor =
                ArgumentCaptor.forClass(ConfirmationToken.class);

        verify(userStub).getUserByConfirmationToken(captor.capture());

        ConfirmationToken request = captor.getValue();

        assertEquals(token, request.getToken());
    }

    @Test
    void shouldReturnResponseDtoWhenUserIsCreated() {

        com.auth_service.dto.UserRequestDto userRequestDto = new UserTestBuilder().buildRequestDto();

        UserRequestDto userProtoRequestDto = new UserTestBuilder().buildProtoRequestDto();

        UserResponseDto userProtoResponseDto = new UserTestBuilder().buildProtoResponseDto();

        com.auth_service.dto.UserResponseDto responseDto = new UserTestBuilder().buildResponseDto();

        when(userMapper.toProtoRequestDto(any()))
                .thenReturn(userProtoRequestDto);

        when(userStub.create(userProtoRequestDto))
                .thenReturn(userProtoResponseDto);

        when(userMapper.toResponseDtoFromProto(userProtoResponseDto))
                .thenReturn(responseDto);

        com.auth_service.dto.UserResponseDto result = grpcUserClient.create(userRequestDto);

        assertEquals(result.getEmail(), userRequestDto.getEmail());
        assertNotNull(result.getId());
        assertEquals(result.getStatus(), UserStatus.NEED_EMAIL_CONFIRMATION);

        verify(userMapper).toProtoRequestDto(userRequestDto);
        verify(userStub).create(userProtoRequestDto);
        verify(userMapper).toResponseDtoFromProto(userProtoResponseDto);
        verify(exceptionHandler, never()).handleGrpcException(any());
    }

    @Test
    void shouldBuildCorrectCreateUserRequestWhenCreate() {

        com.auth_service.dto.UserRequestDto userRequestDto = new UserTestBuilder().buildRequestDto();
        UserRequestDto requestDto1 = new UserTestBuilder().buildProtoRequestDto();

        when(userMapper.toProtoRequestDto(any()))
                .thenReturn(requestDto1);

        when(userStub.create(any()))
                .thenReturn(UserResponseDto.getDefaultInstance());

        when(userMapper.toResponseDtoFromProto(any()))
                .thenReturn(new com.auth_service.dto.UserResponseDto());

        grpcUserClient.create(userRequestDto);

        ArgumentCaptor<UserRequestDto> captor = ArgumentCaptor.forClass(UserRequestDto.class);

        verify(userStub).create(captor.capture());

        UserRequestDto requestDto = captor.getValue();

        assertEquals(requestDto.getEmail(), userRequestDto.getEmail());
        assertEquals(requestDto.getUsername(), userRequestDto.getUsername());
    }

    @Test
    void shouldReturnTokenWhenGenerateEmailConfirmationToken() {

        UserId userId = new UserTestBuilder().buildUserId();

        ConfirmationToken confirmationToken = ConfirmationToken.newBuilder().build();

        when(userStub.generateEmailConfirmationToken(userId))
                .thenReturn(confirmationToken);

        String token = grpcUserClient.generateEmailConfirmationToken(userId.getId());

        assertNotNull(token);

        verify(exceptionHandler, never()).handleGrpcException(any());
    }

    @Test
    void shouldBuildCorrectUserIdRequestWhenGenerateEmailConfirmationToken() {

        Long userid = 2L;

        UserId userId = new UserTestBuilder().withId(2L).buildUserId();

        when(userStub.generateEmailConfirmationToken(userId))
                .thenReturn(ConfirmationToken.getDefaultInstance());

        grpcUserClient.generateEmailConfirmationToken(userid);

        ArgumentCaptor<UserId> captor = ArgumentCaptor.forClass(UserId.class);

        verify(userStub).generateEmailConfirmationToken(captor.capture());

        UserId request = captor.getValue();

        assertEquals(request.getId(), userid);
    }

    @Test
    void shouldChangeUserStatusWhenConfirmUserEmail() {

        ConfirmationToken confirmationToken = ConfirmationToken.newBuilder().setToken("token").build();

        when(userStub.confirmUserEmail(confirmationToken))
                .thenReturn(Empty.getDefaultInstance());

        grpcUserClient.confirmUserEmail(confirmationToken.getToken());

        verify(exceptionHandler, never()).handleGrpcException(any());
        verify(userStub).confirmUserEmail(confirmationToken);
    }

    @Test
    void shouldBuildCorrectConfirmationTokenRequestWhenConfirmUserEmail() {

        String token = "token";

        ConfirmationToken confirmationToken = ConfirmationToken.newBuilder().setToken(token).build();

        when(userStub.confirmUserEmail(confirmationToken))
                .thenReturn(Empty.newBuilder().build());

        grpcUserClient.confirmUserEmail(token);

        ArgumentCaptor<ConfirmationToken> captor = ArgumentCaptor.forClass(ConfirmationToken.class);

        verify(userStub).confirmUserEmail(captor.capture());

        ConfirmationToken value = captor.getValue();

        assertEquals(value.getToken(), token);
    }


}
