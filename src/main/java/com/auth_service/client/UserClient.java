package com.auth_service.client;

import com.auth_service.dto.UserAuthDto;
import com.auth_service.dto.UserRequestDto;
import com.auth_service.dto.UserResponseDto;

public interface UserClient {

    UserAuthDto getByUsername(String username);

    UserResponseDto getUserByConfirmationToken(String token);

    UserResponseDto create(UserRequestDto userRequestDto);

    String generateEmailConfirmationToken(Long userId);

    void confirmUserEmail(String token);
}
