package com.scheduler.backend.domain.log.repository;

import com.scheduler.backend.domain.log.entity.LoginLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
    Page<LoginLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<LoginLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
