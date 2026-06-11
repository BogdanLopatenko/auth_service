package com.auth_service.config;

import com.auth_service.feign.decoder.UserServiceDecoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserServiceClientConfig {

    @Bean
    public ErrorDecoder userServiceErrorDecoder(ObjectMapper objectMapper){
        return new UserServiceDecoder(objectMapper);
    }
}
