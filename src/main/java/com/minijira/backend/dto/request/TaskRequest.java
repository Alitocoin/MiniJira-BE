package com.minijira.backend.dto.request;

import com.minijira.backend.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskRequest {

    @NotBlank(message = "El título es obligatorio")
    private String title;

    private String description;

    @NotNull(message = "El status es obligatorio")
    private TaskStatus status;

    private Integer storyPoints;

    private Double estimatedHours;

    private LocalDate startDate;

    private LocalDate endDate;

    private Long assignedUserId;

    @NotNull(message = "El proyecto es obligatorio")
    private Long projectId;
}
