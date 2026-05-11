package com.minijira.backend.dto.response;

import com.minijira.backend.model.Task;
import com.minijira.backend.model.TaskStatus;
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
    private Long assignedUserId;
    private String assignedUserName;
    private Long projectId;
    private String projectName;

    public static TaskResponse from(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .storyPoints(task.getStoryPoints())
                .estimatedHours(task.getEstimatedHours())
                .startDate(task.getStartDate())
                .endDate(task.getEndDate())
                .assignedUserId(task.getAssignedUser() != null ? task.getAssignedUser().getId() : null)
                .assignedUserName(task.getAssignedUser() != null ? task.getAssignedUser().getName() : null)
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .build();
    }
}
