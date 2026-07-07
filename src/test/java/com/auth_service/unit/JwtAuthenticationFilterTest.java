package com.auth_service.unit;

import com.auth_service.dto.security.CustomUserDetails;
import com.auth_service.security.JwtAuthenticationFilter;
import com.auth_service.security.service.JwtService;
import com.auth_service.security.service.impl.CustomUserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void shouldSkipWhenAuthorizationHeaderMissing()
            throws ServletException, IOException {

        when(request.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldSkipWhenHeaderIsNotBearer()
            throws ServletException, IOException {

        when(request.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn("Basic test");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldAuthenticateUserWhenTokenValid()
            throws ServletException, IOException {

        String token = "jwt-token";

        when(request.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer " + token);

        when(jwtService.extractUsername(token))
                .thenReturn("john");

        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(userDetailsService.loadUserByUsername("john"))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid(token))
                .thenReturn(true);

        when(userDetails.getAuthorities())
                .thenReturn(List.of());

        filter.doFilter(request, response, filterChain);

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(auth);
        assertEquals(userDetails, auth.getPrincipal());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenTokenInvalid()
            throws ServletException, IOException {

        String token = "jwt-token";

        when(request.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer " + token);

        when(jwtService.extractUsername(token))
                .thenReturn("john");

        when(jwtService.isTokenValid(token))
                .thenReturn(false);

        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(userDetailsService.loadUserByUsername("john"))
                .thenReturn(userDetails);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
