package com.scheduler.backend.domain.company.controller;

import com.scheduler.backend.domain.company.dto.CompanyRequest;
import com.scheduler.backend.domain.company.dto.CompanyResponse;
import com.scheduler.backend.domain.company.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/api/companies")
    public ResponseEntity<List<CompanyResponse>> findAll() {
        return ResponseEntity.ok(companyService.findAll());
    }

    @GetMapping("/api/companies/{id}")
    public ResponseEntity<CompanyResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.findById(id));
    }

    @PostMapping("/api/admin/companies")
    public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.ok(companyService.create(request));
    }

    @PutMapping("/api/admin/companies/{id}")
    public ResponseEntity<CompanyResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.ok(companyService.update(id, request));
    }

    @PatchMapping("/api/admin/companies/{id}/active")
    public ResponseEntity<Void> setActive(@PathVariable Long id,
                                           @RequestBody Map<String, Boolean> body) {
        companyService.setActive(id, Boolean.TRUE.equals(body.get("active")));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/admin/companies/{companyId}/teams/{teamId}")
    public ResponseEntity<Void> addTeam(@PathVariable Long companyId, @PathVariable Long teamId) {
        companyService.addTeam(companyId, teamId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/admin/companies/{companyId}/teams/{teamId}")
    public ResponseEntity<Void> removeTeam(@PathVariable Long companyId, @PathVariable Long teamId) {
        companyService.removeTeam(companyId, teamId);
        return ResponseEntity.ok().build();
    }
}
