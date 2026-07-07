package com.auth_service.config;

import com.auth_service.integration.server.TestGrpcUserServer;
import io.grpc.Server;
import io.grpc.inprocess.InProcessServerBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@TestConfiguration
public class GrpcTestConfig {


    @Bean(destroyMethod = "shutdown")
    public Server inProcessServer() throws IOException {
        return InProcessServerBuilder.forName("test-channel")
                .addService(new TestGrpcUserServer())
                .build()
                .start();
    }
}
