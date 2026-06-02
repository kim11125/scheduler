package com.scheduler.backend.domain.log.repository;

import com.scheduler.backend.domain.log.entity.UserProfileChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserProfileChangeLogRepository extends JpaRepository<UserProfileChangeLog, Long> {
    List<UserProfileChangeLog> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
