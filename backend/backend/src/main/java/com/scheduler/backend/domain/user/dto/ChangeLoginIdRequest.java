package com.scheduler.backend.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ChangeLoginIdRequest {
    @NotBlank
    private String currentPassword;
    @NotBlank
    private String newLoginId;
}
