package com.scheduler.backend.domain.admin.controller;

import com.scheduler.backend.domain.admin.dto.UserResponse;
import com.scheduler.backend.domain.admin.service.AdminService;
import com.scheduler.backend.domain.schedule.dto.ScheduleResponse;
import com.scheduler.backend.domain.user.entity.UserStatus;
import com.scheduler.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        adminService.deleteSchedule(id);
        return ResponseEntity.ok().build();
    }

    // 관리자가 사용자 비밀번호 변경
    @PutMapping("/users/{id}/password")
    public ResponseEntity<?> changeUserPassword(@PathVariable Long id,
                                                 @RequestBody Map<String, String> body) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setPassword(passwordEncoder.encode(body.get("newPassword")));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    // 역할 변경 (ADMIN만 가능 - SecurityConfig에서 제한)
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> changeRole(@PathVariable Long id,
                                        @RequestBody Map<String, String> body) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        String newRole = body.get("role"); // ADMIN, MANAGER, USER
        if (!List.of("ADMIN", "MANAGER", "USER").contains(newRole)) {
            return ResponseEntity.badRequest().body(Map.of("message", "유효하지 않은 역할입니다."));
        }
        user.setRole(newRole);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }
}
