package com.scheduler.backend.domain.company.repository;

import com.scheduler.backend.domain.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByName(String name);
    Optional<Company> findByName(String name);
    List<Company> findAllByIsActiveOrderByNameAsc(Boolean isActive);
}
