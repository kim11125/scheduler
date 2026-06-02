package com.scheduler.backend.domain.company.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CompanyRequest {
    @NotBlank
    private String name;
    private String description;
    private String logoUrl;
}
