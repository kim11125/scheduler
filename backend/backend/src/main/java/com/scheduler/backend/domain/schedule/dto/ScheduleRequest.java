package com.scheduler.backend.domain.schedule.dto;

import com.scheduler.backend.domain.schedule.entity.BaseballType;
import com.scheduler.backend.domain.schedule.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ScheduleRequest {
    @NotBlank
    private String title;
    @NotNull
    private Category category;
    private BaseballType baseballType;
    @NotNull
    private LocalDate date;
    private String memo;
}
