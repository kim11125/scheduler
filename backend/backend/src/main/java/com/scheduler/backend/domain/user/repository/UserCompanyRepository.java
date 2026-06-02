package com.scheduler.backend.domain.user.repository;

import com.scheduler.backend.domain.user.entity.UserCompany;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserCompanyRepository extends JpaRepository<UserCompany, Long> {

    @EntityGraph(attributePaths = {"company"})
    List<UserCompany> findAllByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<UserCompany> findAllByCompanyId(Long companyId);

    Optional<UserCompany> findByUserIdAndCompanyId(Long userId, Long companyId);

    boolean existsByUserIdAndCompanyId(Long userId, Long companyId);

    @Modifying
    @Query("UPDATE UserCompany uc SET uc.isPrimary = false WHERE uc.user.id = :userId")
    void clearPrimaryByUserId(@Param("userId") Long userId);
}
