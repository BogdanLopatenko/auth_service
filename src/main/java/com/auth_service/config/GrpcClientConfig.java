package com.auth_service.config;

import com.mailing_service.generated.MailingServiceGrpc;
import com.user_service.generated.UserServiceGrpc;
import io.grpc.Channel;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @GrpcClient(value = "user-service")
    private Channel userChannel;

    @GrpcClient(value = "mailing_service")
    private Channel mailingChannel;

    @Bean
    public UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub() {

        return UserServiceGrpc.newBlockingStub(userChannel);
    }

    @Bean
    public MailingServiceGrpc.MailingServiceBlockingStub mailingServiceBlockingStub() {

        return MailingServiceGrpc.newBlockingStub(mailingChannel);
    }
}
