package com.minijira.backend.service;

import com.minijira.backend.dto.request.TaskRequest;
import com.minijira.backend.dto.request.TaskStatusRequest;
import com.minijira.backend.dto.response.TaskResponse;

import java.util.List;

public interface TaskService {

    List<TaskResponse> findAll();

    TaskResponse findById(Long id);

    TaskResponse create(TaskRequest request);

    TaskResponse update(Long id, TaskRequest request);

    void delete(Long id);

    TaskResponse updateStatus(Long id, TaskStatusRequest request);
}
