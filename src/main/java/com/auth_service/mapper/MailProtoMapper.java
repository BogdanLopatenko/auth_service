package com.auth_service.mapper;

import com.auth_service.dto.MailDto;

public class MailProtoMapper {


    public com.mailing_service.generated.MailDto toProtoDto(MailDto dto){

        return com.mailing_service.generated.MailDto.newBuilder()
                .setEmail(dto.getEmail())
                .setText(dto.getText())
                .setTheme(dto.getTheme())
                .build();
    }

}
