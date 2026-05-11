package com.minijira.backend.service;

import com.minijira.backend.dto.ProjectResponse;
import com.minijira.backend.dto.StatusUpdateRequest;
import com.minijira.backend.dto.TaskRequest;
import com.minijira.backend.dto.TaskResponse;
import com.minijira.backend.dto.UserResponse;
import com.minijira.backend.exception.ResourceNotFoundException;
import com.minijira.backend.model.Project;
import com.minijira.backend.model.Task;
import com.minijira.backend.model.TaskStatus;
import com.minijira.backend.model.User;
import com.minijira.backend.repository.ProjectRepository;
import com.minijira.backend.repository.TaskRepository;
import com.minijira.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public TaskServiceImpl(TaskRepository taskRepository,
                           UserRepository userRepository,
                           ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> findAll() {
        return taskRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse findById(Long id) {
        Task task = findTaskOrThrow(id);
        return toResponse(task);
    }

    @Override
    public TaskResponse create(TaskRequest request) {
        Task task = new Task();
        applyRequestToTask(request, task);
        task.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO);
        return toResponse(taskRepository.save(task));
    }

    @Override
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = findTaskOrThrow(id);
        applyRequestToTask(request, task);
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        return toResponse(taskRepository.save(task));
    }

    @Override
    public TaskResponse updateStatus(Long id, StatusUpdateRequest request) {
        Task task = findTaskOrThrow(id);
        task.setStatus(request.getStatus());
        return toResponse(taskRepository.save(task));
    }

    @Override
    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task", id);
        }
        taskRepository.deleteById(id);
    }

    private void applyRequestToTask(TaskRequest request, Task task) {
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStoryPoints(request.getStoryPoints());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setStartDate(request.getStartDate());
        task.setEndDate(request.getEndDate());

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssigneeId()));
            task.setAssignee(assignee);
        } else {
            task.setAssignee(null);
        }

        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", request.getProjectId()));
            task.setProject(project);
        } else {
            task.setProject(null);
        }
    }

    private Task findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }

    private TaskResponse toResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setStoryPoints(task.getStoryPoints());
        response.setEstimatedHours(task.getEstimatedHours());
        response.setStartDate(task.getStartDate());
        response.setEndDate(task.getEndDate());
        response.setCreatedAt(task.getCreatedAt());

        if (task.getAssignee() != null) {
            User u = task.getAssignee();
            response.setAssignee(new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getCreatedAt()));
        }

        if (task.getProject() != null) {
            Project p = task.getProject();
            response.setProject(new ProjectResponse(p.getId(), p.getName(), p.getDescription(), p.getCreatedAt()));
        }

        return response;
    }
}
