package com.minijira.backend.dto.response;

import com.minijira.backend.model.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
