package com.scheduler.backend.domain.schedule.controller;

import com.scheduler.backend.domain.schedule.dto.ScheduleRequest;
import com.scheduler.backend.domain.schedule.dto.ScheduleResponse;
import com.scheduler.backend.domain.schedule.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getMySchedules(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(scheduleService.getMySchedules(userId));
    }

    @PostMapping
    public ResponseEntity<ScheduleResponse> create(@AuthenticationPrincipal Long userId,
                                                    @Valid @RequestBody ScheduleRequest req) {
        return ResponseEntity.ok(scheduleService.create(userId, req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleResponse> update(@AuthenticationPrincipal Long userId,
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody ScheduleRequest req) {
        return ResponseEntity.ok(scheduleService.update(userId, id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Long userId,
                                       @PathVariable Long id) {
        scheduleService.delete(userId, id);
        return ResponseEntity.ok().build();
    }
}
