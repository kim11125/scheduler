package com.scheduler.backend.domain.auth.controller;

import com.scheduler.backend.domain.auth.dto.LoginRequest;
import com.scheduler.backend.domain.auth.dto.LoginResponse;
import com.scheduler.backend.domain.auth.dto.RegisterRequest;
import com.scheduler.backend.domain.auth.service.AuthService;
import com.scheduler.backend.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req,
                                               HttpServletRequest request) {
        String ip = getClientIp(request);
        return ResponseEntity.ok(authService.login(req, ip));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Long userId,
                                       @RequestParam String username,
                                       HttpServletRequest request) {
        String ip = getClientIp(request);
        authService.logout(userId, username, ip);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check-login-id")
    public ResponseEntity<?> checkLoginId(@RequestParam String loginId) {
        if (loginId == null || !loginId.matches("^[a-zA-Z0-9_-]{4,30}$")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "loginId", loginId,
                    "available", false,
                    "message", "아이디 형식이 올바르지 않습니다. (영문/숫자/언더스코어/하이픈, 4~30자)"
            ));
        }
        boolean exists = userRepository.existsByUsername(loginId);
        return ResponseEntity.ok(Map.of(
                "loginId", loginId,
                "available", !exists,
                "message", exists ? "이미 사용 중인 아이디입니다." : "사용 가능한 아이디입니다."
        ));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
