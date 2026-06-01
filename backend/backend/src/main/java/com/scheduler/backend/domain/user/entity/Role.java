package com.scheduler.backend.domain.user.entity;

public enum Role {
    ADMIN,    // 최고 관리자 - 모든 권한 + 관리자 임명
    MANAGER,  // 일반 관리자 - 회원/일정 관리 (임명 불가)
    USER      // 일반 사용자
}
