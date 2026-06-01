package com.scheduler.backend.domain.admin.service;

import com.scheduler.backend.domain.admin.dto.UserResponse;
import com.scheduler.backend.domain.schedule.dto.ScheduleResponse;
import com.scheduler.backend.domain.schedule.repository.ScheduleRepository;
import com.scheduler.backend.domain.user.entity.User;
import com.scheduler.backend.domain.user.entity.UserStatus;
import com.scheduler.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(UserResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getPendingUsers() {
        return userRepository.findAllByStatusOrderByCreatedAtDesc(UserStatus.PENDING)
                .stream().map(UserResponse::new).toList();
    }

    @Transactional
    public void setStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setStatus(status);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getAllSchedules() {
        return scheduleRepository.findAllNotDeleted()
                .stream().map(ScheduleResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getUserSchedules(Long userId) {
        return scheduleRepository.findByUserIdAndNotDeleted(userId)
                .stream().map(ScheduleResponse::new).toList();
    }

    @Transactional
    public void deleteSchedule(Long scheduleId) {
        var schedule = scheduleRepository.findByIdAndDeletedAtIsNull(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));
        schedule.setDeletedAt(java.time.LocalDateTime.now());
    }
}
