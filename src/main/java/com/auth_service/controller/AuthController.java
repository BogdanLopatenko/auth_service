package com.auth_service.controller;

import com.auth_service.client.UserClient;
import com.auth_service.dto.UserRequestDto;
import com.auth_service.dto.security.AuthRequest;
import com.auth_service.dto.security.AuthResponse;
import com.auth_service.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final UserClient userServiceClient;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registration(@RequestBody UserRequestDto dto) {

        return ResponseEntity.ok(authService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Refresh-Token") String refreshToken) {

        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @GetMapping("/confirm-email")
    public void confirmEmail(@RequestParam("token") String token) {

        userServiceClient.confirmUserEmail(token);
    }
}