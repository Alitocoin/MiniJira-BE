package com.minijira.backend.service.impl;

import com.minijira.backend.dto.request.TaskRequest;
import com.minijira.backend.dto.request.TaskStatusRequest;
import com.minijira.backend.dto.response.TaskResponse;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskServiceImpl - Tests Unitarios")
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Project proyecto;
    private User usuario;
    private Task tareaBase;

    @BeforeEach
    void setUp() {
        proyecto = Project.builder()
                .id(10L)
                .name("Proyecto Alpha")
                .description("Descripción del proyecto")
                .build();

        usuario = User.builder()
                .id(5L)
                .name("Carlos Dev")
                .email("carlos@example.com")
                .password("hashed")
                .build();

        tareaBase = Task.builder()
                .id(1L)
                .title("Tarea de prueba")
                .description("Descripción de la tarea")
                .status(TaskStatus.TODO)
                .storyPoints(3)
                .estimatedHours(4.0)
                .startDate(LocalDate.of(2026, 1, 10))
                .endDate(LocalDate.of(2026, 1, 20))
                .assignedUser(usuario)
                .project(proyecto)
                .build();
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findAll devuelve todas las tareas con detalles")
    void findAll_cuandoExistenTareas_debeRetornarListaCompleta() {
        Task tareaB = Task.builder()
                .id(2L)
                .title("Segunda tarea")
                .status(TaskStatus.IN_PROGRESS)
                .project(proyecto)
                .build();

        when(taskRepository.findAllWithDetails()).thenReturn(Arrays.asList(tareaBase, tareaB));

        List<TaskResponse> resultado = taskService.findAll();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getId()).isEqualTo(1L);
        assertThat(resultado.get(0).getTitle()).isEqualTo("Tarea de prueba");
        assertThat(resultado.get(1).getId()).isEqualTo(2L);
        assertThat(resultado.get(1).getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskRepository, times(1)).findAllWithDetails();
    }

    @Test
    @DisplayName("findAll sin tareas devuelve lista vacía")
    void findAll_cuandoNoHayTareas_debeRetornarListaVacia() {
        when(taskRepository.findAllWithDetails()).thenReturn(Collections.emptyList());

        List<TaskResponse> resultado = taskService.findAll();

        assertThat(resultado).isEmpty();
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findById con id existente retorna tarea correcta")
    void findById_cuandoIdExiste_debeRetornarTareaCorrecta() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(tareaBase));

        TaskResponse resultado = taskService.findById(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getTitle()).isEqualTo("Tarea de prueba");
        assertThat(resultado.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(resultado.getStoryPoints()).isEqualTo(3);
        assertThat(resultado.getProjectId()).isEqualTo(10L);
        assertThat(resultado.getProjectName()).isEqualTo("Proyecto Alpha");
        assertThat(resultado.getAssignedUserId()).isEqualTo(5L);
        assertThat(resultado.getAssignedUserName()).isEqualTo("Carlos Dev");
    }

    @Test
    @DisplayName("findById con id inexistente lanza ResourceNotFoundException")
    void findById_cuandoIdNoExiste_debeLanzarResourceNotFoundException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(taskRepository, times(1)).findById(999L);
    }

    // -------------------------------------------------------------------------
    // create — happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("create con datos válidos guarda tarea y retorna DTO correcto")
    void create_cuandoDatosValidos_debeGuardarYRetornarTarea() {
        TaskRequest request = buildValidTaskRequest();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        when(userRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(taskRepository.save(any(Task.class))).thenReturn(tareaBase);

        TaskResponse resultado = taskService.create(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getTitle()).isEqualTo("Tarea de prueba");
        assertThat(resultado.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(resultado.getProjectId()).isEqualTo(10L);
        assertThat(resultado.getAssignedUserId()).isEqualTo(5L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("create sin usuario asignado crea tarea sin assignedUser")
    void create_cuandoSinUsuarioAsignado_debeCrearTareaSinAsignar() {
        TaskRequest request = buildValidTaskRequest();
        request.setAssignedUserId(null);

        Task tareaSinUsuario = Task.builder()
                .id(2L)
                .title("Tarea sin asignar")
                .status(TaskStatus.TODO)
                .project(proyecto)
                .assignedUser(null)
                .build();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        when(taskRepository.save(any(Task.class))).thenReturn(tareaSinUsuario);

        TaskResponse resultado = taskService.create(request);

        assertThat(resultado.getAssignedUserId()).isNull();
        assertThat(resultado.getAssignedUserName()).isNull();
        verify(userRepository, never()).findById(any());
    }

    // -------------------------------------------------------------------------
    // create — edge cases: proyecto/usuario inexistente
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("create con projectId inexistente lanza ResourceNotFoundException")
    void create_cuandoProyectoInexistente_debeLanzarResourceNotFoundException() {
        TaskRequest request = buildValidTaskRequest();
        request.setProjectId(99L);

        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("create con assignedUserId inexistente lanza ResourceNotFoundException")
    void create_cuandoUsuarioAsignadoInexistente_debeLanzarResourceNotFoundException() {
        TaskRequest request = buildValidTaskRequest();
        request.setAssignedUserId(88L);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        when(userRepository.findById(88L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("88");

        verify(taskRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // create — edge cases: validaciones de negocio
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("create con storyPoints negativos: el servicio no valida — documenta comportamiento actual")
    void create_cuandoStoryPointsNegativos_guardaSinLanzarExcepcion() {
        // El servicio actual no valida storyPoints negativos en código (solo Bean Validation en controller).
        // Este test documenta el comportamiento actual del servicio puro.
        TaskRequest request = buildValidTaskRequest();
        request.setStoryPoints(-5);

        Task tareaConPuntosNegativos = Task.builder()
                .id(3L)
                .title("Tarea puntos negativos")
                .status(TaskStatus.TODO)
                .storyPoints(-5)
                .project(proyecto)
                .build();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        when(userRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(taskRepository.save(any(Task.class))).thenReturn(tareaConPuntosNegativos);

        TaskResponse resultado = taskService.create(request);

        assertThat(resultado.getStoryPoints()).isEqualTo(-5);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("create con estimatedHours negativas: el servicio no valida — documenta comportamiento actual")
    void create_cuandoEstimatedHoursNegativas_guardaSinLanzarExcepcion() {
        TaskRequest request = buildValidTaskRequest();
        request.setEstimatedHours(-2.5);

        Task tareaHorasNegativas = Task.builder()
                .id(4L)
                .title("Tarea horas negativas")
                .status(TaskStatus.TODO)
                .estimatedHours(-2.5)
                .project(proyecto)
                .build();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        when(userRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(taskRepository.save(any(Task.class))).thenReturn(tareaHorasNegativas);

        TaskResponse resultado = taskService.create(request);

        assertThat(resultado.getEstimatedHours()).isEqualTo(-2.5);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("create con startDate posterior a endDate: el servicio no valida — documenta comportamiento actual")
    void create_cuandoStartDateMayorQueEndDate_guardaSinLanzarExcepcion() {
        TaskRequest request = buildValidTaskRequest();
        request.setStartDate(LocalDate.of(2026, 5, 20));
        request.setEndDate(LocalDate.of(2026, 5, 1));  // endDate < startDate

        Task tareaFechasInvalidas = Task.builder()
                .id(5L)
                .title("Tarea fechas invertidas")
                .status(TaskStatus.TODO)
                .startDate(LocalDate.of(2026, 5, 20))
                .endDate(LocalDate.of(2026, 5, 1))
                .project(proyecto)
                .build();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        when(userRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(taskRepository.save(any(Task.class))).thenReturn(tareaFechasInvalidas);

        TaskResponse resultado = taskService.create(request);

        // Documenta que el servicio no lanza excepción con fechas invertidas
        assertThat(resultado.getStartDate()).isAfter(resultado.getEndDate());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("update con id existente actualiza campos y retorna DTO correcto")
    void update_cuandoIdExiste_debeActualizarYRetornarTarea() {
        TaskRequest request = buildValidTaskRequest();
        request.setTitle("Título actualizado");
        request.setStatus(TaskStatus.IN_PROGRESS);
        request.setStoryPoints(8);

        Task tareaActualizada = Task.builder()
                .id(1L)
                .title("Título actualizado")
                .status(TaskStatus.IN_PROGRESS)
                .storyPoints(8)
                .project(proyecto)
                .assignedUser(usuario)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(tareaBase));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        when(userRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(taskRepository.save(any(Task.class))).thenReturn(tareaActualizada);

        TaskResponse resultado = taskService.update(1L, request);

        assertThat(resultado.getTitle()).isEqualTo("Título actualizado");
        assertThat(resultado.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(resultado.getStoryPoints()).isEqualTo(8);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("update con id inexistente lanza ResourceNotFoundException")
    void update_cuandoIdNoExiste_debeLanzarResourceNotFoundException() {
        TaskRequest request = buildValidTaskRequest();

        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("update con proyecto inexistente lanza ResourceNotFoundException")
    void update_cuandoProyectoInexistente_debeLanzarResourceNotFoundException() {
        TaskRequest request = buildValidTaskRequest();
        request.setProjectId(77L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(tareaBase));
        when(projectRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("77");

        verify(taskRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("delete con id existente invoca delete en repositorio")
    void delete_cuandoIdExiste_debeEliminarTarea() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(tareaBase));
        doNothing().when(taskRepository).delete(tareaBase);

        taskService.delete(1L);

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).delete(tareaBase);
    }

    @Test
    @DisplayName("delete con id inexistente lanza ResourceNotFoundException")
    void delete_cuandoIdNoExiste_debeLanzarResourceNotFoundException() {
        when(taskRepository.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(55L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("55");

        verify(taskRepository, never()).delete(any());
    }

    // -------------------------------------------------------------------------
    // updateStatus
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateStatus a DONE actualiza solo el status")
    void updateStatus_cuandoIdExiste_debeActualizarSoloStatus() {
        TaskStatusRequest statusRequest = new TaskStatusRequest();
        statusRequest.setStatus(TaskStatus.DONE);

        Task tareaActualizada = Task.builder()
                .id(1L)
                .title("Tarea de prueba")
                .status(TaskStatus.DONE)
                .project(proyecto)
                .assignedUser(usuario)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(tareaBase));
        when(taskRepository.save(any(Task.class))).thenReturn(tareaActualizada);

        TaskResponse resultado = taskService.updateStatus(1L, statusRequest);

        assertThat(resultado.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(resultado.getTitle()).isEqualTo("Tarea de prueba");
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("updateStatus con id inexistente lanza ResourceNotFoundException")
    void updateStatus_cuandoIdNoExiste_debeLanzarResourceNotFoundException() {
        TaskStatusRequest statusRequest = new TaskStatusRequest();
        statusRequest.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(111L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateStatus(111L, statusRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("111");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus de TODO a IN_PROGRESS refleja la transición correctamente")
    void updateStatus_transicionTodoAInProgress_debeReflexarCambio() {
        TaskStatusRequest statusRequest = new TaskStatusRequest();
        statusRequest.setStatus(TaskStatus.IN_PROGRESS);

        Task tareaInProgress = Task.builder()
                .id(1L)
                .title("Tarea de prueba")
                .status(TaskStatus.IN_PROGRESS)
                .project(proyecto)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(tareaBase));
        when(taskRepository.save(any(Task.class))).thenReturn(tareaInProgress);

        TaskResponse resultado = taskService.updateStatus(1L, statusRequest);

        assertThat(resultado.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(resultado.getStatus()).isNotEqualTo(TaskStatus.TODO);
    }

    // -------------------------------------------------------------------------
    // helpers privados
    // -------------------------------------------------------------------------

    private TaskRequest buildValidTaskRequest() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Tarea de prueba");
        request.setDescription("Descripción de la tarea");
        request.setStatus(TaskStatus.TODO);
        request.setStoryPoints(3);
        request.setEstimatedHours(4.0);
        request.setStartDate(LocalDate.of(2026, 1, 10));
        request.setEndDate(LocalDate.of(2026, 1, 20));
        request.setAssignedUserId(5L);
        request.setProjectId(10L);
        return request;
    }
}
