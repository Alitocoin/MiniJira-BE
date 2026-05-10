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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService — tests unitarios")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private TaskService taskService;

    private Task tareaBase;
    private User usuarioBase;
    private Project proyectoBase;

    @BeforeEach
    void setUp() {
        usuarioBase = User.builder()
                .id(1L)
                .name("Ana García")
                .email("ana@devforce.ai")
                .build();

        proyectoBase = Project.builder()
                .id(1L)
                .name("MiniJira")
                .description("Proyecto de gestión de tareas")
                .build();

        tareaBase = Task.builder()
                .id(1L)
                .title("Implementar login")
                .description("Crear endpoint de autenticación")
                .status(TaskStatus.TODO)
                .storyPoints(3)
                .estimatedHours(4.0)
                .startDate(LocalDate.of(2026, 5, 10))
                .endDate(LocalDate.of(2026, 5, 14))
                .assignedUser(usuarioBase)
                .project(proyectoBase)
                .build();
    }

    // -------------------------------------------------------------------------
    // createTask
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createTask_shouldReturnTaskResponse_whenValidRequest")
    void createTask_shouldReturnTaskResponse_whenValidRequest() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Implementar login");
        request.setDescription("Crear endpoint de autenticación");
        request.setStatus(TaskStatus.TODO);
        request.setStoryPoints(3);
        request.setEstimatedHours(4.0);
        request.setStartDate(LocalDate.of(2026, 5, 10));
        request.setEndDate(LocalDate.of(2026, 5, 14));
        request.setAssignedUserId(1L);
        request.setProjectId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(proyectoBase));
        when(taskRepository.save(any(Task.class))).thenReturn(tareaBase);

        TaskResponse response = taskService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Implementar login");
        assertThat(response.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(response.getAssignedUser()).isNotNull();
        assertThat(response.getAssignedUser().getEmail()).isEqualTo("ana@devforce.ai");
        assertThat(response.getProject()).isNotNull();
        assertThat(response.getProject().getName()).isEqualTo("MiniJira");

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("createTask_sinRelaciones_shouldReturnTaskResponseBasica")
    void createTask_sinRelaciones_shouldReturnTaskResponseBasica() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Tarea sin asignar");
        request.setStatus(TaskStatus.TODO);

        Task tareaMinima = Task.builder()
                .id(2L)
                .title("Tarea sin asignar")
                .status(TaskStatus.TODO)
                .build();

        when(taskRepository.save(any(Task.class))).thenReturn(tareaMinima);

        TaskResponse response = taskService.create(request);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getAssignedUser()).isNull();
        assertThat(response.getProject()).isNull();
        verify(userRepository, never()).findById(any());
        verify(projectRepository, never()).findById(any());
    }

    @Test
    @DisplayName("createTask_conUsuarioInexistente_shouldThrowResourceNotFoundException")
    void createTask_conUsuarioInexistente_shouldThrowResourceNotFoundException() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Tarea con usuario inexistente");
        request.setStatus(TaskStatus.TODO);
        request.setAssignedUserId(99L);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // getTaskById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getTaskById_shouldReturnTaskResponse_whenExists")
    void getTaskById_shouldReturnTaskResponse_whenExists() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(tareaBase));

        TaskResponse response = taskService.findById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Implementar login");
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getTaskById_shouldThrowException_whenNotFound")
    void getTaskById_shouldThrowException_whenNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(taskRepository, times(1)).findById(999L);
    }

    // -------------------------------------------------------------------------
    // updateTaskStatus
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateTaskStatus_shouldChangeStatus_whenValidStatus")
    void updateTaskStatus_shouldChangeStatus_whenValidStatus() {
        Task tareaGuardada = Task.builder()
                .id(1L)
                .title("Implementar login")
                .description("Crear endpoint de autenticación")
                .status(TaskStatus.IN_PROGRESS)
                .assignedUser(usuarioBase)
                .project(proyectoBase)
                .build();

        TaskStatusRequest statusRequest = new TaskStatusRequest();
        statusRequest.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(tareaBase));
        when(taskRepository.save(any(Task.class))).thenReturn(tareaGuardada);

        TaskResponse response = taskService.updateStatus(1L, statusRequest);

        assertThat(response.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("updateTaskStatus_shouldThrowException_whenTaskNotFound")
    void updateTaskStatus_shouldThrowException_whenTaskNotFound() {
        TaskStatusRequest statusRequest = new TaskStatusRequest();
        statusRequest.setStatus(TaskStatus.DONE);

        when(taskRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateStatus(404L, statusRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("updateTaskStatus_shouldTransitionFromTodoDone")
    void updateTaskStatus_shouldTransitionFromTodoDone() {
        Task tareaFinalizada = Task.builder()
                .id(1L)
                .title("Implementar login")
                .status(TaskStatus.DONE)
                .build();

        TaskStatusRequest statusRequest = new TaskStatusRequest();
        statusRequest.setStatus(TaskStatus.DONE);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(tareaBase));
        when(taskRepository.save(any(Task.class))).thenReturn(tareaFinalizada);

        TaskResponse response = taskService.updateStatus(1L, statusRequest);

        assertThat(response.getStatus()).isEqualTo(TaskStatus.DONE);
    }

    // -------------------------------------------------------------------------
    // getAllTasks
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAllTasks_shouldReturnList")
    void getAllTasks_shouldReturnList() {
        Task segundaTarea = Task.builder()
                .id(2L)
                .title("Revisar PR")
                .status(TaskStatus.IN_PROGRESS)
                .build();

        when(taskRepository.findAllWithRelations()).thenReturn(List.of(tareaBase, segundaTarea));

        List<TaskResponse> resultado = taskService.findAll();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getId()).isEqualTo(1L);
        assertThat(resultado.get(1).getId()).isEqualTo(2L);
        verify(taskRepository, times(1)).findAllWithRelations();
    }

    @Test
    @DisplayName("getAllTasks_shouldReturnEmptyList_whenNoTasks")
    void getAllTasks_shouldReturnEmptyList_whenNoTasks() {
        when(taskRepository.findAllWithRelations()).thenReturn(List.of());

        List<TaskResponse> resultado = taskService.findAll();

        assertThat(resultado).isEmpty();
        verify(taskRepository, times(1)).findAllWithRelations();
    }

    // -------------------------------------------------------------------------
    // updateTask (full update)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateTask_shouldReturnUpdatedResponse_whenValid")
    void updateTask_shouldReturnUpdatedResponse_whenValid() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Login actualizado");
        request.setStatus(TaskStatus.IN_PROGRESS);
        request.setStoryPoints(5);

        Task tareaActualizada = Task.builder()
                .id(1L)
                .title("Login actualizado")
                .status(TaskStatus.IN_PROGRESS)
                .storyPoints(5)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(tareaBase));
        when(taskRepository.save(any(Task.class))).thenReturn(tareaActualizada);

        TaskResponse response = taskService.update(1L, request);

        assertThat(response.getTitle()).isEqualTo("Login actualizado");
        assertThat(response.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(response.getStoryPoints()).isEqualTo(5);
    }

    @Test
    @DisplayName("updateTask_shouldThrowException_whenNotFound")
    void updateTask_shouldThrowException_whenNotFound() {
        TaskRequest request = new TaskRequest();
        request.setTitle("No importa");
        request.setStatus(TaskStatus.TODO);

        when(taskRepository.findById(777L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(777L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("777");
    }

    // -------------------------------------------------------------------------
    // deleteTask
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("delete_shouldCallDeleteById_whenTaskExists")
    void delete_shouldCallDeleteById_whenTaskExists() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        taskService.delete(1L);

        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("delete_shouldThrowException_whenTaskNotFound")
    void delete_shouldThrowException_whenTaskNotFound() {
        when(taskRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(taskRepository, never()).deleteById(any());
    }

    // -------------------------------------------------------------------------
    // findByStatus
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByStatus_shouldReturnFilteredList")
    void findByStatus_shouldReturnFilteredList() {
        when(taskRepository.findByStatusWithRelations(TaskStatus.TODO))
                .thenReturn(List.of(tareaBase));

        List<TaskResponse> resultado = taskService.findByStatus(TaskStatus.TODO);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getStatus()).isEqualTo(TaskStatus.TODO);
    }
}
