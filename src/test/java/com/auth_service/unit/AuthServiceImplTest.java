package com.auth_service.unit;

import com.auth_service.client.UserClient;
import com.auth_service.config.properties.JwtConfigurationProperties;
import com.auth_service.dto.MailDto;
import com.auth_service.dto.UserRequestDto;
import com.auth_service.dto.UserResponseDto;
import com.auth_service.dto.security.AuthRequest;
import com.auth_service.dto.security.AuthResponse;
import com.auth_service.dto.security.CustomUserDetails;
import com.auth_service.exception.InvalidRefreshTokenException;
import com.auth_service.producer.MailProducer;
import com.auth_service.security.service.AuthHelper;
import com.auth_service.security.service.JwtService;
import com.auth_service.security.service.impl.AuthServiceImpl;
import com.auth_service.security.service.impl.CustomUserDetailsServiceImpl;
import com.auth_service.util.UserTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private JwtConfigurationProperties jwtProperties;

    @Mock
    private UserClient userClient;

    @Mock
    private AuthHelper authHelper;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsServiceImpl userDetailsService;

    @Mock
    private MailProducer mailProducer;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_ShouldRegisterUser_AndReturnTokens() {

        UserRequestDto request = new UserTestBuilder().buildRequestDto();
        UserResponseDto createdUser = new UserTestBuilder().buildResponseDto();
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(encoder.encode(anyString())).thenReturn("encoded-password");

        when(userClient.create(any(UserRequestDto.class)))
                .thenReturn(createdUser);

        when(userClient.generateEmailConfirmationToken(anyLong()))
                .thenReturn("confirmation-token");

        when(authHelper.constructEmailConfirmationUrl(anyString()))
                .thenReturn("http://localhost/confirm");

        when(authHelper.constructEmailTextWithUrl(any(), any()))
                .thenReturn("email-text");

        when(userDetailsService.loadUserByUsername(anyString()))
                .thenReturn(userDetails);

        when(jwtProperties.accessExpirationTime())
                .thenReturn(1000);

        when(jwtProperties.refreshExpirationTime())
                .thenReturn(2000);

        when(jwtService.generateToken(userDetails, 1000))
                .thenReturn("access-token");

        when(jwtService.generateToken(userDetails, 2000))
                .thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(encoder).encode(anyString());
        verify(userClient).create(any(UserRequestDto.class));
        verify(userClient).generateEmailConfirmationToken(anyLong());
        verify(mailProducer).sendMail(any(MailDto.class));
        verify(userDetailsService).loadUserByUsername(request.getUsername());
    }

    @Test
    void login_ShouldAuthenticateUser_AndReturnTokens() {

        AuthRequest request = new AuthRequest("john", "password");

        Authentication authentication = mock(Authentication.class);

        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        when(jwtProperties.accessExpirationTime())
                .thenReturn(1000);

        when(jwtProperties.refreshExpirationTime())
                .thenReturn(2000);

        when(jwtService.generateToken(userDetails, 1000))
                .thenReturn("access-token");

        when(jwtService.generateToken(userDetails, 2000))
                .thenReturn("refresh-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void refreshToken_ShouldGenerateNewTokens_WhenRefreshTokenValid() {

        String refreshToken = "refresh-token";

        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(jwtService.extractUsername(refreshToken))
                .thenReturn("john");

        when(userDetailsService.loadUserByUsername("john"))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid(refreshToken))
                .thenReturn(true);

        when(jwtProperties.accessExpirationTime())
                .thenReturn(1000);

        when(jwtProperties.refreshExpirationTime())
                .thenReturn(2000);

        when(jwtService.generateToken(userDetails, 1000))
                .thenReturn("new-access-token");

        when(jwtService.generateToken(userDetails, 2000))
                .thenReturn("new-refresh-token");

        AuthResponse response = authService.refreshToken(refreshToken);

        assertNotNull(response);

        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
    }

    @Test
    void refreshToken_ShouldThrowException_WhenRefreshTokenInvalid() {

        String refreshToken = "invalid-token";

        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(jwtService.extractUsername(refreshToken))
                .thenReturn("john");

        when(userDetailsService.loadUserByUsername("john"))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid(refreshToken))
                .thenReturn(false);

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> authService.refreshToken(refreshToken)
        );
    }
}
