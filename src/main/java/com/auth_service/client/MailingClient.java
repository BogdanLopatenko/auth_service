package com.auth_service.client;

import com.auth_service.dto.MailDto;

public interface MailingClient {

    void sendMail(MailDto dto);
}
