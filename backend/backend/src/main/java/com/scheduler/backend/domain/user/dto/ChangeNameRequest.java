package com.scheduler.backend.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ChangeNameRequest {
    @NotBlank
    private String name;
}
