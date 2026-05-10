package com.devforce.minijira.service;

import com.devforce.minijira.dto.request.TaskRequest;
import com.devforce.minijira.dto.request.TaskStatusRequest;
import com.devforce.minijira.dto.response.TaskResponse;
import com.devforce.minijira.exception.ResourceNotFoundException;
import com.devforce.minijira.model.Project;
import com.devforce.minijira.model.Task;
import com.devforce.minijira.model.TaskStatus;
import com.devforce.minijira.model.User;
import com.devforce.minijira.repository.ProjectRepository;
import com.devforce.minijira.repository.TaskRepository;
import com.devforce.minijira.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public List<TaskResponse> findAll() {
        return taskRepository.findAllWithRelations()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse findById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", id));
        return toResponse(task);
    }

    public List<TaskResponse> findByStatus(TaskStatus status) {
        return taskRepository.findByStatusWithRelations(status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        Task task = buildTaskFromRequest(new Task(), request);
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", id));
        buildTaskFromRequest(task, request);
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateStatus(Long id, TaskStatusRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", id));
        task.setStatus(request.getStatus());
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tarea", id);
        }
        taskRepository.deleteById(id);
    }

    private Task buildTaskFromRequest(Task task, TaskRequest request) {
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setStoryPoints(request.getStoryPoints());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setStartDate(request.getStartDate());
        task.setEndDate(request.getEndDate());

        if (request.getAssignedUserId() != null) {
            User user = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", request.getAssignedUserId()));
            task.setAssignedUser(user);
        } else {
            task.setAssignedUser(null);
        }

        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proyecto", request.getProjectId()));
            task.setProject(project);
        } else {
            task.setProject(null);
        }

        return task;
    }

    private TaskResponse toResponse(Task task) {
        TaskResponse.TaskResponseBuilder builder = TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .storyPoints(task.getStoryPoints())
                .estimatedHours(task.getEstimatedHours())
                .startDate(task.getStartDate())
                .endDate(task.getEndDate());

        if (task.getAssignedUser() != null) {
            builder.assignedUser(com.devforce.minijira.dto.response.UserResponse.builder()
                    .id(task.getAssignedUser().getId())
                    .name(task.getAssignedUser().getName())
                    .email(task.getAssignedUser().getEmail())
                    .build());
        }

        if (task.getProject() != null) {
            builder.project(com.devforce.minijira.dto.response.ProjectResponse.builder()
                    .id(task.getProject().getId())
                    .name(task.getProject().getName())
                    .description(task.getProject().getDescription())
                    .build());
        }

        return builder.build();
    }
}
