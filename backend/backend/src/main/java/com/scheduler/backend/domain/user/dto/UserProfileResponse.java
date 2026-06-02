package com.scheduler.backend.domain.user.dto;

import com.scheduler.backend.domain.user.entity.User;
import lombok.Getter;

import java.util.List;

@Getter
public class UserProfileResponse {
    private final Long id;
    private final String loginId;
    private final String name;
    private final String profileImageUrl;
    private List<UserCompanyResponse> companies;
    private List<UserTeamResponse> teams;

    public UserProfileResponse(User u) {
        this.id = u.getId();
        this.loginId = u.getUsername();
        this.name = u.getName();
        this.profileImageUrl = u.getProfileImageUrl();
    }

    public UserProfileResponse withCompanies(List<UserCompanyResponse> companies) {
        this.companies = companies;
        return this;
    }

    public UserProfileResponse withTeams(List<UserTeamResponse> teams) {
        this.teams = teams;
        return this;
    }
}
