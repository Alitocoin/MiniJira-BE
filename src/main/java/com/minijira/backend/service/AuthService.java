package com.minijira.backend.service;

import com.minijira.backend.dto.request.AuthRequest;
import com.minijira.backend.dto.request.RegisterRequest;
import com.minijira.backend.dto.response.AuthResponse;
import com.minijira.backend.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(AuthRequest request);
}
