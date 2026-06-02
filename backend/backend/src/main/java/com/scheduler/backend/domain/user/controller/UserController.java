package com.scheduler.backend.domain.user.controller;

import com.scheduler.backend.domain.user.dto.ChangeLoginIdRequest;
import com.scheduler.backend.domain.user.dto.ChangeNameRequest;
import com.scheduler.backend.domain.user.dto.UserProfileResponse;
import com.scheduler.backend.domain.user.repository.UserRepository;
import com.scheduler.backend.domain.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileService userProfileService;

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

    // 내 프로필 조회
    @GetMapping("/me/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(userProfileService.getProfile(userId));
    }

    // 내 프로필 이름 변경
    @PatchMapping("/me/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal Long userId,
                                            @Valid @RequestBody ChangeNameRequest request) {
        userProfileService.updateName(userId, userId, request.getName());
        return ResponseEntity.ok(Map.of("message", "이름이 변경되었습니다."));
    }

    // 내 로그인 아이디 변경
    @PatchMapping("/me/login-id")
    public ResponseEntity<?> changeLoginId(@AuthenticationPrincipal Long userId,
                                            @Valid @RequestBody ChangeLoginIdRequest request) {
        userProfileService.changeLoginId(userId, request.getCurrentPassword(), request.getNewLoginId());
        return ResponseEntity.ok(Map.of(
                "message", "로그인 아이디가 변경되었습니다.",
                "loginIdChanged", true,
                "forceLogout", true
        ));
    }

    // 내 프로필 이미지 업로드 (신규)
    @PostMapping("/me/profile-image")
    public ResponseEntity<?> uploadProfileImage(@AuthenticationPrincipal Long userId,
                                                 @RequestParam("file") MultipartFile file) {
        String url = userProfileService.uploadProfileImage(userId, file);
        return ResponseEntity.ok(Map.of("profileImageUrl", url));
    }

    // 내 프로필 이미지 교체
    @PutMapping("/me/profile-image")
    public ResponseEntity<?> updateProfileImage(@AuthenticationPrincipal Long userId,
                                                 @RequestParam("file") MultipartFile file) {
        String url = userProfileService.uploadProfileImage(userId, file);
        return ResponseEntity.ok(Map.of("profileImageUrl", url));
    }

    // 내 프로필 이미지 삭제
    @DeleteMapping("/me/profile-image")
    public ResponseEntity<?> deleteProfileImage(@AuthenticationPrincipal Long userId) {
        userProfileService.deleteProfileImage(userId);
        return ResponseEntity.ok().build();
    }
}
