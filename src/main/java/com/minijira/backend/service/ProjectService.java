package com.minijira.backend.service;

import com.minijira.backend.dto.request.ProjectRequest;
import com.minijira.backend.dto.response.ProjectResponse;

import java.util.List;

public interface ProjectService {

    List<ProjectResponse> findAll();

    ProjectResponse findById(Long id);

    ProjectResponse create(ProjectRequest request);
}
