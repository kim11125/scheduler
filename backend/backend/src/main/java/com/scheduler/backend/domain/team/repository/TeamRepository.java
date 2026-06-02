package com.scheduler.backend.domain.team.repository;

import com.scheduler.backend.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findAllByIsActiveOrderByNameAsc(Boolean isActive);
}
