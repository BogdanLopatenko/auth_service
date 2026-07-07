package com.auth_service.integration;

import com.auth_service.dto.security.CustomUserDetails;
import com.auth_service.security.service.JwtService;
import com.auth_service.security.service.impl.CustomUserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsServiceImpl userDetailsService;

    @Test
    void shouldReturnForbiddenWhenNoToken() throws Exception {
        mockMvc.perform(get("/private"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAuthenticateUserWhenTokenIsValid() throws Exception {

        CustomUserDetails user = new CustomUserDetails(
                "username",
                "john",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(jwtService.extractUsername("valid-token"))
                .thenReturn("username");

        when(jwtService.isTokenValid("valid-token"))
                .thenReturn(true);

        when(userDetailsService.loadUserByUsername("username"))
                .thenReturn(user);

        mockMvc.perform(get("/private")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("username"));

        verify(jwtService).extractUsername("valid-token");
        verify(jwtService).isTokenValid("valid-token");
        verify(userDetailsService).loadUserByUsername("username");
    }

    @Test
    void shouldRejectInvalidToken() throws Exception {

        when(jwtService.extractUsername("bad-token"))
                .thenReturn("john");

        when(jwtService.isTokenValid("bad-token"))
                .thenReturn(false);

        CustomUserDetails user = mock(CustomUserDetails.class);
        when(userDetailsService.loadUserByUsername("john"))
                .thenReturn(user);

        mockMvc.perform(get("/private")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer bad-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldIgnoreInvalidAuthorizationHeader() throws Exception {

        mockMvc.perform(get("/private")
                        .header(HttpHeaders.AUTHORIZATION, "Basic 123456"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
    }
}
