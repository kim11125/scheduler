package com.scheduler.backend.domain.schedule.service;

import com.scheduler.backend.domain.schedule.dto.ScheduleRequest;
import com.scheduler.backend.domain.schedule.dto.ScheduleResponse;
import com.scheduler.backend.domain.schedule.entity.Schedule;
import com.scheduler.backend.domain.schedule.repository.ScheduleRepository;
import com.scheduler.backend.domain.user.entity.User;
import com.scheduler.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getMySchedules(Long userId) {
        return scheduleRepository.findByUserIdAndNotDeleted(userId)
                .stream().map(ScheduleResponse::new).toList();
    }

    @Transactional
    public ScheduleResponse create(Long userId, ScheduleRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Schedule schedule = Schedule.builder()
                .user(user)
                .title(req.getTitle())
                .category(req.getCategory())
                .baseballType(req.getBaseballType())
                .date(req.getDate())
                .memo(req.getMemo())
                .build();

        return new ScheduleResponse(scheduleRepository.save(schedule));
    }

    @Transactional
    public ScheduleResponse update(Long userId, Long scheduleId, ScheduleRequest req) {
        Schedule schedule = scheduleRepository.findByIdAndDeletedAtIsNull(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        if (!schedule.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        schedule.setTitle(req.getTitle());
        schedule.setCategory(req.getCategory());
        schedule.setBaseballType(req.getBaseballType());
        schedule.setDate(req.getDate());
        schedule.setMemo(req.getMemo());

        return new ScheduleResponse(schedule);
    }

    @Transactional
    public void delete(Long userId, Long scheduleId) {
        Schedule schedule = scheduleRepository.findByIdAndDeletedAtIsNull(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        if (!schedule.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        schedule.setDeletedAt(LocalDateTime.now());
    }
}
