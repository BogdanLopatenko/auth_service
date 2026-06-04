package com.auth_service.security.service.impl;

import com.auth_service.security.service.AuthHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class AuthHelperImpl implements AuthHelper {

    @Value("${spring.application.public-url}")
    private String publicUrl;


    @Override
    public String constructEmailConfirmationUrl(String token) {

        return UriComponentsBuilder
                .fromHttpUrl(publicUrl)
                .path("/auth/confirm-email")
                .queryParam("token", token)
                .toUriString();
    }

    @Override
    public String constructEmailTextWithUrl(String mailText, String reconfirmationUrl){

        return String.format(mailText, reconfirmationUrl);
    }
}
