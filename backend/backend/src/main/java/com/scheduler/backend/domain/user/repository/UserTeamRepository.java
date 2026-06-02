package com.scheduler.backend.domain.user.repository;

import com.scheduler.backend.domain.user.entity.UserTeam;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserTeamRepository extends JpaRepository<UserTeam, Long> {

    @EntityGraph(attributePaths = {"team"})
    List<UserTeam> findAllByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<UserTeam> findAllByTeamId(Long teamId);

    Optional<UserTeam> findByUserIdAndTeamId(Long userId, Long teamId);

    boolean existsByUserIdAndTeamId(Long userId, Long teamId);

    @Modifying
    @Query("UPDATE UserTeam ut SET ut.isPrimary = false WHERE ut.user.id = :userId")
    void clearPrimaryByUserId(@Param("userId") Long userId);
}
