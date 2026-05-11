package com.minijira.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String token;
    private String type;
    private Long userId;
    private String username;
}
