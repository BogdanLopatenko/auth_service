package com.auth_service.integration;


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
import com.auth_service.security.service.JwtService;
import com.auth_service.security.service.impl.AuthServiceImpl;
import com.auth_service.security.service.impl.CustomUserDetailsServiceImpl;
import com.auth_service.util.UserTestBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class AuthServiceImplIT {

    @Autowired
    private AuthServiceImpl authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserClient userClient;

    @MockBean
    private MailProducer mailProducer;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private CustomUserDetailsServiceImpl userDetailsService;

    private final JwtConfigurationProperties jwtConfigurationProperties = new JwtConfigurationProperties(
            "secret",
            3600000,
            604800000
    );

    @Test
    void shouldRegisterUserSuccessfully() {

        UserRequestDto request = new UserTestBuilder().buildRequestDto();

        UserResponseDto createdUser = new UserTestBuilder().buildResponseDto();

        CustomUserDetails userDetails = new UserTestBuilder().buildCustomUserDetails();

        when(userClient.create(any(UserRequestDto.class)))
                .thenReturn(createdUser);


        when(userClient.generateEmailConfirmationToken(1L))
                .thenReturn("confirmation-token");


        when(userDetailsService.loadUserByUsername(createdUser.getUsername()))
                .thenReturn(userDetails);

        when(jwtService.generateToken(
                any(CustomUserDetails.class),
                eq(jwtConfigurationProperties.accessExpirationTime())
        ))
                .thenReturn("access-token");


        when(jwtService.generateToken(
                any(CustomUserDetails.class),
                eq(jwtConfigurationProperties.refreshExpirationTime())
        ))
                .thenReturn("refresh-token");


        AuthResponse response =
                authService.register(request);


        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertThat(response)
                .isNotNull();

        assertThat(response.getAccessToken())
                .isNotBlank();

        assertThat(response.getRefreshToken())
                .isNotBlank();


        verify(userClient)
                .create(any(UserRequestDto.class));

        verify(userClient)
                .generateEmailConfirmationToken(1L);


        verify(mailProducer)
                .sendMail(any(MailDto.class));
    }


    @Test
    void shouldLoginSuccessfully() {

        AuthRequest authRequest = new UserTestBuilder().buildAuthRequestDto();

        CustomUserDetails userDetails = new UserTestBuilder().buildCustomUserDetails();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );


        when(jwtService.generateToken(
                any(CustomUserDetails.class),
                eq(jwtConfigurationProperties.accessExpirationTime())
        ))
                .thenReturn("access-token");


        when(jwtService.generateToken(
                any(CustomUserDetails.class),
                eq(jwtConfigurationProperties.refreshExpirationTime())
        ))
                .thenReturn("refresh-token");

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);


        AuthResponse response =
                authService.login(authRequest);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());


        assertThat(response)
                .isNotNull();

        assertThat(response.getAccessToken())
                .isNotBlank();

        assertThat(response.getRefreshToken())
                .isNotBlank();


        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
    }


    @Test
    void shouldRefreshTokenSuccessfully() {

        CustomUserDetails userDetails = new UserTestBuilder().buildCustomUserDetails();

        String username = userDetails.getUsername();

        when(jwtService.extractUsername(anyString()))
                .thenReturn(username);


        when(userDetailsService.loadUserByUsername(username))
                .thenReturn(userDetails);


        when(jwtService.isTokenValid(anyString()))
                .thenReturn(true);

        when(jwtService.generateToken(
                any(CustomUserDetails.class),
                eq(jwtConfigurationProperties.accessExpirationTime())
        ))
                .thenReturn("access-token");


        when(jwtService.generateToken(
                any(CustomUserDetails.class),
                eq(jwtConfigurationProperties.refreshExpirationTime())
        ))
                .thenReturn("refresh-token");


        AuthResponse response =
                authService.refreshToken("refresh-token");

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());


        assertNotNull(response);

        assertThat(response.getAccessToken())
                .isNotBlank();

        assertThat(response.getRefreshToken())
                .isNotBlank();


        verify(jwtService)
                .extractUsername("refresh-token");

        verify(jwtService)
                .isTokenValid("refresh-token");
    }


    @Test
    void shouldThrowExceptionWhenRefreshTokenInvalid() {

        CustomUserDetails customUserDetails = new UserTestBuilder().buildCustomUserDetails();

        String username = customUserDetails.getUsername();


        when(jwtService.extractUsername(anyString()))
                .thenReturn(username);


        when(userDetailsService.loadUserByUsername(username))
                .thenReturn(customUserDetails);


        when(jwtService.isTokenValid("bad-token"))
                .thenReturn(false);


        assertThatThrownBy(() ->
                authService.refreshToken("bad-token")
        )
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}
