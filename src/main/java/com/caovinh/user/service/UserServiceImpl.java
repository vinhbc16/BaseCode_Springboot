package com.caovinh.user.service;

import com.caovinh.common.dto.PageResponse;
import com.caovinh.common.exception.BadRequestException;
import com.caovinh.common.exception.ResourceNotFoundException;
import com.caovinh.user.dto.UserRequestDto;
import com.caovinh.user.dto.UserResponseDto;
import com.caovinh.user.entity.User;
import com.caovinh.user.mapper.UserMapper;
import com.caovinh.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto createUser(UserRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new BadRequestException("Email already exists: " + requestDto.getEmail());
        }

        User user = userMapper.toEntity(requestDto);
        user.setActive(true);

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponseDto> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);

        List<UserResponseDto> content = userPage.getContent()
                .stream()
                .map(userMapper::toDto)
                .toList();

        return PageResponse.<UserResponseDto>builder()
                .content(content)
                .pageNo(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .build();
    }

    @Override
    public UserResponseDto updateUser(Long id, UserRequestDto requestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Check email uniqueness if email is changing
        if (!user.getEmail().equals(requestDto.getEmail())
                && userRepository.existsByEmail(requestDto.getEmail())) {
            throw new BadRequestException("Email already exists: " + requestDto.getEmail());
        }

        userMapper.updateEntityFromDto(requestDto, user);
        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userRepository.delete(user);
    }
}
