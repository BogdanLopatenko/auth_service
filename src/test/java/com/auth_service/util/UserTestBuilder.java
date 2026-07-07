package com.auth_service.util;

import com.auth_service.dto.UserAuthDto;
import com.auth_service.dto.UserRequestDto;
import com.auth_service.dto.UserResponseDto;
import com.auth_service.dto.security.AuthRequest;
import com.auth_service.dto.security.AuthResponse;
import com.auth_service.dto.security.CustomUserDetails;
import com.auth_service.enums.UserRole;
import com.auth_service.enums.UserStatus;
import com.user_service.generated.UserId;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class UserTestBuilder {

    private Long id = 1L;

    private String username = "testusername";

    private String password = "tetspassword";

    private String firstname = "testfirstname";

    private String lastname = "testlastname";

    private String email = "testemail@gmail.com";

    private UserRole role = UserRole.USER;

    private com.user_service.generated.UserRole protoRole = com.user_service.generated.UserRole.USER;

    private UserStatus status = UserStatus.NEED_EMAIL_CONFIRMATION;

    private com.user_service.generated.UserStatus protoStatus = com.user_service.generated.UserStatus.NEED_EMAIL_CONFIRMATION;

    public UserTestBuilder withId(Long id) {

        this.id = id;

        return this;
    }

    public UserTestBuilder withUsername(String username) {

        this.username = username;

        return this;
    }

    public UserTestBuilder withPassword(String password) {

        this.password = password;

        return this;
    }

    public UserTestBuilder withFirstname(String firstname) {

        this.firstname = firstname;

        return this;
    }

    public UserTestBuilder withLastname(String lastname) {

        this.lastname = lastname;

        return this;
    }

    public UserTestBuilder withEmail(String email) {

        this.email = email;

        return this;
    }

    public UserTestBuilder withRole(UserRole role) {

        this.role = role;

        return this;
    }

    public UserTestBuilder withStatus(UserStatus status) {

        this.status = status;

        return this;
    }

    public UserResponseDto buildResponseDto() {

        return new UserResponseDto(
                id,
                username,
                firstname,
                lastname,
                email,
                role,
                status
        );
    }

    public UserRequestDto buildRequestDto() {

        return new UserRequestDto(
                username,
                password,
                firstname,
                lastname,
                email
        );
    }

    public UserAuthDto buildAuthDto() {

        return new UserAuthDto(
                username,
                password,
                email,
                role
        );
    }

    public AuthRequest buildAuthRequestDto() {

        return new AuthRequest(
                username,
                password
        );
    }

    public AuthResponse buildAuthResponseDto() {

        return new AuthResponse(
                "someAccessToken",
                "someRefreshToken"
        );
    }

    public CustomUserDetails buildCustomUserDetails(){

        return new CustomUserDetails(
                username,
                password,
                email,
                List.of(
                        new SimpleGrantedAuthority(role.name()))
        );
    }

    public com.user_service.generated.UserAuthDto buildProtoAuthDto() {

        return com.user_service.generated.UserAuthDto.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .setEmail(email)
                .setRole(protoRole)
                .build();
    }

    public com.user_service.generated.UserResponseDto buildProtoResponseDto() {

        return com.user_service.generated.UserResponseDto.newBuilder()
                .setId(id)
                .setUsername(username)
                .setFirstname(firstname)
                .setLastname(lastname)
                .setEmail(email)
                .setRole(protoRole)
                .setStatus(protoStatus)
                .build();
    }

    public com.user_service.generated.UserRequestDto buildProtoRequestDto() {

        return com.user_service.generated.UserRequestDto.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .setFirstname(firstname)
                .setLastname(lastname)
                .setEmail(email)
                .build();
    }

    public UserId buildUserId() {

        return UserId.newBuilder()
                .setId(id)
                .build();
    }
}
