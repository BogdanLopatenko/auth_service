package com.auth_service.client.unit;

import com.auth_service.client.grpc.GrpcUserClient;
import com.auth_service.enums.UserStatus;
import com.auth_service.exception.handler.GrpcExceptionHandler;
import com.auth_service.mapper.UserProtoMapper;
import com.auth_service.util.UserTestBuilder;
import com.google.protobuf.Empty;
import com.user_service.generated.ConfirmationToken;
import com.user_service.generated.UserAuthDto;
import com.user_service.generated.UserId;
import com.user_service.generated.UserRequestDto;
import com.user_service.generated.UserResponseDto;
import com.user_service.generated.UserServiceGrpc;
import com.user_service.generated.Username;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private GrpcUserClient grpcUserClient;

    @Test
    void getByUsername_Success_ReturnsAuthDto() {

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
    void getByUsername_ShouldBuildCorrectRequest() {

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
    void getByUsername_WhenGrpcThrows_ShouldHandleException() {

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
    void getUserByConfirmationToken_Success_ResponseDto() {

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
    void getUserByConfirmationToken_ShouldBuildCorrectRequest() {

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
    void create_Success_ReturnsResponseDto() {

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
    void create_ShouldBuildCorrectRequest() {

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
    void generateEmailConfirmationToken_Success_ReturnsToken() {

        UserId userId = new UserTestBuilder().buildUserId();

        ConfirmationToken confirmationToken = ConfirmationToken.newBuilder().build();

        when(userStub.generateEmailVerificationToken(userId))
                .thenReturn(confirmationToken);

        String token = grpcUserClient.generateEmailConfirmationToken(userId.getId());

        assertNotNull(token);

        verify(exceptionHandler, never()).handleGrpcException(any());
    }

    @Test
    void generateEmailConfirmationToken_ShouldBuildCorrectRequest() {

        Long userid = 2L;

        UserId userId = new UserTestBuilder().withId(2L).buildUserId();

        when(userStub.generateEmailVerificationToken(userId))
                .thenReturn(ConfirmationToken.getDefaultInstance());

        grpcUserClient.generateEmailConfirmationToken(userid);

        ArgumentCaptor<UserId> captor = ArgumentCaptor.forClass(UserId.class);

        verify(userStub).generateEmailVerificationToken(captor.capture());

        UserId request = captor.getValue();

        assertEquals(request.getId(), userid);
    }

    @Test
    void verifyUserEmail_Success_ShouldChangeUserStatus(){

        ConfirmationToken confirmationToken = ConfirmationToken.newBuilder().setToken("token").build();

        when(userStub.verifyUserEmail(confirmationToken))
                .thenReturn(any());

        grpcUserClient.verifyUserEmail(confirmationToken.getToken());

        verify(exceptionHandler, never()).handleGrpcException(any());
        verify(userStub).verifyUserEmail(confirmationToken);
    }

    @Test
    void verifyUserEmail_ShouldBuildCorrectRequest() {

        String token = "token";

        ConfirmationToken confirmationToken = ConfirmationToken.newBuilder().setToken(token).build();

        when(userStub.verifyUserEmail(confirmationToken))
                .thenReturn(Empty.newBuilder().build());

        grpcUserClient.verifyUserEmail(token);

        ArgumentCaptor<ConfirmationToken> captor = ArgumentCaptor.forClass(ConfirmationToken.class);

        verify(userStub).verifyUserEmail(captor.capture());

        ConfirmationToken value = captor.getValue();

        assertEquals(value.getToken(), token);
    }


}
