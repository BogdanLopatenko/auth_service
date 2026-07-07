package com.auth_service.integration;

import com.auth_service.config.GrpcTestConfig;
import com.auth_service.dto.security.CustomUserDetails;
import com.auth_service.exception.TimeoutException;
import com.auth_service.exception.user_service.UserNotFoundException;
import com.auth_service.integration.server.TestGrpcUserServer;
import com.auth_service.security.service.impl.CustomUserDetailsServiceImpl;
import com.auth_service.util.GrpcTestUtil;
import com.google.rpc.Code;
import com.user_service.generated.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.Assert.*;

@ActiveProfiles("test")
@SpringBootTest
@Import(GrpcTestConfig.class)
public class CustomUserDetailsServiceImplTest {

    @Autowired
    private CustomUserDetailsServiceImpl userDetailsService;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        TestGrpcUserServer.reset();
    }

    @Test
    void shouldReturnUserByUsername() {

        String username = "someusername";

        CustomUserDetails result = userDetailsService.loadUserByUsername(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    @Test
    void shouldThrowUserNotFoundWhenUserNotExist() {

        String username = "someusername";

        TestGrpcUserServer.setForcedError(GrpcTestUtil.createStatusRuntimeException(
                Code.NOT_FOUND,
                ErrorCode.USER_NOT_FOUND,
                "User not found",
                "User not found by username" + username
        ));

        assertThrows(UserNotFoundException.class, () -> {

            userDetailsService.loadUserByUsername(username);
        });
    }

    @Test
    void shouldThrowTimeoutException() {

        TestGrpcUserServer.setForcedTimeout(true);

        assertThrows(TimeoutException.class, () -> {

            userDetailsService.loadUserByUsername("someusername");
        });
    }
}
