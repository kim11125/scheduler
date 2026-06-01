package com.scheduler.backend.domain.schedule.dto;

import com.scheduler.backend.domain.schedule.entity.Schedule;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class ScheduleResponse {
    private final Long id;
    private final Long userId;
    private final String userName;
    private final String title;
    private final String category;
    private final String baseballType;
    private final LocalDate date;
    private final LocalDate endDate;
    private final String memo;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ScheduleResponse(Schedule s) {
        this.id = s.getId();
        this.userId = s.getUser().getId();
        this.userName = s.getUser().getName();
        this.title = s.getTitle();
        this.category = s.getCategory().name();
        this.baseballType = s.getBaseballType() != null ? s.getBaseballType().name() : null;
        this.date = s.getDate();
        this.endDate = s.getEndDate();
        this.memo = s.getMemo();
        this.createdAt = s.getCreatedAt();
        this.updatedAt = s.getUpdatedAt();
    }
}
