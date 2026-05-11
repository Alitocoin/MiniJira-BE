package com.minijira.backend.service.impl;

import com.minijira.backend.dto.request.UserRequest;
import com.minijira.backend.dto.response.UserResponse;
import com.minijira.backend.exception.DuplicateResourceException;
import com.minijira.backend.exception.ResourceNotFoundException;
import com.minijira.backend.model.User;
import com.minijira.backend.repository.UserRepository;
import com.minijira.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserResponse> findAll() {
        log.debug("Consultando todos los usuarios");
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse findById(Long id) {
        log.debug("Consultando usuario id={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public UserResponse create(UserRequest request) {
        log.debug("Creando usuario email={}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Ya existe un usuario con el email: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();

        User saved = userRepository.save(user);
        log.info("Usuario creado id={}", saved.getId());
        return UserResponse.from(saved);
    }
}
