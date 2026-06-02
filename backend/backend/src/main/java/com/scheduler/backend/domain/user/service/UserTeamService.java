package com.scheduler.backend.domain.user.service;

import com.scheduler.backend.domain.team.entity.Team;
import com.scheduler.backend.domain.team.repository.TeamRepository;
import com.scheduler.backend.domain.user.dto.UserTeamResponse;
import com.scheduler.backend.domain.user.entity.User;
import com.scheduler.backend.domain.user.entity.UserTeam;
import com.scheduler.backend.domain.user.repository.UserTeamRepository;
import com.scheduler.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserTeamService {

    private final UserTeamRepository userTeamRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    @Transactional(readOnly = true)
    public List<UserTeamResponse> getUserTeams(Long userId) {
        return userTeamRepository.findAllByUserId(userId).stream()
                .map(UserTeamResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserTeamResponse addTeam(Long userId, Long teamId) {
        if (userTeamRepository.existsByUserIdAndTeamId(userId, teamId)) {
            throw new IllegalArgumentException("이미 등록된 팀입니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
        UserTeam ut = UserTeam.builder().user(user).team(team).build();
        return new UserTeamResponse(userTeamRepository.save(ut));
    }

    @Transactional
    public void setPrimary(Long userId, Long teamId) {
        userTeamRepository.clearPrimaryByUserId(userId);
        UserTeam ut = userTeamRepository.findByUserIdAndTeamId(userId, teamId)
                .orElseThrow(() -> new IllegalArgumentException("등록된 팀이 아닙니다."));
        ut.setIsPrimary(true);
        userTeamRepository.save(ut);
    }

    @Transactional
    public void removeTeam(Long userId, Long teamId) {
        UserTeam ut = userTeamRepository.findByUserIdAndTeamId(userId, teamId)
                .orElseThrow(() -> new IllegalArgumentException("등록된 팀이 아닙니다."));
        userTeamRepository.delete(ut);
    }
}
