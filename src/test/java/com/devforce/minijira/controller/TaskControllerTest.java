package com.devforce.minijira.controller;

import com.devforce.minijira.dto.request.TaskRequest;
import com.devforce.minijira.dto.request.TaskStatusRequest;
import com.devforce.minijira.dto.response.ProjectResponse;
import com.devforce.minijira.dto.response.TaskResponse;
import com.devforce.minijira.dto.response.UserResponse;
import com.devforce.minijira.exception.GlobalExceptionHandler;
import com.devforce.minijira.exception.ResourceNotFoundException;
import com.devforce.minijira.model.TaskStatus;
import com.devforce.minijira.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("TaskController — tests de integración (MockMvc)")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    private ObjectMapper objectMapper;
    private TaskResponse tareaResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        tareaResponse = TaskResponse.builder()
                .id(1L)
                .title("Implementar login")
                .description("Crear endpoint de autenticación")
                .status(TaskStatus.TODO)
                .storyPoints(3)
                .estimatedHours(4.0)
                .startDate(LocalDate.of(2026, 5, 10))
                .endDate(LocalDate.of(2026, 5, 14))
                .assignedUser(UserResponse.builder()
                        .id(1L)
                        .name("Ana García")
                        .email("ana@devforce.ai")
                        .build())
                .project(ProjectResponse.builder()
                        .id(1L)
                        .name("MiniJira")
                        .description("Proyecto demo")
                        .build())
                .build();
    }

    // -------------------------------------------------------------------------
    // POST /api/tasks → 201 Created
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/tasks → 201 con body válido")
    void createTask_shouldReturn201_whenValidRequest() throws Exception {
        TaskRequest request = buildTaskRequest("Implementar login", TaskStatus.TODO);

        when(taskService.create(any(TaskRequest.class))).thenReturn(tareaResponse);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Implementar login"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.assignedUser.email").value("ana@devforce.ai"))
                .andExpect(jsonPath("$.project.name").value("MiniJira"));

        verify(taskService, times(1)).create(any(TaskRequest.class));
    }

    @Test
    @DisplayName("POST /api/tasks → 400 cuando title es blank")
    void createTask_shouldReturn400_whenTitleIsBlank() throws Exception {
        TaskRequest request = buildTaskRequest("", TaskStatus.TODO);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));

        verify(taskService, never()).create(any());
    }

    @Test
    @DisplayName("POST /api/tasks → 400 cuando status es null")
    void createTask_shouldReturn400_whenStatusIsNull() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("Tarea sin status");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).create(any());
    }

    // -------------------------------------------------------------------------
    // GET /api/tasks → 200 con lista
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/tasks → 200 con lista de tareas")
    void getAllTasks_shouldReturn200_withList() throws Exception {
        TaskResponse segundaTarea = TaskResponse.builder()
                .id(2L)
                .title("Revisar PR")
                .status(TaskStatus.IN_PROGRESS)
                .build();

        when(taskService.findAll()).thenReturn(List.of(tareaResponse, segundaTarea));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[0].title").value("Implementar login"))
                .andExpect(jsonPath("$[1].status").value("IN_PROGRESS"));

        verify(taskService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /api/tasks → 200 con lista vacía")
    void getAllTasks_shouldReturn200_withEmptyList() throws Exception {
        when(taskService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // -------------------------------------------------------------------------
    // GET /api/tasks/{id} → 200 con tarea existente
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/tasks/{id} → 200 con tarea existente")
    void getTaskById_shouldReturn200_whenExists() throws Exception {
        when(taskService.findById(1L)).thenReturn(tareaResponse);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Implementar login"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.storyPoints").value(3))
                .andExpect(jsonPath("$.estimatedHours").value(4.0));

        verify(taskService, times(1)).findById(1L);
    }

    // -------------------------------------------------------------------------
    // GET /api/tasks/{id} → 404 con id inexistente
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/tasks/{id} → 404 con id inexistente")
    void getTaskById_shouldReturn404_whenNotFound() throws Exception {
        when(taskService.findById(999L))
                .thenThrow(new ResourceNotFoundException("Tarea", 999L));

        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(containsString("999")));

        verify(taskService, times(1)).findById(999L);
    }

    // -------------------------------------------------------------------------
    // PATCH /api/tasks/{id}/status → 200 con status actualizado
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PATCH /api/tasks/{id}/status → 200 con status actualizado")
    void updateStatus_shouldReturn200_withUpdatedStatus() throws Exception {
        TaskStatusRequest statusRequest = new TaskStatusRequest();
        statusRequest.setStatus(TaskStatus.IN_PROGRESS);

        TaskResponse tareaActualizada = TaskResponse.builder()
                .id(1L)
                .title("Implementar login")
                .status(TaskStatus.IN_PROGRESS)
                .build();

        when(taskService.updateStatus(eq(1L), any(TaskStatusRequest.class)))
                .thenReturn(tareaActualizada);

        mockMvc.perform(patch("/api/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(taskService, times(1)).updateStatus(eq(1L), any(TaskStatusRequest.class));
    }

    @Test
    @DisplayName("PATCH /api/tasks/{id}/status → 404 cuando tarea no existe")
    void updateStatus_shouldReturn404_whenTaskNotFound() throws Exception {
        TaskStatusRequest statusRequest = new TaskStatusRequest();
        statusRequest.setStatus(TaskStatus.DONE);

        when(taskService.updateStatus(eq(404L), any(TaskStatusRequest.class)))
                .thenThrow(new ResourceNotFoundException("Tarea", 404L));

        mockMvc.perform(patch("/api/tasks/404/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PATCH /api/tasks/{id}/status → 400 cuando status es null")
    void updateStatus_shouldReturn400_whenStatusIsNull() throws Exception {
        mockMvc.perform(patch("/api/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":null}"))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).updateStatus(any(), any());
    }

    // -------------------------------------------------------------------------
    // DELETE /api/tasks/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/tasks/{id} → 204 cuando tarea existe")
    void deleteTask_shouldReturn204_whenExists() throws Exception {
        doNothing().when(taskService).delete(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} → 404 cuando tarea no existe")
    void deleteTask_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Tarea", 999L))
                .when(taskService).delete(999L);

        mockMvc.perform(delete("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // -------------------------------------------------------------------------
    // GET /api/tasks/status/{status}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/tasks/status/TODO → 200 con tareas filtradas")
    void getByStatus_shouldReturn200_withFilteredTasks() throws Exception {
        when(taskService.findByStatus(TaskStatus.TODO)).thenReturn(List.of(tareaResponse));

        mockMvc.perform(get("/api/tasks/status/TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("TODO"));
    }

    // -------------------------------------------------------------------------
    // PUT /api/tasks/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /api/tasks/{id} → 200 con tarea actualizada")
    void updateTask_shouldReturn200_whenValid() throws Exception {
        TaskRequest request = buildTaskRequest("Login actualizado", TaskStatus.IN_PROGRESS);

        TaskResponse actualizada = TaskResponse.builder()
                .id(1L)
                .title("Login actualizado")
                .status(TaskStatus.IN_PROGRESS)
                .build();

        when(taskService.update(eq(1L), any(TaskRequest.class))).thenReturn(actualizada);

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Login actualizado"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private TaskRequest buildTaskRequest(String title, TaskStatus status) {
        TaskRequest req = new TaskRequest();
        req.setTitle(title);
        req.setStatus(status);
        req.setStoryPoints(3);
        req.setEstimatedHours(4.0);
        return req;
    }
}
