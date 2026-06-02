package com.scheduler.backend.domain.company.service;

import com.scheduler.backend.domain.company.dto.CompanyRequest;
import com.scheduler.backend.domain.company.dto.CompanyResponse;
import com.scheduler.backend.domain.company.entity.Company;
import com.scheduler.backend.domain.company.entity.CompanyTeam;
import com.scheduler.backend.domain.company.repository.CompanyRepository;
import com.scheduler.backend.domain.company.repository.CompanyTeamRepository;
import com.scheduler.backend.domain.team.entity.Team;
import com.scheduler.backend.domain.team.repository.TeamRepository;
import com.scheduler.backend.domain.user.dto.UserCompanyResponse;
import com.scheduler.backend.domain.user.repository.UserCompanyRepository;
import com.scheduler.backend.domain.team.dto.TeamResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyTeamRepository companyTeamRepository;
    private final TeamRepository teamRepository;
    private final UserCompanyRepository userCompanyRepository;

    @Transactional(readOnly = true)
    public List<CompanyResponse> findAll() {
        return companyRepository.findAll().stream()
                .map(CompanyResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CompanyResponse findById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회사를 찾을 수 없습니다."));

        List<TeamResponse> teams = companyTeamRepository.findAllByCompanyId(id).stream()
                .map(ct -> new TeamResponse(ct.getTeam()))
                .collect(Collectors.toList());

        List<UserCompanyResponse> users = userCompanyRepository.findAllByCompanyId(id).stream()
                .map(UserCompanyResponse::new)
                .collect(Collectors.toList());

        return new CompanyResponse(company).withTeams(teams).withUsers(users);
    }

    @Transactional
    public CompanyResponse create(CompanyRequest request) {
        if (companyRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("이미 존재하는 회사명입니다.");
        }
        Company company = Company.builder()
                .name(request.getName())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .build();
        return new CompanyResponse(companyRepository.save(company));
    }

    @Transactional
    public CompanyResponse update(Long id, CompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회사를 찾을 수 없습니다."));
        if (!company.getName().equals(request.getName()) && companyRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("이미 존재하는 회사명입니다.");
        }
        company.setName(request.getName());
        company.setDescription(request.getDescription());
        company.setLogoUrl(request.getLogoUrl());
        return new CompanyResponse(companyRepository.save(company));
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회사를 찾을 수 없습니다."));
        company.setIsActive(active);
        companyRepository.save(company);
    }

    @Transactional
    public void addTeam(Long companyId, Long teamId) {
        if (companyTeamRepository.existsByCompanyIdAndTeamId(companyId, teamId)) {
            throw new IllegalArgumentException("이미 연결된 팀입니다.");
        }
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("회사를 찾을 수 없습니다."));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
        CompanyTeam ct = CompanyTeam.builder().company(company).team(team).build();
        companyTeamRepository.save(ct);
    }

    @Transactional
    public void removeTeam(Long companyId, Long teamId) {
        CompanyTeam ct = companyTeamRepository.findByCompanyIdAndTeamId(companyId, teamId)
                .orElseThrow(() -> new IllegalArgumentException("연결된 팀이 없습니다."));
        companyTeamRepository.delete(ct);
    }
}
