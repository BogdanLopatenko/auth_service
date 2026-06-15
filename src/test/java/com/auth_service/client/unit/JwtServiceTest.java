package com.auth_service.client.unit;

import com.auth_service.config.properties.JwtConfigurationProperties;
import com.auth_service.dto.security.CustomUserDetails;
import com.auth_service.security.service.JwtService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {


    private JwtService jwtService;

    @Mock
    private JwtConfigurationProperties properties;

    @Mock
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {

        when(properties.secret())
                .thenReturn("myverysecuremyverysecuremyverysecure12");

        jwtService = new JwtService(properties);
    }

    @Test
    void shouldGenerateToken() {

        when(userDetails.getUsername()).thenReturn("john");
        when(userDetails.getEmail()).thenReturn("john@test.com");
        when(userDetails.getAuthorities())
                .thenReturn((Collection) List.of(
                        new SimpleGrantedAuthority("ROLE_USER")
                ));
        String token = jwtService.generateToken(userDetails, 100000);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractUsername() {

        when(userDetails.getUsername()).thenReturn("john");
        when(userDetails.getEmail()).thenReturn("john@test.com");
        when(userDetails.getAuthorities())
                .thenReturn((Collection) List.of(
                        new SimpleGrantedAuthority("ROLE_USER")
                ));

        String token = jwtService.generateToken(userDetails, 100000);

        String username = jwtService.extractUsername(token);

        assertEquals("john", username);
    }

    @Test
    void shouldReturnTrueWhenTokenValid() {

        when(userDetails.getUsername()).thenReturn("john");
        when(userDetails.getEmail()).thenReturn("john@test.com");
        when(userDetails.getAuthorities())
                .thenReturn((Collection) List.of(
                        new SimpleGrantedAuthority("ROLE_USER")
                ));

        String token = jwtService.generateToken(userDetails, 100000);

        boolean valid = jwtService.isTokenValid(token);

        assertTrue(valid);
    }

    @Test
    void shouldReturnFalseWhenTokenExpired() {

        when(userDetails.getUsername()).thenReturn("john");
        when(userDetails.getEmail()).thenReturn("john@test.com");
        when(userDetails.getAuthorities())
                .thenReturn((Collection) List.of(
                        new SimpleGrantedAuthority("ROLE_USER")
                ));

        String token = jwtService.generateToken(userDetails, -1000);

        boolean valid = jwtService.isTokenValid(token);

        assertFalse(valid);
    }

    @Test
    void shouldReturnFalseWhenTokenMalformed() {

        boolean valid = jwtService.isTokenValid("invalid.jwt.token");

        assertFalse(valid);
    }

    @Test
    void shouldThrowExceptionWhenExtractingUsernameFromInvalidToken() {

        assertThrows(JwtException.class,
                () -> jwtService.extractUsername("invalid.jwt.token"));
    }
}
