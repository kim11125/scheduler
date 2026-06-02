package com.scheduler.backend.domain.team.dto;

import com.scheduler.backend.domain.company.dto.CompanyResponse;
import com.scheduler.backend.domain.team.entity.Team;
import com.scheduler.backend.domain.user.dto.UserTeamResponse;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class TeamResponse {
    private final Long id;
    private final String name;
    private final String category;
    private final String description;
    private final String logoUrl;
    private final Boolean isActive;
    private final LocalDateTime createdAt;
    private List<CompanyResponse> companies;
    private List<UserTeamResponse> users;

    public TeamResponse(Team t) {
        this.id = t.getId();
        this.name = t.getName();
        this.category = t.getCategory() != null ? t.getCategory().name() : null;
        this.description = t.getDescription();
        this.logoUrl = t.getLogoUrl();
        this.isActive = t.getIsActive();
        this.createdAt = t.getCreatedAt();
    }

    public TeamResponse withCompanies(List<CompanyResponse> companies) {
        this.companies = companies;
        return this;
    }

    public TeamResponse withUsers(List<UserTeamResponse> users) {
        this.users = users;
        return this;
    }
}
