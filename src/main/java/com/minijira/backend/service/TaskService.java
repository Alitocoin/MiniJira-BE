package com.minijira.backend.service;

import com.minijira.backend.dto.StatusUpdateRequest;
import com.minijira.backend.dto.TaskRequest;
import com.minijira.backend.dto.TaskResponse;

import java.util.List;

public interface TaskService {
    List<TaskResponse> findAll();
    TaskResponse findById(Long id);
    TaskResponse create(TaskRequest request);
    TaskResponse update(Long id, TaskRequest request);
    TaskResponse updateStatus(Long id, StatusUpdateRequest request);
    void delete(Long id);
}
