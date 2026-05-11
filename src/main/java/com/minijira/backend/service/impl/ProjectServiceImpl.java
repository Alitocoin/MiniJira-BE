package com.minijira.backend.service.impl;

import com.minijira.backend.dto.request.ProjectRequest;
import com.minijira.backend.dto.response.ProjectResponse;
import com.minijira.backend.exception.ResourceNotFoundException;
import com.minijira.backend.model.Project;
import com.minijira.backend.repository.ProjectRepository;
import com.minijira.backend.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    @Override
    public List<ProjectResponse> findAll() {
        log.debug("Consultando todos los proyectos");
        return projectRepository.findAll()
                .stream()
                .map(ProjectResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectResponse findById(Long id) {
        log.debug("Consultando proyecto id={}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto", id));
        return ProjectResponse.from(project);
    }

    @Override
    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        log.debug("Creando proyecto name={}", request.getName());

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Project saved = projectRepository.save(project);
        log.info("Proyecto creado id={}", saved.getId());
        return ProjectResponse.from(saved);
    }
}
