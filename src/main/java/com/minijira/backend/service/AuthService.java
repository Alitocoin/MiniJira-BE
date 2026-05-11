package com.minijira.backend.service;

import com.minijira.backend.dto.AuthResponse;
import com.minijira.backend.dto.LoginRequest;
import com.minijira.backend.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
