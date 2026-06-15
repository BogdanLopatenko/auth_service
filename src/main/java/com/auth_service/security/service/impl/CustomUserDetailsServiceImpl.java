package com.auth_service.security.service.impl;

import com.auth_service.client.UserClient;
import com.auth_service.dto.UserAuthDto;
import com.auth_service.dto.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final UserClient userClient;

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("Trying to get user by username: {}", username);

        UserAuthDto byUsername = userClient.getByUsername(username);

        log.info("User was successfully found");

        return new CustomUserDetails(
                byUsername.getUsername(),
                byUsername.getPassword(),
                byUsername.getEmail(),
                Collections.singletonList(new SimpleGrantedAuthority(String.valueOf(byUsername.getRole()))));
    }
}
