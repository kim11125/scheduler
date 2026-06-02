package com.scheduler.backend.domain.user.controller;

import com.scheduler.backend.domain.user.dto.UserTeamResponse;
import com.scheduler.backend.domain.user.service.UserTeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users/{userId}/teams")
@RequiredArgsConstructor
public class UserTeamController {

    private final UserTeamService userTeamService;

    @GetMapping
    public ResponseEntity<List<UserTeamResponse>> getUserTeams(@PathVariable Long userId) {
        return ResponseEntity.ok(userTeamService.getUserTeams(userId));
    }

    @PostMapping("/{teamId}")
    public ResponseEntity<UserTeamResponse> addTeam(@PathVariable Long userId,
                                                     @PathVariable Long teamId) {
        return ResponseEntity.ok(userTeamService.addTeam(userId, teamId));
    }

    @PatchMapping("/{teamId}/primary")
    public ResponseEntity<Void> setPrimary(@PathVariable Long userId,
                                            @PathVariable Long teamId) {
        userTeamService.setPrimary(userId, teamId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> removeTeam(@PathVariable Long userId,
                                            @PathVariable Long teamId) {
        userTeamService.removeTeam(userId, teamId);
        return ResponseEntity.ok().build();
    }
}
