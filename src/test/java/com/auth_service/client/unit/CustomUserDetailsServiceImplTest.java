package com.auth_service.client.unit;

import com.auth_service.client.grpc.GrpcUserClient;
import com.auth_service.dto.UserAuthDto;
import com.auth_service.dto.security.CustomUserDetails;
import com.auth_service.exception.user_service.UserNotFoundException;
import com.auth_service.security.service.impl.CustomUserDetailsServiceImpl;
import com.auth_service.util.UserTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceImplTest {

    @Mock
    private GrpcUserClient userClient;

    @InjectMocks
    private CustomUserDetailsServiceImpl userDetailsService;

    @Test
    void loadByUsername_Success_shouldLoadUserByUsername() {

        UserAuthDto dto = new UserTestBuilder().buildAuthDto();

        when(userClient.getByUsername(dto.getUsername()))
                .thenReturn(dto);

        CustomUserDetails result =
                userDetailsService.loadUserByUsername(dto.getUsername());

        assertNotNull(result);

        assertEquals(dto.getUsername(), result.getUsername());
        assertEquals(dto.getPassword(), result.getPassword());
        assertEquals(dto.getEmail(), result.getEmail());

        assertEquals(
                dto.getRole().name(),
                result.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
        );

        verify(userClient).getByUsername(dto.getUsername());
    }

    @Test
    void loadByUsername_UserNotFound_ThrowsException() {

        when(userClient.getByUsername("someusername"))
                .thenThrow(new UserNotFoundException("user not found by username"));

        assertThrows(UserNotFoundException.class, () -> {

            userDetailsService.loadUserByUsername("someusername");
        }, "Should throw UserNotFoundException when username not exist");

        verify(userClient).getByUsername("someusername");
    }
}

