package com.devforce.minijira.dto.response;

import com.devforce.minijira.model.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Integer storyPoints;
    private Double estimatedHours;
    private LocalDate startDate;
    private LocalDate endDate;
    private UserResponse assignedUser;
    private ProjectResponse project;
}
