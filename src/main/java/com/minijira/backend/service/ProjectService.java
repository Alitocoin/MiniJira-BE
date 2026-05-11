package com.minijira.backend.service;

import com.minijira.backend.dto.ProjectRequest;
import com.minijira.backend.dto.ProjectResponse;

import java.util.List;

public interface ProjectService {
    List<ProjectResponse> findAll();
    ProjectResponse findById(Long id);
    ProjectResponse create(ProjectRequest request);
    ProjectResponse update(Long id, ProjectRequest request);
    void delete(Long id);
}
