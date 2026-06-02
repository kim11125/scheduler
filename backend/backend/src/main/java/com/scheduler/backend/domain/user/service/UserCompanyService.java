package com.scheduler.backend.domain.user.service;

import com.scheduler.backend.domain.company.entity.Company;
import com.scheduler.backend.domain.company.repository.CompanyRepository;
import com.scheduler.backend.domain.user.dto.UserCompanyResponse;
import com.scheduler.backend.domain.user.entity.User;
import com.scheduler.backend.domain.user.entity.UserCompany;
import com.scheduler.backend.domain.user.repository.UserCompanyRepository;
import com.scheduler.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCompanyService {

    private final UserCompanyRepository userCompanyRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public List<UserCompanyResponse> getUserCompanies(Long userId) {
        return userCompanyRepository.findAllByUserId(userId).stream()
                .map(UserCompanyResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserCompanyResponse addCompany(Long userId, Long companyId) {
        if (userCompanyRepository.existsByUserIdAndCompanyId(userId, companyId)) {
            throw new IllegalArgumentException("이미 등록된 회사입니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("회사를 찾을 수 없습니다."));
        UserCompany uc = UserCompany.builder().user(user).company(company).build();
        return new UserCompanyResponse(userCompanyRepository.save(uc));
    }

    @Transactional
    public void setPrimary(Long userId, Long companyId) {
        userCompanyRepository.clearPrimaryByUserId(userId);
        UserCompany uc = userCompanyRepository.findByUserIdAndCompanyId(userId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("등록된 회사가 아닙니다."));
        uc.setIsPrimary(true);
        userCompanyRepository.save(uc);
    }

    @Transactional
    public void removeCompany(Long userId, Long companyId) {
        UserCompany uc = userCompanyRepository.findByUserIdAndCompanyId(userId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("등록된 회사가 아닙니다."));
        userCompanyRepository.delete(uc);
    }
}
