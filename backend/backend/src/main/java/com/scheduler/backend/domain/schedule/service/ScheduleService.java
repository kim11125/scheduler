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
    public ScheduleResponse create(Long requesterId, ScheduleRequest req) {
        // 관리자가 다른 유저 대신 추가하는 경우
        Long targetId = (req.getTargetUserId() != null) ? req.getTargetUserId() : requesterId;
        User user = userRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Schedule schedule = Schedule.builder()
                .user(user)
                .title(req.getTitle())
                .category(req.getCategory())
                .baseballType(req.getBaseballType())
                .date(req.getDate())
                .endDate(req.getEndDate())
                .memo(req.getMemo())
                .build();

        return new ScheduleResponse(scheduleRepository.save(schedule));
    }

    @Transactional
    public ScheduleResponse update(Long requesterId, Long scheduleId, ScheduleRequest req, boolean isAdmin) {
        Schedule schedule = scheduleRepository.findByIdAndDeletedAtIsNull(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        if (!isAdmin && !schedule.getUser().getId().equals(requesterId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        schedule.setTitle(req.getTitle());
        schedule.setCategory(req.getCategory());
        schedule.setBaseballType(req.getBaseballType());
        schedule.setDate(req.getDate());
        schedule.setEndDate(req.getEndDate());
        schedule.setMemo(req.getMemo());

        return new ScheduleResponse(schedule);
    }

    @Transactional
    public void delete(Long requesterId, Long scheduleId, boolean isAdmin) {
        Schedule schedule = scheduleRepository.findByIdAndDeletedAtIsNull(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        if (!isAdmin && !schedule.getUser().getId().equals(requesterId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        schedule.setDeletedAt(LocalDateTime.now());
    }
}
