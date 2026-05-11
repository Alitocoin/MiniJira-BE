package com.minijira.backend.service;

import com.minijira.backend.dto.UserRequest;
import com.minijira.backend.dto.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> findAll();
    UserResponse findById(Long id);
    UserResponse create(UserRequest request);
    UserResponse update(Long id, UserRequest request);
    void delete(Long id);
}
