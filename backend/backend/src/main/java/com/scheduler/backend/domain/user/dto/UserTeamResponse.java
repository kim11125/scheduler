package com.scheduler.backend.domain.user.dto;

import com.scheduler.backend.domain.user.entity.UserTeam;
import lombok.Getter;

@Getter
public class UserTeamResponse {
    private final Long id;
    private final Long userId;
    private final String userName;
    private final Long teamId;
    private final String teamName;
    private final String teamCategory;
    private final Boolean isPrimary;

    public UserTeamResponse(UserTeam ut) {
        this.id = ut.getId();
        this.userId = ut.getUser().getId();
        this.userName = ut.getUser().getName();
        this.teamId = ut.getTeam().getId();
        this.teamName = ut.getTeam().getName();
        this.teamCategory = ut.getTeam().getCategory() != null ? ut.getTeam().getCategory().name() : null;
        this.isPrimary = ut.getIsPrimary();
    }
}
