package com.scheduler.backend.domain.admin.dto;

import com.scheduler.backend.domain.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponse {
    private final Long id;
    private final String username;
    private final String name;
    private final String role;
    private final String status;
    private final LocalDateTime createdAt;

    public UserResponse(User u) {
        this.id = u.getId();
        this.username = u.getUsername();
        this.name = u.getName();
        this.role = u.getRole().name();
        this.status = u.getStatus().name();
        this.createdAt = u.getCreatedAt();
    }
}
