package com.devforce.minijira.dto.request;

import com.devforce.minijira.model.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskStatusRequest {

    @NotNull(message = "El status es obligatorio")
    private TaskStatus status;
}
