package com.auth_service.security.service.impl;

import com.auth_service.client.UserClient;
import com.auth_service.config.properties.JwtConfigurationProperties;
import com.auth_service.constant.AuthConstant;
import com.auth_service.constant.ExceptionConstant;
import com.auth_service.dto.MailDto;
import com.auth_service.dto.UserRequestDto;
import com.auth_service.dto.UserResponseDto;
import com.auth_service.dto.security.AuthRequest;
import com.auth_service.dto.security.AuthResponse;
import com.auth_service.dto.security.CustomUserDetails;
import com.auth_service.exception.InvalidRefreshTokenException;
import com.auth_service.producer.MailProducer;
import com.auth_service.security.service.AuthHelper;
import com.auth_service.security.service.AuthService;
import com.auth_service.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtConfigurationProperties jwtConfigurationProperties;

    private final UserClient userClient;

    private final AuthHelper authHelper;

    private final PasswordEncoder encoder;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    private final CustomUserDetailsServiceImpl userDetailsService;

    private final MailProducer mailProducer;

    @Override
    public AuthResponse register(UserRequestDto requestDto) {

        log.info("Encoding password");

        requestDto.setPassword(encoder.encode(requestDto.getPassword()));

        log.info("Trying to create a user");

        UserResponseDto createdUser = userClient.create(requestDto);

        log.info("User was created successfully");

        log.info("Trying to generate confirmation token email: {}", createdUser.getEmail());

        String emailVerificationToken = userClient.generateEmailConfirmationToken(createdUser.getId());

        log.info("Confirmation token was successfully generated");

        log.info("Trying to send confirmation mail");

        String url = authHelper.constructEmailConfirmationUrl(emailVerificationToken);

        mailProducer.sendMail(constructMail(
                createdUser.getEmail(),
                AuthConstant.REGISTRATION_THEME,
                authHelper.constructEmailTextWithUrl(AuthConstant.REGISTRATION_MAIL_TEXT, url)));

        log.info("Confirmation mail was successfully sent");

        log.info("Getting custom user details for username: {}", requestDto.getUsername());

        CustomUserDetails customUserDetails = userDetailsService.loadUserByUsername(requestDto.getUsername());

        log.info("Generating access and refresh tokens");

        return generateTokens(customUserDetails);
    }

    @Override
    public AuthResponse login(AuthRequest request) {

        log.info("Getting authentication by username: {}", request.getUsername());

        Authentication authentication = getAuthentication(request.getUsername(), request.getPassword());

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        log.info("Generating access and refresh tokens");

        return generateTokens(userDetails);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {

        String extractedUsername = jwtService.extractUsername(refreshToken);

        CustomUserDetails userDetailsByLogin = userDetailsService.loadUserByUsername(extractedUsername);

        log.info("Checking validity of refresh token for user: {}", extractedUsername);

        if (!jwtService.isTokenValid(refreshToken)) {
            throw new InvalidRefreshTokenException(ExceptionConstant.INVALID_REFRESH_TOKEN);
        }

        log.info("Generating new tokens");

        return generateTokens(userDetailsByLogin);
    }

    private AuthResponse generateTokens(CustomUserDetails customUserDetails) {

        String accessToken = jwtService.generateToken(customUserDetails, jwtConfigurationProperties.accessExpirationTime());
        String refreshToken = jwtService.generateToken(customUserDetails, jwtConfigurationProperties.refreshExpirationTime());

        return new AuthResponse(accessToken, refreshToken);
    }

    private Authentication getAuthentication(String login, String password) {

        log.debug("Attempting to authenticate user: {}", login);

        Authentication userAuthentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login, password)
        );

        log.info("Authentication was successfully got");

        return userAuthentication;
    }

    private MailDto constructMail(String email, String theme, String text) {

        return new MailDto(email, theme, text);
    }
}
