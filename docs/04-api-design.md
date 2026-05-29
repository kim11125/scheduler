# 04. API 설계

## 공통 규칙

- Base URL: `/api`
- 요청/응답 형식: `application/json`
- 인증이 필요한 API: `Authorization: Bearer <accessToken>` 헤더 필수
- 날짜 형식: `YYYY-MM-DD` (예: `2026-06-01`)
- 일시 형식: ISO 8601 (예: `2026-06-01T12:00:00`)

### 공통 에러 응답 형식

```json
{
  "code": "ERROR_CODE",
  "message": "사람이 읽을 수 있는 에러 메시지"
}
```

### 공통 에러 코드

| HTTP | code | 설명 |
|------|------|------|
| 400 | `VALIDATION_ERROR` | 입력값 유효성 오류 |
| 401 | `UNAUTHORIZED` | 토큰 없음 또는 만료 |
| 403 | `FORBIDDEN` | 권한 없음 |
| 404 | `NOT_FOUND` | 리소스 없음 |
| 409 | `CONFLICT` | 중복 (예: 아이디 중복) |
| 500 | `INTERNAL_ERROR` | 서버 오류 |

---

## 1. 인증 API

### POST /api/auth/register

회원가입 신청. 누구나 접근 가능.

**Request Body:**
```json
{
  "username": "hong123",
  "password": "pass1234",
  "name": "홍길동"
}
```

| 필드 | 타입 | 필수 | 규칙 |
|------|------|------|------|
| `username` | string | O | 영문 소문자로 시작, 영문 소문자+숫자, 4~20자 |
| `password` | string | O | 영문+숫자 조합, 8~50자 |
| `name` | string | O | 1~50자 |

**Response (201):**
```json
{
  "message": "회원가입 신청이 완료되었습니다. 관리자 승인을 기다려주세요."
}
```

**Error:**
- `400 VALIDATION_ERROR`: 입력값 규칙 위반
- `409 CONFLICT`: username 중복

---

### POST /api/auth/login

로그인. 누구나 접근 가능.

**Request Body:**
```json
{
  "username": "hong123",
  "password": "pass1234"
}
```

