# 03. 데이터베이스 설계

## 1. 테이블 목록

| 테이블명 | 설명 |
|----------|------|
| `users` | 사용자 계정 정보 |
| `schedules` | 개인 일정 |
| `audit_logs` | 관리자/시스템 행위 감사 로그 |

---

## 2. ERD (텍스트)

```
users (1) ──── (N) schedules
  id                user_id (FK)

users (1) ──── (N) audit_logs
  id                actor_user_id (FK, nullable)
```

---

## 3. 테이블 상세 정의

### 3-1. users

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| `id` | `BIGSERIAL` | PK | 사용자 고유 ID |
| `username` | `VARCHAR(20)` | NOT NULL, UNIQUE | 로그인 아이디. 영문 소문자+숫자, 4~20자 |
| `password_hash` | `VARCHAR(100)` | NOT NULL | BCrypt 해시값 |
| `name` | `VARCHAR(50)` | NOT NULL | 표시 이름 |
| `role` | `VARCHAR(10)` | NOT NULL, DEFAULT 'USER' | 권한: `ADMIN`, `USER` |
| `status` | `VARCHAR(10)` | NOT NULL, DEFAULT 'PENDING' | 상태: `PENDING`, `ACTIVE`, `REJECTED`, `DISABLED` |
| `last_login_at` | `TIMESTAMP` | NULL | 마지막 로그인 시각 |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT NOW() | 생성일시 |
| `updated_at` | `TIMESTAMP` | NOT NULL, DEFAULT NOW() | 수정일시 |

**인덱스:**
- `idx_users_username` ON `username` (로그인 조회용)
- `idx_users_status` ON `status` (PENDING 목록 조회용)

**Soft Delete:** 미적용. 계정 삭제는 `status = DISABLED` 처리.

---

### 3-2. schedules

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| `id` | `BIGSERIAL` | PK | 일정 고유 ID |
| `user_id` | `BIGINT` | NOT NULL, FK → users(id) | 작성자 |
| `title` | `VARCHAR(100)` | NOT NULL | 일정 제목 |
| `category` | `VARCHAR(20)` | NOT NULL | 카테고리 enum |
| `baseball_type` | `VARCHAR(10)` | NULL | 홈/원정. category=BASEBALL일 때만 값 존재 |
| `date` | `DATE` | NOT NULL | 일정 날짜 (날짜만, 시간 없음) |
| `memo` | `TEXT` | NULL | 메모. 빈 값 허용 |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT NOW() | 생성일시 |
| `updated_at` | `TIMESTAMP` | NOT NULL, DEFAULT NOW() | 수정일시 |
| `deleted_at` | `TIMESTAMP` | NULL | 삭제일시. NULL이면 유효한 일정 |

**category enum 값:**

| 값 | 설명 |
|----|------|
| `BASEBALL` | 야구 |
| `BASKETBALL` | 농구 |
| `SOCCER` | 축구 |
| `WOMENS_VOLLEYBALL` | 여자배구 |
| `MENS_VOLLEYBALL` | 남자배구 |
| `ETC` | 기타 |

**baseball_type enum 값:**

| 값 | 설명 |
|----|------|
| `HOME` | 홈 |
| `AWAY` | 원정 |

**비즈니스 규칙:**
- `category = BASEBALL`이면 `baseball_type`은 반드시 값이 있어야 한다 (애플리케이션 레벨 검증).
- `category ≠ BASEBALL`이면 `baseball_type`은 항상 `NULL`로 저장한다.
- 같은 날짜에 여러 일정 등록이 허용된다. 유니크 제약 없음.
- Soft Delete 적용: `deleted_at IS NULL`인 행만 유효한 일정으로 취급.

**인덱스:**
- `idx_schedules_user_id` ON `user_id` (사용자별 조회용)
- `idx_schedules_date` ON `date` (날짜별 조회용)
- `idx_schedules_user_id_date` ON `(user_id, date)` (복합 조회용)
- `idx_schedules_deleted_at` ON `deleted_at` (소프트 딜리트 필터용)

---

### 3-3. audit_logs

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| `id` | `BIGSERIAL` | PK | 로그 고유 ID |
| `actor_user_id` | `BIGINT` | NULL, FK → users(id) | 행위자 (시스템 행위 시 NULL) |
| `action_type` | `VARCHAR(50)` | NOT NULL | 행위 유형 enum |
| `target_type` | `VARCHAR(20)` | NULL | 대상 엔티티 유형: `USER`, `SCHEDULE` |
| `target_id` | `BIGINT` | NULL | 대상 엔티티 ID |
| `message` | `TEXT` | NULL | 부가 설명 |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT NOW() | 발생일시 |

**action_type 값 목록:**

| 값 | 설명 |
|----|------|
| `USER_REGISTERED` | 회원가입 신청 |
| `USER_APPROVED` | 계정 승인 |
| `USER_REJECTED` | 계정 거절 |
| `USER_DISABLED` | 계정 비활성화 |
| `USER_ACTIVATED` | 계정 재활성화 |
| `SCHEDULE_CREATED` | 일정 생성 |
| `SCHEDULE_UPDATED` | 일정 수정 |
| `SCHEDULE_DELETED` | 일정 삭제 |

**인덱스:**
- `idx_audit_logs_actor_user_id` ON `actor_user_id`
- `idx_audit_logs_action_type` ON `action_type`
- `idx_audit_logs_created_at` ON `created_at`

---

## 4. Soft Delete 적용 범위

| 테이블 | Soft Delete | 방식 |
|--------|-------------|------|
| `users` | 미적용 | `status = DISABLED` 로 비활성화 |
| `schedules` | 적용 | `deleted_at` 컬럼. NULL이면 유효, 값이 있으면 삭제됨 |
| `audit_logs` | 미적용 | 감사 로그는 삭제하지 않음 |

---

## 5. 초기 관리자 계정 생성 전략

Flyway V2 migration SQL에서 초기 ADMIN 계정을 직접 INSERT한다.

```sql
-- V2__init_admin.sql
INSERT INTO users (username, password_hash, name, role, status, created_at, updated_at)
VALUES (
  'admin',
  '$2a$12$<bcrypt_hash_of_initial_password>',
  '관리자',
  'ADMIN',
  'ACTIVE',
  NOW(),
  NOW()
);
```

**주의사항:**
- `password_hash` 값은 배포 전 BCrypt로 직접 생성하여 SQL에 넣는다.
- 초기 비밀번호는 반드시 배포 직후 변경해야 한다. (비밀번호 변경 기능은 향후 구현)
- `V2__init_admin.sql`은 운영 환경에서 한 번만 실행된다. 중복 실행 방지를 위해 `INSERT ... ON CONFLICT DO NOTHING` 사용을 권장한다.

```sql
INSERT INTO users (username, password_hash, name, role, status, created_at, updated_at)
VALUES (
  'admin',
  '$2a$12$<bcrypt_hash>',
  '관리자',
  'ADMIN',
  'ACTIVE',
  NOW(),
  NOW()
) ON CONFLICT (username) DO NOTHING;
```

---

## 6. Flyway Migration 계획

| 파일명 | 내용 |
|--------|------|
| `V1__init_schema.sql` | `users`, `schedules`, `audit_logs` 테이블 생성, 인덱스 생성 |
| `V2__init_admin.sql` | 초기 ADMIN 계정 INSERT |

추후 스키마 변경 시 `V3__`, `V4__` 형태로 순차 추가한다. 기존 migration 파일은 절대 수정하지 않는다.

---

## 7. 확인 필요 사항

없음. 모든 항목이 확정되었음.
