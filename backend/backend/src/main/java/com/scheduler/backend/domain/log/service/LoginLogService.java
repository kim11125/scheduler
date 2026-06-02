package com.scheduler.backend.domain.log.service;

import com.scheduler.backend.domain.log.entity.LoginLog;
import com.scheduler.backend.domain.log.repository.LoginLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final LoginLogRepository loginLogRepository;

    @Transactional
    public void record(Long userId, String username, String action, String ip) {
        loginLogRepository.save(LoginLog.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .ipAddress(ip)
                .build());
    }

    @Transactional(readOnly = true)
    public Page<LoginLog> getLogs(int page, int size) {
        return loginLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<LoginLog> getLogsByUser(Long userId, int page, int size) {
        return loginLogRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }
}
