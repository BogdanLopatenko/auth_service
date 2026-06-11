package com.auth_service.client.grpc;

import com.auth_service.client.MailingClient;
import com.auth_service.dto.MailDto;
import com.auth_service.mapper.MailProtoMapper;
import com.mailing_service.generated.MailingServiceGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GrpcMailingClient implements MailingClient {

    private final MailingServiceGrpc.MailingServiceBlockingStub mailingStub;

    private final MailProtoMapper mailProtoMapper;


    @Override
    public void sendMail(MailDto dto) {

        log.info("Trying to send mail");

        com.mailing_service.generated.MailDto mailDto = mailProtoMapper.toProtoDto(dto);

        mailingStub.sendMail(mailDto);

        log.info("Mail was successfully sent");
    }
}
