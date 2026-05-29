# 06. 권한 정책

## 1. Role 정의

| Role | 설명 |
|------|------|
| `ADMIN` | 관리자. 전체 사용자 관리 및 전체 일정 조회/수정/삭제 가능. 본인 일정 등록도 가능 |
| `USER` | 일반 사용자. 승인 후 본인 일정만 관리 가능 |

- Role은 가입 시 자동으로 `USER`로 설정된다.
- `ADMIN`은 Flyway 초기 데이터 INSERT 또는 DB 직접 수정으로만 부여된다. 별도 Role 변경 API는 MVP에 포함하지 않는다.

---

## 2. User Status 정의

| Status | 설명 | 전이 가능 상태 |
|--------|------|----------------|
| `PENDING` | 가입 신청 후 승인 대기 | → ACTIVE, → REJECTED |
| `ACTIVE` | 승인 완료. 서비스 이용 가능 | → DISABLED |
| `REJECTED` | 가입 거절 | (없음, 최종 상태) |
| `DISABLED` | 비활성화. 로그인은 가능하나 서비스 이용 불가 | → ACTIVE |

**Status 전이 규칙:**
- PENDING → ACTIVE: 관리자 승인
- PENDING → REJECTED: 관리자 거절
- ACTIVE → DISABLED: 관리자 비활성화
- DISABLED → ACTIVE: 관리자 재활성화
- REJECTED는 최종 상태. 재활성화 API 없음

---

## 3. API별 접근 권한

### 인증 API (공개)

| API | 조건 |
|-----|------|
| `POST /api/auth/register` | 누구나 |
| `POST /api/auth/login` | 누구나 |
| `POST /api/auth/logout` | 로그인 상태 (status 무관) |
| `GET /api/auth/me` | 로그인 상태 (status 무관) |

> `/api/auth/logout`, `/api/auth/me`는 status에 상관없이 로그인 상태(유효한 토큰)이면 접근 가능하다. status 확인은 클라이언트 라우터 가드에서 처리한다.

### 관리자 API

| API | 조건 |
|-----|------|
| `GET /api/admin/users` | ADMIN + ACTIVE |
| `GET /api/admin/users/pending` | ADMIN + ACTIVE |
| `PATCH /api/admin/users/{id}/approve` | ADMIN + ACTIVE |
| `PATCH /api/admin/users/{id}/reject` | ADMIN + ACTIVE |
| `PATCH /api/admin/users/{id}/disable` | ADMIN + ACTIVE |
| `PATCH /api/admin/users/{id}/activate` | ADMIN + ACTIVE |
| `GET /api/admin/schedules` | ADMIN + ACTIVE |
| `GET /api/admin/users/{id}/schedules` | ADMIN + ACTIVE |

### 일정 API

| API | 조건 |
|-----|------|
| `GET /api/schedules` | USER + ACTIVE (본인 일정만 반환) |
| `POST /api/schedules` | USER + ACTIVE 또는 ADMIN + ACTIVE |
| `GET /api/schedules/{id}` | 작성자 본인(ACTIVE) 또는 ADMIN |
| `PUT /api/schedules/{id}` | 작성자 본인(ACTIVE) 또는 ADMIN |
| `DELETE /api/schedules/{id}` | 작성자 본인(ACTIVE) 또는 ADMIN |

---

## 4. 화면별 접근 권한

| 경로 | 조건 |
|------|------|
| `/login` | 누구나 (로그인 상태면 자동 분기) |
| `/register` | 누구나 (로그인 상태면 자동 분기) |
| `/pending` | PENDING, REJECTED, DISABLED 상태 사용자 |
| `/` | USER + ACTIVE |
| `/schedules/new` | USER + ACTIVE 또는 ADMIN + ACTIVE |
| `/schedules/:id/edit` | 작성자 본인(ACTIVE) 또는 ADMIN + ACTIVE |
| `/admin` | ADMIN + ACTIVE |
| `/admin/schedules` | ADMIN + ACTIVE |

---

## 5. 일정 조회 권한

| 주체 | 조회 범위 |
|------|-----------|
| USER + ACTIVE | 본인이 작성한 일정만 조회 가능 |
| ADMIN | 모든 사용자의 일정 조회 가능 |
| PENDING / REJECTED / DISABLED | 일정 API 접근 불가 (403 반환) |

**백엔드 강제 규칙:**
- `GET /api/schedules`: 토큰에서 추출한 userId로만 조회. 쿼리 파라미터로 다른 userId 지정 불가.
- `GET /api/schedules/{id}`: 일정의 userId와 요청자 userId가 다르면 403. ADMIN은 예외.

---

## 6. 일정 수정/삭제 권한

| 주체 | 수정/삭제 가능 범위 |
|------|---------------------|
| USER + ACTIVE | 본인이 작성한 일정만 가능 |
| ADMIN | 모든 사용자의 일정 수정/삭제 가능 |

**백엔드 강제 규칙:**
- `PUT /api/schedules/{id}`, `DELETE /api/schedules/{id}`: 일정의 userId와 요청자 userId가 다르면 403. ADMIN은 예외.
- 관리자가 다른 사용자의 일정을 수정/삭제해도 해당 사용자에게 별도 알림 없음.

---

## 7. 관리자 기능 권한

| 기능 | 권한 |
|------|------|
| 사용자 목록 조회 | ADMIN + ACTIVE |
| 사용자 승인 | ADMIN + ACTIVE |
| 사용자 거절 | ADMIN + ACTIVE |
| 사용자 비활성화 | ADMIN + ACTIVE |
| 사용자 재활성화 | ADMIN + ACTIVE |
| 전체 일정 조회 | ADMIN + ACTIVE |
| 타 사용자 일정 수정 | ADMIN + ACTIVE |
| 타 사용자 일정 삭제 | ADMIN + ACTIVE |
| 본인 일정 등록/조회/수정/삭제 | ADMIN + ACTIVE |
| 다른 ADMIN 계정 생성 | 불가 (MVP 범위 외) |

---

## 8. 예외 케이스

| 상황 | 처리 |
|------|------|
| 만료된 JWT 토큰으로 API 요청 | 401 UNAUTHORIZED 반환 |
| USER가 `/api/admin/*` 접근 | 403 FORBIDDEN 반환 |
| PENDING 사용자가 `/api/schedules` 접근 | 403 FORBIDDEN 반환 |
| REJECTED 사용자가 로그인 후 API 요청 | `/api/auth/me`, `/api/auth/logout` 제외 모두 403 |
| DISABLED 사용자가 로그인 시도 | 로그인은 성공. 토큰 발급. 단 일정 API는 403 |
| 존재하지 않는 일정 ID로 접근 | 404 NOT_FOUND 반환. 소유자 여부 노출 없음 |
| 소프트 딜리트된 일정 ID로 접근 | 404 NOT_FOUND 반환 (삭제 여부 노출 없음) |
| USER가 타인의 scheduleId로 직접 접근 | 403 FORBIDDEN. 타인 일정 존재 여부 노출 금지 |
| ADMIN이 자기 자신을 비활성화 | 허용하지 않음 (방어 로직 필요) |

> 민감한 에러 응답에 사용자 정보, 내부 구조, DB 오류 메시지를 포함하지 않는다.

---

## 9. 확인 필요 사항

없음. 모든 권한 정책이 확정되었음.
