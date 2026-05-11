package com.minijira.backend.service.impl;

import com.minijira.backend.dto.request.AuthRequest;
import com.minijira.backend.dto.request.RegisterRequest;
import com.minijira.backend.dto.response.AuthResponse;
import com.minijira.backend.dto.response.UserResponse;
import com.minijira.backend.exception.DuplicateResourceException;
import com.minijira.backend.exception.ResourceNotFoundException;
import com.minijira.backend.model.User;
import com.minijira.backend.repository.UserRepository;
import com.minijira.backend.security.JwtUtil;
import com.minijira.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.debug("Registro de usuario email={}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Ya existe un usuario con el email: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        User saved = userRepository.save(user);
        log.info("Usuario registrado id={}", saved.getId());
        return UserResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        log.debug("Login email={}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResourceNotFoundException(
                    "Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        log.info("Login exitoso para id={}", user.getId());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .username(user.getName())
                .build();
    }
}
