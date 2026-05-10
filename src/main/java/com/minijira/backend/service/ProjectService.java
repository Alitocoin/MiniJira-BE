package com.minijira.backend.service;

import com.minijira.backend.dto.ProjectRequest;
import com.minijira.backend.dto.ProjectResponse;

import java.util.List;

public interface ProjectService {
    List<ProjectResponse> findAll();
    ProjectResponse create(ProjectRequest request);
}
