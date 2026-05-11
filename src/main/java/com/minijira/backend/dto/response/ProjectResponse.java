package com.minijira.backend.dto.response;

import com.minijira.backend.model.Project;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;

    public static ProjectResponse from(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .build();
    }
}
