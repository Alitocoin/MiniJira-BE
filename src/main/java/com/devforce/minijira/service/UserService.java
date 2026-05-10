package com.devforce.minijira.service;

import com.devforce.minijira.dto.request.UserRequest;
import com.devforce.minijira.dto.response.UserResponse;
import com.devforce.minijira.exception.BusinessException;
import com.devforce.minijira.exception.ResourceNotFoundException;
import com.devforce.minijira.model.User;
import com.devforce.minijira.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        return toResponse(user);
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Ya existe un usuario con el email: " + request.getEmail());
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();
        return toResponse(userRepository.save(user));
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
