package com.caovinh.user.service;

import com.caovinh.common.dto.PageResponse;
import com.caovinh.user.dto.UserRequestDto;
import com.caovinh.user.dto.UserResponseDto;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponseDto createUser(UserRequestDto requestDto);

    UserResponseDto getUserById(Long id);

    PageResponse<UserResponseDto> getAllUsers(Pageable pageable);

    UserResponseDto updateUser(Long id, UserRequestDto requestDto);

    void deleteUser(Long id);
}
