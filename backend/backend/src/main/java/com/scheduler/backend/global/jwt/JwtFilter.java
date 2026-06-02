package com.scheduler.backend.global.jwt;

import com.scheduler.backend.domain.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtProvider.validate(token)) {
                Long userId = jwtProvider.getUserId(token);
                int tokenVersion = jwtProvider.getTokenVersion(token);

                // tokenVersion 검증
                var userOpt = userRepository.findById(userId);
                if (userOpt.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "사용자를 찾을 수 없습니다.");
                    return;
                }
                Integer dbVersion = userOpt.get().getTokenVersion();
                if (dbVersion == null) dbVersion = 0;
                if (tokenVersion != dbVersion) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 만료되었습니다. 다시 로그인하세요.");
                    return;
                }

                String role = jwtProvider.getRole(token);
                var auth = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }
}
