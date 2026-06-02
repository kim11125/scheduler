package com.scheduler.backend.domain.log.controller;

import com.scheduler.backend.domain.log.entity.LoginLog;
import com.scheduler.backend.domain.log.service.LoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class LoginLogController {

    private final LoginLogService loginLogService;

    @GetMapping
    public ResponseEntity<Page<LoginLog>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(loginLogService.getLogs(page, size));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<LoginLog>> getLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(loginLogService.getLogsByUser(userId, page, size));
    }
}
