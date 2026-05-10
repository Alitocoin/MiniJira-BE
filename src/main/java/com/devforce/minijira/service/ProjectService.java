package com.devforce.minijira.service;

import com.devforce.minijira.dto.request.ProjectRequest;
import com.devforce.minijira.dto.response.ProjectResponse;
import com.devforce.minijira.exception.ResourceNotFoundException;
import com.devforce.minijira.model.Project;
import com.devforce.minijira.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;

    public List<ProjectResponse> findAll() {
        return projectRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse findById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto", id));
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return toResponse(projectRepository.save(project));
    }

    public ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .build();
    }
}
