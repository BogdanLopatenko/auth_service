package com.auth_service.security.service;

import com.auth_service.config.properties.JwtConfigurationProperties;
import com.auth_service.constant.SecurityConstant;
import com.auth_service.dto.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {

    private final JwtConfigurationProperties jwtConfigurationProperties;

    public String generateToken(CustomUserDetails userDetails, Integer expiration) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim(SecurityConstant.ROLE_CLAIM, userDetails.getAuthorities().iterator().next().getAuthority())
                .claim(SecurityConstant.EMAIL_CLAIM, userDetails.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {

        try {

            Claims allClaims = extractAllClaims(token);

            return !allClaims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {

            return false;
        }
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        log.info("Entering getSigningKey() method");

        SecretKey secretKey = Keys.hmacShaKeyFor(jwtConfigurationProperties.secret().getBytes());

        log.info("Exit getSigningKey() method");
        return secretKey;
    }

    private Key getKey() {
        return Keys.hmacShaKeyFor(jwtConfigurationProperties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
