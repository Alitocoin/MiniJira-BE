package com.minijira.backend.controller;

import com.minijira.backend.dto.request.TaskRequest;
import com.minijira.backend.dto.request.TaskStatusRequest;
import com.minijira.backend.dto.response.TaskResponse;
import com.minijira.backend.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAll() {
        return ResponseEntity.ok(taskService.findAll());
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        TaskResponse created = taskService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody TaskStatusRequest request) {
        return ResponseEntity.ok(taskService.updateStatus(id, request));
    }
}
