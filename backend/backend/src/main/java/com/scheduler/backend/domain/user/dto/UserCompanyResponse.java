package com.scheduler.backend.domain.user.dto;

import com.scheduler.backend.domain.user.entity.UserCompany;
import lombok.Getter;

@Getter
public class UserCompanyResponse {
    private final Long id;
    private final Long userId;
    private final String userName;
    private final Long companyId;
    private final String companyName;
    private final Boolean isPrimary;

    public UserCompanyResponse(UserCompany uc) {
        this.id = uc.getId();
        this.userId = uc.getUser().getId();
        this.userName = uc.getUser().getName();
        this.companyId = uc.getCompany().getId();
        this.companyName = uc.getCompany().getName();
        this.isPrimary = uc.getIsPrimary();
    }
}
