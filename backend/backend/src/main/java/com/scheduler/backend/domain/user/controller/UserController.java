package com.scheduler.backend.domain.user.controller;

import com.scheduler.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal Long userId) {
        var opt = userRepository.findById(userId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var u = opt.get();
        return ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "username", u.getUsername(),
                "name", u.getName(),
                "role", u.getRole(),
                "status", u.getStatus().name()
        ));
    }

    // 내 비밀번호 변경
    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal Long userId,
            @RequestBody Map<String, String> body) {

        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "현재 비밀번호가 올바르지 않습니다."));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }
}
