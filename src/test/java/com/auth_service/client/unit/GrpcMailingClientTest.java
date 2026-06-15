package com.auth_service.client.unit;


import com.auth_service.client.grpc.GrpcMailingClient;
import com.auth_service.dto.MailDto;
import com.auth_service.mapper.MailProtoMapper;
import com.google.protobuf.Empty;
import com.mailing_service.generated.MailingServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GrpcMailingClientTest {

    @Mock
    private MailingServiceGrpc.MailingServiceBlockingStub mailingStub;

    @Mock
    private MailProtoMapper mailProtoMapper;

    @InjectMocks
    private GrpcMailingClient grpcMailingClient;

    @Test
    void sendMail_Success() {

        MailDto mailDto = new MailDto("someemail", "sometheme", "sometext");

        com.mailing_service.generated.MailDto protoMailDto = com.mailing_service.generated.MailDto.newBuilder()
                .setEmail("someemail")
                .setTheme("sometheme")
                .setText("sometext")
                .build();

        when(mailProtoMapper.toProtoDto(mailDto))
                .thenReturn(protoMailDto);

        when(mailingStub.sendMail(protoMailDto))
                .thenReturn(Empty.getDefaultInstance());

        assertDoesNotThrow(() -> grpcMailingClient.sendMail(mailDto));

        verify(mailProtoMapper).toProtoDto(mailDto);
        verify(mailingStub).sendMail(protoMailDto);
    }

    @Test
    void sendMail_WhenGrpcFails_ShouldThrowException() {

        MailDto mailDto =
                new MailDto("someemail", "sometheme", "sometext");

        com.mailing_service.generated.MailDto protoMailDto =
                com.mailing_service.generated.MailDto.newBuilder()
                        .build();

        when(mailProtoMapper.toProtoDto(mailDto))
                .thenReturn(protoMailDto);

        when(mailingStub.sendMail(protoMailDto))
                .thenThrow(
                        new StatusRuntimeException(Status.UNAVAILABLE)
                );

        assertThrows(
                StatusRuntimeException.class,
                () -> grpcMailingClient.sendMail(mailDto)
        );
    }

}
