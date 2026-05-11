package com.minijira.backend.dto.request;

import com.minijira.backend.model.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskStatusRequest {

    @NotNull(message = "El status es obligatorio")
    private TaskStatus status;
}
