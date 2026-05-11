package com.minijira.backend.service;

import com.minijira.backend.dto.StatusUpdateRequest;
import com.minijira.backend.dto.TaskRequest;
import com.minijira.backend.dto.TaskResponse;
import com.minijira.backend.exception.ResourceNotFoundException;
import com.minijira.backend.model.Project;
import com.minijira.backend.model.Task;
import com.minijira.backend.model.TaskStatus;
import com.minijira.backend.model.User;
import com.minijira.backend.repository.ProjectRepository;
import com.minijira.backend.repository.TaskRepository;
import com.minijira.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskServiceImpl — unit tests")
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task buildTask(Long id, String title, TaskStatus status) {
        Task t = new Task();
        t.setId(id);
        t.setTitle(title);
        t.setDescription("Descripción de prueba");
        t.setStatus(status);
        t.setStoryPoints(3);
        t.setEstimatedHours(4.0);
        t.setStartDate(LocalDate.of(2026, 1, 1));
        t.setEndDate(LocalDate.of(2026, 1, 15));
        t.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        return t;
    }

    private User buildUser(Long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(username + "@test.com");
        u.setPassword("hashed");
        u.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        return u;
    }

    private Project buildProject(Long id, String name) {
        Project p = new Project();
        p.setId(id);
        p.setName(name);
        p.setDescription("Proyecto de prueba");
        p.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        return p;
    }

    // ------------------------------------------------------------------ //
    // findAll
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("getAllTasks_returnsListOfTasks: devuelve todas las tareas mapeadas a DTO")
    void getAllTasks_returnsListOfTasks() {
        Task t1 = buildTask(1L, "Tarea A", TaskStatus.TODO);
        Task t2 = buildTask(2L, "Tarea B", TaskStatus.IN_PROGRESS);
        when(taskRepository.findAll()).thenReturn(List.of(t1, t2));

        List<TaskResponse> result = taskService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Tarea A");
        assertThat(result.get(1).getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllTasks_emptyRepository: devuelve lista vacía cuando no hay tareas")
    void getAllTasks_emptyRepository_returnsEmptyList() {
        when(taskRepository.findAll()).thenReturn(List.of());

        List<TaskResponse> result = taskService.findAll();

        assertThat(result).isEmpty();
        verify(taskRepository).findAll();
    }

    // ------------------------------------------------------------------ //
    // findById
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("getTaskById_existingId_returnsTask: retorna DTO correcto para id existente")
    void getTaskById_existingId_returnsTask() {
        Task task = buildTask(10L, "Mi tarea", TaskStatus.DONE);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        TaskResponse result = taskService.findById(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("Mi tarea");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(result.getStoryPoints()).isEqualTo(3);
        verify(taskRepository).findById(10L);
    }

    @Test
    @DisplayName("getTaskById_nonExistingId_throwsResourceNotFoundException: lanza excepción para id inexistente")
    void getTaskById_nonExistingId_throwsResourceNotFoundException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(taskRepository).findById(99L);
    }

    // ------------------------------------------------------------------ //
    // create
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("createTask_validRequest_returnsTaskResponse: persiste y devuelve tarea con status TODO por defecto")
    void createTask_validRequest_returnsTaskResponse() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Nueva tarea");
        request.setDescription("Descripción");
        request.setStoryPoints(5);
        request.setEstimatedHours(8.0);
        // status null => debe defaultear a TODO

        Task saved = buildTask(1L, "Nueva tarea", TaskStatus.TODO);
        saved.setDescription("Descripción");
        saved.setStoryPoints(5);
        saved.setEstimatedHours(8.0);
        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        TaskResponse result = taskService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Nueva tarea");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(result.getStoryPoints()).isEqualTo(5);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("createTask_withExplicitStatus_respectsProvidedStatus")
    void createTask_withExplicitStatus_respectsProvidedStatus() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Tarea con status");
        request.setStatus(TaskStatus.IN_PROGRESS);

        Task saved = buildTask(2L, "Tarea con status", TaskStatus.IN_PROGRESS);
        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        TaskResponse result = taskService.create(request);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("createTask_withAssigneeAndProject_resolvesRelations")
    void createTask_withAssigneeAndProject_resolvesRelations() {
        User user = buildUser(5L, "jdoe");
        Project project = buildProject(3L, "Sprint Alpha");

        TaskRequest request = new TaskRequest();
        request.setTitle("Tarea con asignado");
        request.setAssigneeId(5L);
        request.setProjectId(3L);

        Task saved = buildTask(7L, "Tarea con asignado", TaskStatus.TODO);
        saved.setAssignee(user);
        saved.setProject(project);

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(projectRepository.findById(3L)).thenReturn(Optional.of(project));
        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        TaskResponse result = taskService.create(request);

        assertThat(result.getAssignee()).isNotNull();
        assertThat(result.getAssignee().getUsername()).isEqualTo("jdoe");
        assertThat(result.getProject()).isNotNull();
        assertThat(result.getProject().getName()).isEqualTo("Sprint Alpha");
    }

    @Test
    @DisplayName("createTask_nonExistingAssigneeId_throwsResourceNotFoundException")
    void createTask_nonExistingAssigneeId_throwsResourceNotFoundException() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Tarea orphan");
        request.setAssigneeId(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTask_nonExistingProjectId_throwsResourceNotFoundException")
    void createTask_nonExistingProjectId_throwsResourceNotFoundException() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Tarea sin proyecto");
        request.setProjectId(888L);

        when(projectRepository.findById(888L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("888");

        verify(taskRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ //
    // updateStatus
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("updateTaskStatus_validStatus_updatesCorrectly: cambia el status y persiste")
    void updateTaskStatus_validStatus_updatesCorrectly() {
        Task task = buildTask(1L, "Tarea A", TaskStatus.TODO);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task updated = buildTask(1L, "Tarea A", TaskStatus.IN_PROGRESS);
        when(taskRepository.save(any(Task.class))).thenReturn(updated);

        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(TaskStatus.IN_PROGRESS);

        TaskResponse result = taskService.updateStatus(1L, req);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("updateTaskStatus_nonExistingId_throwsResourceNotFoundException")
    void updateTaskStatus_nonExistingId_throwsResourceNotFoundException() {
        when(taskRepository.findById(55L)).thenReturn(Optional.empty());

        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(TaskStatus.DONE);

        assertThatThrownBy(() -> taskService.updateStatus(55L, req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("55");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateTaskStatus_toDone_setsCorrectFinalStatus")
    void updateTaskStatus_toDone_setsCorrectFinalStatus() {
        Task task = buildTask(2L, "En progreso", TaskStatus.IN_PROGRESS);
        when(taskRepository.findById(2L)).thenReturn(Optional.of(task));

        Task saved = buildTask(2L, "En progreso", TaskStatus.DONE);
        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(TaskStatus.DONE);

        TaskResponse result = taskService.updateStatus(2L, req);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.DONE);
    }

    // ------------------------------------------------------------------ //
    // delete
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("deleteTask_existingId_deletesSuccessfully: llama a deleteById cuando existe")
    void deleteTask_existingId_deletesSuccessfully() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        taskService.delete(1L);

        verify(taskRepository).existsById(1L);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteTask_nonExistingId_throwsResourceNotFoundException: lanza excepción para id inexistente")
    void deleteTask_nonExistingId_throwsResourceNotFoundException() {
        when(taskRepository.existsById(77L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.delete(77L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("77");

        verify(taskRepository, never()).deleteById(any());
    }

    // ------------------------------------------------------------------ //
    // toResponse — mapeo con relaciones nulas
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("createTask_noAssigneeNoProject_responseHasNullRelations")
    void createTask_noAssigneeNoProject_responseHasNullRelations() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Tarea simple");

        Task saved = buildTask(3L, "Tarea simple", TaskStatus.TODO);
        // assignee y project son null por defecto en buildTask
        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        TaskResponse result = taskService.create(request);

        assertThat(result.getAssignee()).isNull();
        assertThat(result.getProject()).isNull();
    }
}
