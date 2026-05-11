package com.minijira.backend.service;

import com.minijira.backend.dto.request.UserRequest;
import com.minijira.backend.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    List<UserResponse> findAll();

    UserResponse findById(Long id);

    UserResponse create(UserRequest request);
}
