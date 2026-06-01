package com.scheduler.backend.domain.auth.service;

import com.scheduler.backend.domain.auth.dto.LoginRequest;
import com.scheduler.backend.domain.auth.dto.LoginResponse;
import com.scheduler.backend.domain.auth.dto.RegisterRequest;
import com.scheduler.backend.domain.user.entity.User;
import com.scheduler.backend.domain.user.entity.UserStatus;
import com.scheduler.backend.domain.user.repository.UserRepository;
import com.scheduler.backend.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public void register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .role("USER")
                .status(UserStatus.PENDING)
                .build();
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtProvider.generate(user.getId(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getName(),
                user.getRole(), user.getStatus().name());
    }
}
