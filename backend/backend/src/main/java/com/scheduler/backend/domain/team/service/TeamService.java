package com.scheduler.backend.domain.team.service;

import com.scheduler.backend.domain.company.dto.CompanyResponse;
import com.scheduler.backend.domain.company.repository.CompanyTeamRepository;
import com.scheduler.backend.domain.team.dto.TeamRequest;
import com.scheduler.backend.domain.team.dto.TeamResponse;
import com.scheduler.backend.domain.team.entity.Team;
import com.scheduler.backend.domain.team.repository.TeamRepository;
import com.scheduler.backend.domain.user.dto.UserTeamResponse;
import com.scheduler.backend.domain.user.repository.UserTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final CompanyTeamRepository companyTeamRepository;
    private final UserTeamRepository userTeamRepository;

    @Transactional(readOnly = true)
    public List<TeamResponse> findAll() {
        return teamRepository.findAll().stream()
                .map(TeamResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeamResponse findById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        List<CompanyResponse> companies = companyTeamRepository.findAllByTeamId(id).stream()
                .map(ct -> new CompanyResponse(ct.getCompany()))
                .collect(Collectors.toList());

        List<UserTeamResponse> users = userTeamRepository.findAllByTeamId(id).stream()
                .map(UserTeamResponse::new)
                .collect(Collectors.toList());

        return new TeamResponse(team).withCompanies(companies).withUsers(users);
    }

    @Transactional
    public TeamResponse create(TeamRequest request) {
        Team team = Team.builder()
                .name(request.getName())
                .category(request.getCategory())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .build();
        return new TeamResponse(teamRepository.save(team));
    }

    @Transactional
    public TeamResponse update(Long id, TeamRequest request) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
        team.setName(request.getName());
        team.setCategory(request.getCategory());
        team.setDescription(request.getDescription());
        team.setLogoUrl(request.getLogoUrl());
        return new TeamResponse(teamRepository.save(team));
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
        team.setIsActive(active);
        teamRepository.save(team);
    }
}
