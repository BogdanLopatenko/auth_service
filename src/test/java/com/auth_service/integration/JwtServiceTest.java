package com.auth_service.integration;

import com.auth_service.config.properties.JwtConfigurationProperties;
import com.auth_service.dto.security.CustomUserDetails;
import com.auth_service.security.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtConfigurationProperties jwtConfigurationProperties;


    @Test
    void shouldGenerateAndExtractUsernameFromToken() {

        CustomUserDetails userDetails = createUserDetails();

        String token = jwtService.generateToken(userDetails, 60000);

        String username = jwtService.extractUsername(token);

        assertThat(username)
                .isEqualTo("john");
    }


    @Test
    void shouldReturnTrueWhenTokenIsValid() {

        CustomUserDetails userDetails = createUserDetails();

        String token = jwtService.generateToken(userDetails, 60000);

        boolean valid = jwtService.isTokenValid(token);

        assertThat(valid)
                .isTrue();
    }


    @Test
    void shouldReturnFalseWhenTokenIsExpired() {

        CustomUserDetails userDetails = createUserDetails();

        String token = jwtService.generateToken(userDetails, -1000);

        boolean valid = jwtService.isTokenValid(token);

        assertThat(valid)
                .isFalse();
    }


    @Test
    void shouldReturnFalseWhenTokenIsInvalid() {

        boolean valid = jwtService.isTokenValid("invalid-token");

        assertThat(valid)
                .isFalse();
    }


    @Test
    void shouldGenerateDifferentTokensForDifferentUsers() {

        CustomUserDetails firstUser = createUserDetails(
                "john"
        );

        CustomUserDetails secondUser = createUserDetails(
                "alex"
        );


        String firstToken =
                jwtService.generateToken(firstUser, 60000);

        String secondToken =
                jwtService.generateToken(secondUser, 60000);


        assertThat(firstToken)
                .isNotEqualTo(secondToken);
    }


    private CustomUserDetails createUserDetails() {
        return createUserDetails("john");
    }


    private CustomUserDetails createUserDetails(String username) {

        return new CustomUserDetails(
                username,
                "password",
                "john@mail.com",
                List.of(
                        new SimpleGrantedAuthority("ROLE_USER")
                )
        );
    }
}
