package com.scheduler.backend.domain.user.repository;

import com.scheduler.backend.domain.user.entity.User;
import com.scheduler.backend.domain.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findAllByStatusOrderByCreatedAtDesc(UserStatus status);
}
