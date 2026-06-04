package com.auth_service.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtConfigurationProperties
        (
                String secret,
                Integer accessExpirationTime,
                Integer refreshExpirationTime
        ) {}
