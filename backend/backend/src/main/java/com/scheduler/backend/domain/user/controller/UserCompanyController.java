package com.scheduler.backend.domain.user.controller;

import com.scheduler.backend.domain.user.dto.UserCompanyResponse;
import com.scheduler.backend.domain.user.service.UserCompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users/{userId}/companies")
@RequiredArgsConstructor
public class UserCompanyController {

    private final UserCompanyService userCompanyService;

    @GetMapping
    public ResponseEntity<List<UserCompanyResponse>> getUserCompanies(@PathVariable Long userId) {
        return ResponseEntity.ok(userCompanyService.getUserCompanies(userId));
    }

    @PostMapping("/{companyId}")
    public ResponseEntity<UserCompanyResponse> addCompany(@PathVariable Long userId,
                                                           @PathVariable Long companyId) {
        return ResponseEntity.ok(userCompanyService.addCompany(userId, companyId));
    }

    @PatchMapping("/{companyId}/primary")
    public ResponseEntity<Void> setPrimary(@PathVariable Long userId,
                                            @PathVariable Long companyId) {
        userCompanyService.setPrimary(userId, companyId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> removeCompany(@PathVariable Long userId,
                                               @PathVariable Long companyId) {
        userCompanyService.removeCompany(userId, companyId);
        return ResponseEntity.ok().build();
    }
}
