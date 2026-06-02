package com.scheduler.backend.domain.user.service;

import com.scheduler.backend.domain.user.dto.UserCompanyResponse;
import com.scheduler.backend.domain.user.dto.UserProfileResponse;
import com.scheduler.backend.domain.user.dto.UserTeamResponse;
import com.scheduler.backend.domain.user.entity.User;
import com.scheduler.backend.domain.user.entity.UserProfileChangeType;
import com.scheduler.backend.domain.user.repository.UserCompanyRepository;
import com.scheduler.backend.domain.user.repository.UserRepository;
import com.scheduler.backend.domain.user.repository.UserTeamRepository;
import com.scheduler.backend.global.storage.ProfileImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final UserTeamRepository userTeamRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileImageStorageService storageService;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<UserCompanyResponse> companies = userCompanyRepository.findAllByUserId(userId).stream()
                .map(UserCompanyResponse::new).collect(Collectors.toList());

        List<UserTeamResponse> teams = userTeamRepository.findAllByUserId(userId).stream()
                .map(UserTeamResponse::new).collect(Collectors.toList());

        return new UserProfileResponse(user).withCompanies(companies).withTeams(teams);
    }

    @Transactional
    public void updateName(Long userId, Long changedBy, String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.length() < 2 || trimmed.length() > 30) {
            throw new IllegalArgumentException("이름은 2~30자여야 합니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setName(trimmed);
        userRepository.save(user);
    }

    @Transactional
    public void changeLoginId(Long userId, String currentPassword, String newLoginId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        validateLoginIdFormat(newLoginId);

        if (user.getUsername().equals(newLoginId)) {
            throw new IllegalArgumentException("현재 아이디와 동일합니다.");
        }
        if (userRepository.existsByUsername(newLoginId)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        user.setUsername(newLoginId);
        user.setTokenVersion(user.getTokenVersion() == null ? 1 : user.getTokenVersion() + 1);
        userRepository.save(user);
    }

    @Transactional
    public void adminChangeLoginId(Long targetUserId, Long adminUserId, String newLoginId) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        validateLoginIdFormat(newLoginId);

        if (user.getUsername().equals(newLoginId)) {
            throw new IllegalArgumentException("현재 아이디와 동일합니다.");
        }
        if (userRepository.existsByUsername(newLoginId)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        user.setUsername(newLoginId);
        user.setTokenVersion(user.getTokenVersion() == null ? 1 : user.getTokenVersion() + 1);
        userRepository.save(user);
    }

    @Transactional
    public String uploadProfileImage(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String oldUrl = user.getProfileImageUrl();
        if (oldUrl != null) {
            storageService.delete(oldUrl);
        }

        String url = storageService.save(file);
        user.setProfileImageUrl(url);
        userRepository.save(user);
        return url;
    }

    @Transactional
    public void deleteProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (user.getProfileImageUrl() != null) {
            storageService.delete(user.getProfileImageUrl());
            user.setProfileImageUrl(null);
            userRepository.save(user);
        }
    }

    private void validateLoginIdFormat(String loginId) {
        if (loginId == null || !loginId.matches("^[a-zA-Z0-9_-]{4,30}$")) {
            throw new IllegalArgumentException("아이디는 영문/숫자/언더스코어/하이픈, 4~30자여야 합니다.");
        }
    }
}