**Response (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "username": "hong123",
    "name": "홍길동",
    "role": "USER",
    "status": "ACTIVE"
  }
}
```

**Error:**
- `400 VALIDATION_ERROR`: 입력값 누락
- `401 UNAUTHORIZED`: 아이디 또는 비밀번호 불일치 (구체적인 원인은 노출하지 않음)

> 로그인 성공 시 사용자 상태(status)와 무관하게 토큰을 발급한다. 클라이언트에서 status를 확인하여 화면을 분기한다.

---

### POST /api/auth/logout

로그아웃. 인증 필요.

서버 측 처리는 없다. 클라이언트에서 토큰을 삭제하는 것으로 로그아웃 처리된다.
서버는 `last_login_at` 갱신 없이 200을 반환한다.

**Response (200):**
```json
{
  "message": "로그아웃되었습니다."
}
```

---

### GET /api/auth/me

현재 로그인 사용자 정보 조회. 인증 필요.

**Response (200):**
```json
{
  "id": 1,
  "username": "hong123",
  "name": "홍길동",
  "role": "USER",
  "status": "ACTIVE",
  "lastLoginAt": "2026-06-01T09:00:00",
  "createdAt": "2026-05-01T10:00:00"
}
```

---

## 2. 관리자 - 사용자 관리 API

> 모든 `/api/admin/*` API는 `ADMIN` role + `ACTIVE` status 필수.

### GET /api/admin/users

전체 사용자 목록 조회.

**Query Parameters:**

| 파라미터 | 필수 | 설명 |
|----------|------|------|
| `status` | X | 필터: `PENDING`, `ACTIVE`, `REJECTED`, `DISABLED` |
| `page` | X | 페이지 번호 (0부터 시작, 기본값 0) |
| `size` | X | 페이지 크기 (기본값 20) |

**Response (200):**
```json
{
  "content": [
    {
      "id": 2,
      "username": "hong123",
      "name": "홍길동",
      "role": "USER",
      "status": "PENDING",
      "createdAt": "2026-05-01T10:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "page": 0,
  "size": 20
}
```

---

### GET /api/admin/users/pending

승인 대기 중인 사용자 목록 조회. (status = PENDING)

**Response (200):**
```json
[
  {
    "id": 2,
    "username": "hong123",
    "name": "홍길동",
    "createdAt": "2026-05-01T10:00:00"
  }
]
```

---

### PATCH /api/admin/users/{userId}/approve

사용자 승인. status를 `ACTIVE`로 변경.

**Path Parameter:** `userId` (Long)

**Response (200):**
```json
{
  "id": 2,
  "username": "hong123",
  "status": "ACTIVE"
}
```

**Error:**
- `404 NOT_FOUND`: 사용자 없음
- `400 VALIDATION_ERROR`: 이미 ACTIVE 상태

---

### PATCH /api/admin/users/{userId}/reject

사용자 거절. status를 `REJECTED`로 변경.

**Response (200):**
```json
{
  "id": 2,
  "username": "hong123",
  "status": "REJECTED"
}
```

---

### PATCH /api/admin/users/{userId}/disable

사용자 비활성화. status를 `DISABLED`로 변경.

**Response (200):**
```json
{
  "id": 2,
  "username": "hong123",
  "status": "DISABLED"
}
```

---

### PATCH /api/admin/users/{userId}/activate

사용자 재활성화. status를 `ACTIVE`로 변경.

**Response (200):**
```json
{
  "id": 2,
  "username": "hong123",
  "status": "ACTIVE"
}
```

---

## 3. 관리자 - 일정 조회 API

### GET /api/admin/schedules

전체 사용자의 일정 조회.

**Query Parameters:**

| 파라미터 | 필수 | 설명 |
|----------|------|------|
| `userId` | X | 특정 사용자 필터 |
| `date` | X | 특정 날짜 필터 (`YYYY-MM-DD`) |
| `category` | X | 카테고리 필터 |
| `page` | X | 기본값 0 |
| `size` | X | 기본값 20 |

**Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "userId": 2,
      "userName": "홍길동",
      "title": "두산 vs LG",
      "category": "BASEBALL",
      "baseballType": "HOME",
      "date": "2026-06-01",
      "memo": "1루 응원석",
      "createdAt": "2026-05-20T10:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "page": 0,
  "size": 20
}
```

---

### GET /api/admin/users/{userId}/schedules

특정 사용자의 일정 조회.

**Query Parameters:**

| 파라미터 | 필수 | 설명 |
|----------|------|------|
| `date` | X | 날짜 필터 |
| `category` | X | 카테고리 필터 |

**Response (200):** GET /api/admin/schedules와 동일 구조

---

## 4. 일반 사용자 - 일정 API

> `ACTIVE` 상태의 사용자만 접근 가능. `PENDING`, `REJECTED`, `DISABLED` 사용자는 403 반환.

### GET /api/schedules

본인 일정 목록 조회. (soft delete된 일정 제외)

**Query Parameters:**

| 파라미터 | 필수 | 설명 |
|----------|------|------|
| `date` | X | 날짜 필터 (`YYYY-MM-DD`) |
| `month` | X | 월 필터 (`YYYY-MM`). 해당 월 전체 조회 |
| `category` | X | 카테고리 필터 |

**Response (200):**
```json
[
  {
    "id": 1,
    "title": "두산 vs LG",
    "category": "BASEBALL",
    "baseballType": "HOME",
    "date": "2026-06-01",
    "memo": "1루 응원석",
    "createdAt": "2026-05-20T10:00:00",
    "updatedAt": "2026-05-20T10:00:00"
  }
]
```

정렬: `createdAt DESC` (등록일 내림차순)

---

### POST /api/schedules

일정 등록. `ACTIVE` 사용자 및 `ADMIN` 가능.

**Request Body:**
```json
{
  "title": "두산 vs LG",
  "category": "BASEBALL",
  "baseballType": "HOME",
  "date": "2026-06-01",
  "memo": "1루 응원석"
}
```

| 필드 | 타입 | 필수 | 규칙 |
|------|------|------|------|
| `title` | string | O | 1~100자 |
| `category` | string | O | enum 값 중 하나 |
| `baseballType` | string | 조건부 필수 | `category=BASEBALL`이면 필수. 나머지는 null |
| `date` | string | O | `YYYY-MM-DD` 형식 |
| `memo` | string | X | 최대 2000자. 생략 가능 |

**Response (201):**
```json
{
  "id": 1,
  "title": "두산 vs LG",
  "category": "BASEBALL",
  "baseballType": "HOME",
  "date": "2026-06-01",
  "memo": "1루 응원석",
  "createdAt": "2026-05-20T10:00:00",
  "updatedAt": "2026-05-20T10:00:00"
}
```

**Error:**
- `400 VALIDATION_ERROR`: 입력값 오류, baseballType 누락(BASEBALL인 경우)

---

### GET /api/schedules/{scheduleId}

일정 상세 조회. 작성자 본인 또는 ADMIN만 가능.

**Response (200):** POST 응답과 동일 구조

**Error:**
- `403 FORBIDDEN`: 본인 일정 아님
- `404 NOT_FOUND`: 일정 없음 또는 삭제됨

---

### PUT /api/schedules/{scheduleId}

일정 수정 (전체 교체). 작성자 본인 또는 ADMIN만 가능.

**Request Body:** POST와 동일

**Response (200):** POST 응답과 동일 구조

**Error:**
- `403 FORBIDDEN`: 본인 일정 아님
- `404 NOT_FOUND`: 일정 없음 또는 삭제됨
- `400 VALIDATION_ERROR`: 입력값 오류

---

### DELETE /api/schedules/{scheduleId}

일정 삭제 (soft delete). 작성자 본인 또는 ADMIN만 가능.

**Response (204):** 응답 바디 없음

**Error:**
- `403 FORBIDDEN`: 본인 일정 아님
- `404 NOT_FOUND`: 일정 없음 또는 이미 삭제됨

---

## 5. API 권한 요약표

| Method | URL | 인증 | 권한 조건 |
|--------|-----|------|-----------|
| POST | `/api/auth/register` | 불필요 | - |
| POST | `/api/auth/login` | 불필요 | - |
| POST | `/api/auth/logout` | 필요 | 로그인 상태 |
| GET | `/api/auth/me` | 필요 | 로그인 상태 |
| GET | `/api/admin/users` | 필요 | ADMIN + ACTIVE |
| GET | `/api/admin/users/pending` | 필요 | ADMIN + ACTIVE |
| PATCH | `/api/admin/users/{userId}/approve` | 필요 | ADMIN + ACTIVE |
| PATCH | `/api/admin/users/{userId}/reject` | 필요 | ADMIN + ACTIVE |
| PATCH | `/api/admin/users/{userId}/disable` | 필요 | ADMIN + ACTIVE |
| PATCH | `/api/admin/users/{userId}/activate` | 필요 | ADMIN + ACTIVE |
| GET | `/api/admin/schedules` | 필요 | ADMIN + ACTIVE |
| GET | `/api/admin/users/{userId}/schedules` | 필요 | ADMIN + ACTIVE |
| GET | `/api/schedules` | 필요 | ACTIVE (본인만) |
| POST | `/api/schedules` | 필요 | ACTIVE 또는 ADMIN |
| GET | `/api/schedules/{id}` | 필요 | 작성자 본인 또는 ADMIN |
| PUT | `/api/schedules/{id}` | 필요 | 작성자 본인 또는 ADMIN |
| DELETE | `/api/schedules/{id}` | 필요 | 작성자 본인 또는 ADMIN |

---

## 6. 확인 필요 사항

없음. 모든 API 항목이 확정되었음.
