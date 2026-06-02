package com.scheduler.backend.domain.team.controller;

import com.scheduler.backend.domain.team.dto.TeamRequest;
import com.scheduler.backend.domain.team.dto.TeamResponse;
import com.scheduler.backend.domain.team.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/api/teams")
    public ResponseEntity<List<TeamResponse>> findAll() {
        return ResponseEntity.ok(teamService.findAll());
    }

    @GetMapping("/api/teams/{id}")
    public ResponseEntity<TeamResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.findById(id));
    }

    @PostMapping("/api/admin/teams")
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody TeamRequest request) {
        return ResponseEntity.ok(teamService.create(request));
    }

    @PutMapping("/api/admin/teams/{id}")
    public ResponseEntity<TeamResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody TeamRequest request) {
        return ResponseEntity.ok(teamService.update(id, request));
    }

    @PatchMapping("/api/admin/teams/{id}/active")
    public ResponseEntity<Void> setActive(@PathVariable Long id,
                                           @RequestBody Map<String, Boolean> body) {
        teamService.setActive(id, Boolean.TRUE.equals(body.get("active")));
        return ResponseEntity.ok().build();
    }
}
