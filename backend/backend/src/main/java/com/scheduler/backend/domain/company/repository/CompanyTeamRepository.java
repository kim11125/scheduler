package com.scheduler.backend.domain.company.repository;

import com.scheduler.backend.domain.company.entity.CompanyTeam;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyTeamRepository extends JpaRepository<CompanyTeam, Long> {

    @EntityGraph(attributePaths = {"team"})
    List<CompanyTeam> findAllByCompanyId(Long companyId);

    @EntityGraph(attributePaths = {"company"})
    List<CompanyTeam> findAllByTeamId(Long teamId);

    Optional<CompanyTeam> findByCompanyIdAndTeamId(Long companyId, Long teamId);

    boolean existsByCompanyIdAndTeamId(Long companyId, Long teamId);
}
