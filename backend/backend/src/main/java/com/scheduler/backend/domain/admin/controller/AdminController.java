package com.scheduler.backend.domain.admin.controller;

import com.scheduler.backend.domain.admin.dto.UserResponse;
import com.scheduler.backend.domain.admin.service.AdminService;
import com.scheduler.backend.domain.schedule.dto.ScheduleResponse;
import com.scheduler.backend.domain.user.entity.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/pending")
    public ResponseEntity<List<UserResponse>> getPendingUsers() {
        return ResponseEntity.ok(adminService.getPendingUsers());
    }

    @PutMapping("/users/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id) {
        adminService.setStatus(id, UserStatus.ACTIVE);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id) {
        adminService.setStatus(id, UserStatus.REJECTED);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/disable")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        adminService.setStatus(id, UserStatus.DISABLED);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        adminService.setStatus(id, UserStatus.ACTIVE);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/schedules")
    public ResponseEntity<List<ScheduleResponse>> getAllSchedules() {
        return ResponseEntity.ok(adminService.getAllSchedules());
    }

    @GetMapping("/schedules/user/{userId}")
    public ResponseEntity<List<ScheduleResponse>> getUserSchedules(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUserSchedules(userId));
    }

    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        // admin도 soft delete
        adminService.getAllSchedules(); // placeholder - 아래 별도 구현 가능
        return ResponseEntity.ok().build();
    }
}
