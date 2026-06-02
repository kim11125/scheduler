package com.scheduler.backend.domain.team.dto;

import com.scheduler.backend.domain.schedule.entity.Category;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TeamRequest {
    @NotBlank
    private String name;
    private Category category;
    private String description;
    private String logoUrl;
}
