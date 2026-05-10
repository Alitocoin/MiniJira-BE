package com.minijira.backend.dto;

import com.minijira.backend.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {

    @NotNull(message = "El status es obligatorio")
    private TaskStatus status;

    public StatusUpdateRequest() {}

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
}
