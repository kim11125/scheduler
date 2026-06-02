package com.scheduler.backend.domain.company.dto;

import com.scheduler.backend.domain.company.entity.Company;
import com.scheduler.backend.domain.team.dto.TeamResponse;
import com.scheduler.backend.domain.user.dto.UserCompanyResponse;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class CompanyResponse {
    private final Long id;
    private final String name;
    private final String description;
    private final String logoUrl;
    private final Boolean isActive;
    private final LocalDateTime createdAt;
    private List<TeamResponse> teams;
    private List<UserCompanyResponse> users;

    public CompanyResponse(Company c) {
        this.id = c.getId();
        this.name = c.getName();
        this.description = c.getDescription();
        this.logoUrl = c.getLogoUrl();
        this.isActive = c.getIsActive();
        this.createdAt = c.getCreatedAt();
    }

    public CompanyResponse withTeams(List<TeamResponse> teams) {
        this.teams = teams;
        return this;
    }

    public CompanyResponse withUsers(List<UserCompanyResponse> users) {
        this.users = users;
        return this;
    }
}
