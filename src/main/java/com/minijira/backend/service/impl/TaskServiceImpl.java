package com.minijira.backend.service.impl;

import com.minijira.backend.dto.request.TaskRequest;
import com.minijira.backend.dto.request.TaskStatusRequest;
import com.minijira.backend.dto.response.TaskResponse;
import com.minijira.backend.exception.ResourceNotFoundException;
import com.minijira.backend.model.Project;
import com.minijira.backend.model.Task;
import com.minijira.backend.model.User;
import com.minijira.backend.repository.ProjectRepository;
import com.minijira.backend.repository.TaskRepository;
import com.minijira.backend.repository.UserRepository;
import com.minijira.backend.service.TaskService;
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
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Override
    public List<TaskResponse> findAll() {
        log.debug("Consultando todas las tareas");
        return taskRepository.findAllWithDetails()
                .stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public TaskResponse findById(Long id) {
        log.debug("Consultando tarea id={}", id);
        Task task = getTaskOrThrow(id);
        return TaskResponse.from(task);
    }

    @Override
    @Transactional
    public TaskResponse create(TaskRequest request) {
        log.debug("Creando tarea title={}", request.getTitle());

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto", request.getProjectId()));

        User assignedUser = resolveUser(request.getAssignedUserId());

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .storyPoints(request.getStoryPoints())
                .estimatedHours(request.getEstimatedHours())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .assignedUser(assignedUser)
                .project(project)
                .build();

        Task saved = taskRepository.save(task);
        log.info("Tarea creada id={}", saved.getId());
        return TaskResponse.from(saved);
    }

    @Override
    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        log.debug("Actualizando tarea id={}", id);

        Task task = getTaskOrThrow(id);

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto", request.getProjectId()));

        User assignedUser = resolveUser(request.getAssignedUserId());

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setStoryPoints(request.getStoryPoints());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setStartDate(request.getStartDate());
        task.setEndDate(request.getEndDate());
        task.setAssignedUser(assignedUser);
        task.setProject(project);

        Task saved = taskRepository.save(task);
        log.info("Tarea actualizada id={}", saved.getId());
        return TaskResponse.from(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Eliminando tarea id={}", id);
        Task task = getTaskOrThrow(id);
        taskRepository.delete(task);
        log.info("Tarea eliminada id={}", id);
    }

    @Override
    @Transactional
    public TaskResponse updateStatus(Long id, TaskStatusRequest request) {
        log.debug("Actualizando status de tarea id={} -> {}", id, request.getStatus());
        Task task = getTaskOrThrow(id);
        task.setStatus(request.getStatus());
        Task saved = taskRepository.save(task);
        log.info("Status de tarea id={} actualizado a {}", id, saved.getStatus());
        return TaskResponse.from(saved);
    }

    // ---- helpers privados ----

    private Task getTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", id));
    }

    private User resolveUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));
    }
}
