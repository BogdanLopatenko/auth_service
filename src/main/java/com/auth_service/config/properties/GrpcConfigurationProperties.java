package com.auth_service.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "grpc")
public record GrpcConfigurationProperties
        (
                Short timeoutDelay
        ) {
}
