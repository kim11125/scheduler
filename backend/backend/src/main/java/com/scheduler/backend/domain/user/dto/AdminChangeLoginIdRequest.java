package com.scheduler.backend.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AdminChangeLoginIdRequest {
    @NotBlank
    private String newLoginId;
}
