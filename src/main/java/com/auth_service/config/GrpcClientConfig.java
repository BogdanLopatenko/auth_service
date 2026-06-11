package com.auth_service.config;

import com.user_service.generated.UserServiceGrpc;
import io.grpc.Channel;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @GrpcClient(value = "user-service")
    private Channel channel;

    public UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub() {

        return UserServiceGrpc.newBlockingStub(channel);
    }
}
