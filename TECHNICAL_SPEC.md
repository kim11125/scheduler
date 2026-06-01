# 스케줄 관리 웹앱 - 전체 기술 명세서

> 작성일: 2026-06-01  
> 버전: v1.0  
> 브랜치: develop

---

## 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [시스템 아키텍처](#2-시스템-아키텍처)
3. [권한 및 사용자 상태 구조](#3-권한-및-사용자-상태-구조)
4. [데이터베이스 설계](#4-데이터베이스-설계)
5. [백엔드 구조 및 로직](#5-백엔드-구조-및-로직)
6. [API 명세](#6-api-명세)
7. [프론트엔드 구조 및 로직](#7-프론트엔드-구조-및-로직)
8. [인증 흐름](#8-인증-흐름)
9. [화면 흐름 및 라우팅](#9-화면-흐름-및-라우팅)
10. [배포 구조](#10-배포-구조)
11. [현재 구현 상태](#11-현재-구현-상태)
12. [미구현 / 개선 필요 항목](#12-미구현--개선-필요-항목)

---

## 1. 프로젝트 개요

### 목적
관리자 승인 기반의 **폐쇄형 개인 스케줄 관리 웹앱**.  
불특정 다수가 가입할 수 없으며, 관리자가 승인한 사용자만 서비스를 이용할 수 있다.

### 핵심 특징
- 회원가입 → 관리자 승인 → 서비스 이용 순서
- 사용자는 본인 일정만 관리
- 관리자(ADMIN/MANAGER)는 전체 회원 및 일정 관리
- 스포츠 경기 관람 일정에 특화된 카테고리 구조
- 3가지 UI 테마 (라이트 / 다크 / 오렌지)

### 기술 스택

| 구분 | 기술 | 버전 |
|------|------|------|
| Frontend | Vue 3 + TypeScript | Vue 3.5 |
| 상태관리 | Pinia | 2.x |
| 라우팅 | Vue Router | 4.x |
| HTTP 클라이언트 | Axios | 1.x |
| 빌드 도구 | Vite | 5.x |
| Backend | Spring Boot | 3.5.14 |
| 언어 | Java | 17 |
| ORM | Spring Data JPA + Hibernate | 6.x |
| 보안 | Spring Security + JWT (jjwt) | 0.12.6 |
| DB | PostgreSQL | 15 |
| 마이그레이션 | Flyway | 11.x |
| 컨테이너 | Docker + Docker Compose | 28.x |
| 웹서버 | Nginx | alpine |
| 서버 | Oracle Cloud Free Tier | Ubuntu 20.04 |

---

## 2. 시스템 아키텍처

### 전체 구조

```
브라우저
  │
  ▼
Nginx (Port 80)
  ├── /          → Vue 3 정적 파일 서빙 (dist/)
  └── /api/**    → Spring Boot 프록시 (Port 8080)
                      │
                      ▼
                 Spring Boot
                      │
                      ▼
                 PostgreSQL (Port 5432)
```

### Docker Compose 구성

```yaml
services:
  postgres:    # PostgreSQL 15 (내부 포트만)
  backend:     # Spring Boot (8080, postgres 헬스체크 후 기동)
  nginx:       # Nginx (80 → 프론트/백 라우팅)
```

### 요청 흐름

```
1. 브라우저 → GET http://server/
   └── Nginx → dist/index.html 반환 (SPA)

2. 브라우저 → POST http://server/api/auth/login
   └── Nginx → http://backend:8080/api/auth/login 프록시

3. 인증된 요청 → Authorization: Bearer <JWT>
   └── JwtFilter → SecurityContext에 userId 등록
   └── Controller → @AuthenticationPrincipal Long userId
```

---

## 3. 권한 및 사용자 상태 구조

### Role (역할)

| Role | 명칭 | 설명 |
|------|------|------|
| `ADMIN` | 최고 관리자 | 모든 권한. 역할 변경(임명/해제) 가능 |
| `MANAGER` | 일반 관리자 | 회원 관리 + 일정 관리. 역할 변경 불가 |
| `USER` | 일반 사용자 | 본인 일정만 관리 |

### UserStatus (상태)

| Status | 설명 | 전환 가능 상태 |
|--------|------|----------------|
| `PENDING` | 가입 승인 대기 | → ACTIVE, REJECTED |
| `ACTIVE` | 정상 이용 | → DISABLED |
| `REJECTED` | 가입 거절 | → ACTIVE (재승인) |
| `DISABLED` | 비활성화 | → ACTIVE (재활성화) |

### 권한 매트릭스

| 기능 | USER | MANAGER | ADMIN |
|------|------|---------|-------|
| 본인 일정 CRUD | ✅ | ✅ | ✅ |
| 타인 일정 추가/수정/삭제 | ❌ | ✅ | ✅ |
| 전체 일정 조회 | ❌ | ✅ | ✅ |
| 회원 승인/거절 | ❌ | ✅ | ✅ |
| 회원 비활성화/재활성화 | ❌ | ✅ | ✅ |
| 회원 비밀번호 변경 | 본인만 | ✅ | ✅ |
| 역할 변경 (임명) | ❌ | ❌ | ✅ |

---

## 4. 데이터베이스 설계

### ERD

```
users (1) ──────< schedules (N)
```

### users 테이블

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGSERIAL | PK | 자동 증가 |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 로그인 아이디 |
| password | VARCHAR(255) | NOT NULL | BCrypt 해시 |
| name | VARCHAR(50) | NOT NULL | 표시 이름 |
| role | VARCHAR(20) | NOT NULL | ADMIN / MANAGER / USER |
| status | VARCHAR(20) | NOT NULL | PENDING / ACTIVE / REJECTED / DISABLED |
| created_at | TIMESTAMP | NOT NULL | 등록일 |
| updated_at | TIMESTAMP | NOT NULL | 수정일 |

### schedules 테이블

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGSERIAL | PK | 자동 증가 |
| user_id | BIGINT | FK → users.id | 작성자 |
| title | VARCHAR(100) | NOT NULL | 일정 제목 |
| category | VARCHAR(30) | NOT NULL | 카테고리 (enum) |
| baseball_type | VARCHAR(10) | NULL | HOME / AWAY (야구 전용) |
| date | DATE | NOT NULL | 시작일 |
| end_date | DATE | NULL | 종료일 (선택) |
| memo | TEXT | NULL | 메모 |
| deleted_at | TIMESTAMP | NULL | 소프트 삭제 시각 |
| created_at | TIMESTAMP | NOT NULL | 작성일 |
| updated_at | TIMESTAMP | NOT NULL | 수정일 |

### 인덱스

```sql
CREATE INDEX idx_schedules_user_id ON schedules(user_id);
CREATE INDEX idx_schedules_date    ON schedules(date);
```

### Flyway 마이그레이션 이력

| 버전 | 파일명 | 내용 |
|------|--------|------|
| V1 | V1__create_users_table.sql | users 테이블 생성 |
| V2 | V2__create_schedules_table.sql | schedules 테이블 생성 |
| V3 | V3__insert_admin_user.sql | 초기 ADMIN 계정 INSERT |
| V4 | V4__add_end_date_to_schedules.sql | end_date 컬럼 추가 |
| V5 | V5__add_manager_role.sql | role 컬럼 VARCHAR 변환 |

### 소프트 삭제 정책
- 일정 삭제 시 `deleted_at = NOW()` 설정 (물리 삭제 X)
- 조회 쿼리는 항상 `WHERE deleted_at IS NULL` 조건 포함
- 향후 삭제 이력 복원 가능

---

## 5. 백엔드 구조 및 로직

### 디렉토리 구조

```
com.scheduler.backend
├── domain
│   ├── auth
│   │   ├── controller/AuthController.java
│   │   ├── dto/ (LoginRequest, LoginResponse, RegisterRequest)
│   │   └── service/AuthService.java
│   ├── schedule
│   │   ├── controller/ScheduleController.java
│   │   ├── dto/ (ScheduleRequest, ScheduleResponse)
│   │   ├── entity/ (Schedule, Category, BaseballType)
│   │   ├── repository/ScheduleRepository.java
│   │   └── service/ScheduleService.java
│   ├── user
│   │   ├── controller/UserController.java
│   │   ├── entity/ (User, Role, UserStatus)
│   │   └── repository/UserRepository.java
│   └── admin
│       ├── controller/AdminController.java
│       ├── dto/UserResponse.java
│       └── service/AdminService.java
└── global
    ├── config/ (SecurityConfig, JpaConfig)
    ├── exception/GlobalExceptionHandler.java
    └── jwt/ (JwtProvider, JwtFilter)
```

### 인증 처리 (JWT)

```
1. 로그인 요청
   POST /api/auth/login { username, password }
   └── AuthService.login()
       ├── UserRepository.findByUsername()
       ├── BCryptPasswordEncoder.matches()
       └── JwtProvider.generate(userId, role)
           └── 응답: { token, userId, name, role, status }

2. 이후 모든 요청
   Header: Authorization: Bearer <token>
   └── JwtFilter.doFilterInternal()
       ├── token 유효성 검증
       ├── userId 추출
       └── SecurityContextHolder에 UsernamePasswordAuthenticationToken 등록
           └── principal = userId (Long)
           └── authorities = [ROLE_ADMIN | ROLE_MANAGER | ROLE_USER]
```

### JWT 설정
- 알고리즘: HS512
- 만료: 24시간 (86400000ms)
- 시크릿: 환경변수 `JWT_SECRET` (64자 이상 권장)
- Claim: `sub` = userId, `role` = 역할명

### 비밀번호 정책
- BCrypt 10 라운드
- 저장 시 평문 비밀번호는 절대 저장하지 않음
- 변경 시 현재 비밀번호 확인 후 교체 (사용자 본인)
- 관리자는 현재 비밀번호 확인 없이 강제 변경 가능

### 카테고리 Enum

```java
enum Category {
    BASEBALL,          // 야구
    BASKETBALL,        // 농구
    SOCCER,            // 축구
    WOMENS_VOLLEYBALL, // 여자배구
    MENS_VOLLEYBALL,   // 남자배구
    ETC                // 기타
}

enum BaseballType {
    HOME,  // 홈경기
    AWAY   // 원정경기
}
// BaseballType은 category = BASEBALL 일 때만 유효
```

### 예외 처리

```
GlobalExceptionHandler
├── IllegalArgumentException → 400 Bad Request + { message }
├── MethodArgumentNotValidException → 400 Bad Request + { message }
└── Exception → 500 Internal Server Error + { message }
```

---

## 6. API 명세

### 인증 (공개)

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/api/auth/register` | 회원가입 신청 | ❌ |
| POST | `/api/auth/login` | 로그인 | ❌ |

**POST /api/auth/register**
```json
// Request
{ "username": "user1", "password": "pass1234", "name": "홍길동" }
// Response: 200 OK (body 없음)
// Error: 400 { "message": "이미 사용 중인 아이디입니다." }
```

**POST /api/auth/login**
```json
// Request
{ "username": "user1", "password": "pass1234" }
// Response
{ "token": "eyJ...", "userId": 2, "name": "홍길동", "role": "USER", "status": "ACTIVE" }
```

### 사용자 본인 (인증 필요)

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/users/me` | 내 정보 조회 |
| PUT | `/api/users/me/password` | 비밀번호 변경 |

**PUT /api/users/me/password**
```json
// Request
{ "currentPassword": "old1234", "newPassword": "new1234" }
// Response: 200 OK
// Error: 400 { "message": "현재 비밀번호가 올바르지 않습니다." }
```

### 일정 (인증 필요)

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/schedules` | 내 일정 전체 조회 |
| POST | `/api/schedules` | 일정 추가 |
| PUT | `/api/schedules/{id}` | 일정 수정 |
| DELETE | `/api/schedules/{id}` | 일정 삭제 (소프트) |

**POST /api/schedules**
```json
// Request
{
  "title": "두산 vs LG",
  "category": "BASEBALL",
  "baseballType": "HOME",
  "date": "2026-06-10",
  "endDate": "2026-06-10",
  "memo": "1루 응원석",
  "targetUserId": null  // 관리자가 다른 유저 대신 추가할 때 사용
}
// Response: ScheduleResponse
```

**ScheduleResponse**
```json
{
  "id": 1,
  "userId": 2,
  "userName": "홍길동",
  "title": "두산 vs LG",
  "category": "BASEBALL",
  "baseballType": "HOME",
  "date": "2026-06-10",
  "endDate": "2026-06-10",
  "memo": "1루 응원석",
  "createdAt": "2026-06-01T10:00:00",
  "updatedAt": "2026-06-01T10:00:00"
}
```

### 관리자 (ADMIN 또는 MANAGER)

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/admin/users` | 전체 사용자 조회 |
| GET | `/api/admin/users/pending` | 승인 대기 사용자 조회 |
| PUT | `/api/admin/users/{id}/approve` | 가입 승인 |
| PUT | `/api/admin/users/{id}/reject` | 가입 거절 |
| PUT | `/api/admin/users/{id}/disable` | 비활성화 |
| PUT | `/api/admin/users/{id}/activate` | 재활성화 |
| PUT | `/api/admin/users/{id}/password` | 비밀번호 강제 변경 |
| PUT | `/api/admin/users/{id}/role` | 역할 변경 **(ADMIN 전용)** |
| GET | `/api/admin/schedules` | 전체 일정 조회 |
| GET | `/api/admin/schedules/user/{userId}` | 특정 사용자 일정 조회 |
| DELETE | `/api/admin/schedules/{id}` | 일정 삭제 (소프트) |

---

## 7. 프론트엔드 구조 및 로직

### 디렉토리 구조

```
src/
├── api/
│   ├── client.ts        # axios 인스턴스 (JWT 자동 첨부, 401 인터셉터)
│   ├── auth.ts          # 로그인/회원가입 API
│   ├── schedule.ts      # 일정 CRUD API
│   ├── admin.ts         # 관리자 API
│   └── user.ts          # 사용자 본인 API
├── stores/
│   ├── auth.ts          # 로그인 상태, 사용자 정보
│   ├── schedule.ts      # 일정 목록, CRUD
│   ├── theme.ts         # 테마 설정
│   └── users.ts         # 관리자용 사용자 목록
├── views/
│   ├── LoginView.vue
│   ├── RegisterView.vue
│   ├── PendingView.vue
│   ├── HomeView.vue     # 캘린더 + 일정 관리
│   └── admin/
│       ├── AdminDashboardView.vue
│       └── AdminSchedulesView.vue
├── composables/
│   └── useCalendar.ts   # 달력 날짜 계산
├── styles/
│   └── themes.css       # CSS 변수 테마 정의
├── types/
│   └── index.ts         # TypeScript 타입 정의
└── router/
    └── index.ts         # 라우팅 + 권한 가드
```

### Pinia 스토어

#### auth.ts
```typescript
- user: User | null        // 로그인한 사용자 정보
- error: string | null     // 로그인 에러 메시지
- login(username, password) // JWT 저장 + user 설정
- register(username, password, name)
- logout()                 // localStorage 초기화
```

#### schedule.ts
```typescript
- schedules: Schedule[]    // 내 일정 목록
- fetchAll()               // GET /api/schedules
- add(data)                // POST /api/schedules
- update(id, data)         // PUT /api/schedules/{id}
- remove(id)               // DELETE /api/schedules/{id}
- getByDate(date)          // 날짜별 필터
- getByMonth(year, month)  // 월별 필터
- getDotsByDate(date)      // 캘린더 점 표시용 (최대 3개)
- getExtraCount(date)      // 초과 일정 수 (+N)
```

#### theme.ts
```typescript
- current: 'light' | 'dark' | 'orange'
- setTheme(key)    // HTML data-theme 속성 변경 + localStorage 저장
- cycle()          // 순환 전환
- init()           // localStorage에서 복원
```

### 테마 구현 방식
```css
/* HTML 요소에 data-theme 속성으로 테마 전환 */
[data-theme="light"]  { --color-primary: #1976D2; --color-background: #FFFFFF; ... }
[data-theme="dark"]   { --color-primary: #00CBA8; --color-background: #0C1929; ... }
[data-theme="orange"] { --color-primary: #F4511E; --color-background: #F5F5F5; ... }
```

### 캘린더 로직 (useCalendar.ts)
```typescript
// 해당 월의 모든 날짜 셀 생성 (앞뒤 빈 칸 포함)
// 반환: CalendarDay[]
interface CalendarDay {
  date: string          // YYYY-MM-DD
  day: number           // 일
  isCurrentMonth: boolean
  isToday: boolean
  dayOfWeek: number     // 0=일, 6=토
}
```

### 날짜 클릭 UX 로직

```
날짜 클릭
  ├── 일정 없는 날 → 바로 추가 모달 오픈
  └── 일정 있는 날
        ├── 다른 날짜 → 해당 날짜 선택 (목록 표시)
        └── 같은 날짜 재클릭 → 추가 모달 오픈
```

---

## 8. 인증 흐름

### 로그인 ~ API 요청 전체 흐름

```
1. 사용자 로그인
   Frontend: POST /api/auth/login
   Backend:  JWT 생성 (payload: userId, role, exp)
   Frontend: localStorage에 token, user 저장

2. 페이지 이동 (라우터 가드)
   router.beforeEach()
   ├── 미로그인 → /login
   ├── ACTIVE + ADMIN/MANAGER → /admin
   ├── ACTIVE + USER → /
   └── PENDING/REJECTED/DISABLED → /pending

3. API 요청
   axios interceptor → headers.Authorization = "Bearer " + token

4. 백엔드 검증
   JwtFilter → token 파싱 → userId → SecurityContext 등록

5. 401 응답 시
   axios interceptor → localStorage 초기화 → /login 이동
```

### 토큰 저장 위치
- `localStorage['token']`: JWT 토큰
- `localStorage['auth_user']`: 사용자 정보 (JSON)
- 새로고침 후 자동 복원

---

## 9. 화면 흐름 및 라우팅

### 라우트 목록

| Path | Name | 컴포넌트 | 접근 권한 |
|------|------|---------|----------|
| `/login` | login | LoginView | 비로그인 |
| `/register` | register | RegisterView | 비로그인 |
| `/pending` | pending | PendingView | PENDING/REJECTED/DISABLED |
| `/` | home | HomeView | ACTIVE + USER |
| `/my-schedules` | my-schedules | HomeView | ACTIVE + 모든 역할 |
| `/admin` | admin | AdminDashboardView | ACTIVE + ADMIN/MANAGER |
| `/admin/schedules` | admin-schedules | AdminSchedulesView | ACTIVE + ADMIN/MANAGER |

### 화면별 주요 기능

#### LoginView (`/login`)
- 아이디/비밀번호 입력
- 로그인 성공 시 역할/상태에 따라 자동 분기

#### RegisterView (`/register`)
- 아이디(영문+숫자, 4~20자), 비밀번호(영문+숫자, 8자↑), 이름 입력
- 가입 완료 시 "승인 대기" 안내

#### PendingView (`/pending`)
- PENDING: 승인 대기 안내
- REJECTED: 거절 안내
- DISABLED: 비활성화 안내

#### HomeView (`/`, `/my-schedules`)
- 월별 캘린더 뷰
- 날짜 클릭 → 일정 목록 / 추가 모달
- FAB 버튼 → 추가 모달
- 관리자 접근 시 좌상단 "‹ 관리" 버튼 노출
- 우상단 프로필 버튼 → 정보 모달 (비밀번호 변경, 로그아웃)

#### AdminDashboardView (`/admin`)
- 통계 카드 (승인대기 / 활성사용자 / 전체일정) 클릭 시 해당 섹션 이동
- 승인 대기 목록 (승인/거절)
- 전체 사용자 목록 (상태별 필터)
- 사용자 카드 클릭 → 상세 모달 (상태변경, 역할변경, 비밀번호 변경)
- 헤더 "📅 내 일정" 버튼 → 본인 일정 관리

#### AdminSchedulesView (`/admin/schedules`)
- 사용자 필터 + 카테고리 필터 + 월 이동
- 뷰 토글: 목록(≡) / 캘린더(▦)
- 일정 카드 클릭 → 수정/삭제 모달

---

## 10. 배포 구조

### Docker Compose

```yaml
services:
  postgres:
    image: postgres:15-alpine
    restart: always
    environment:
      POSTGRES_DB: scheduler
      POSTGRES_USER: scheduler
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: pg_isready -U scheduler

  backend:
    build: ./backend/backend
    restart: always
    environment:
      DB_HOST: postgres
      DB_NAME: scheduler
      DB_USER: scheduler
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
    depends_on:
      postgres:
        condition: service_healthy

  nginx:
    image: nginx:alpine
    restart: always
    ports:
      - "80:80"
    volumes:
      - ./frontend/dist:/usr/share/nginx/html
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
    depends_on:
      - backend
```

### Nginx 설정

```nginx
events {}
http {
  include /etc/nginx/mime.types;
  resolver 127.0.0.11 valid=10s;

  server {
    listen 80;

    location / {
      root /usr/share/nginx/html;
      try_files $uri $uri/ /index.html;  # SPA 라우팅
    }

    location /api/ {
      set $backend http://backend:8080;
      proxy_pass $backend;
      proxy_set_header Host $host;
      proxy_set_header X-Real-IP $remote_addr;
    }
  }
}
```

### 배포 절차

```bash
# 1. 서버에서 소스 가져오기
git clone https://github.com/kim11125/scheduler.git
cd scheduler
git checkout develop

# 2. 프론트엔드 빌드
cd frontend && npm install && npm run build && cd ..

# 3. 백엔드 빌드
cd backend/backend && ./gradlew bootJar -x test && cd ../..

# 4. 환경변수 설정
# docker-compose.yml의 환경변수 직접 설정 또는 .env 파일 생성

# 5. 실행
docker compose up --build -d

# 6. 확인
docker compose ps
docker logs scheduler-backend
```

### OCI 서버 정보

| 항목 | 값 |
|------|-----|
| 서버 IP | 168.107.15.194 |
| Shape | VM.Standard.E2.1.Micro (1 OCPU, 1GB RAM) |
| OS | Ubuntu 20.04 LTS |
| Swap | 2GB |
| Docker | 28.1.1 |
| Docker Compose | v2.35.1 |

> **현재 이슈**: OCI Security List에서 80 포트가 막혀 있어 외부 접속 불가.  
> MFA(핸드폰 분실) 문제로 OCI 콘솔 접근 불가 상태. 해결 후 Security List 인바운드 규칙 추가 필요.

---

## 11. 현재 구현 상태

### 완료

| 항목 | 상태 |
|------|------|
| 설계 문서 8종 | ✅ |
| Vue 3 프론트엔드 전체 화면 | ✅ |
| Spring Boot 백엔드 전체 API | ✅ |
| PostgreSQL + Flyway 마이그레이션 | ✅ |
| JWT 인증 | ✅ |
| Docker Compose 구성 | ✅ |
| Nginx 설정 | ✅ |
| 로컬 Docker 환경 동작 확인 | ✅ |
| OCI 서버 기본 세팅 | ✅ |
| GitHub 저장소 (develop/prod) | ✅ |
| 3가지 테마 | ✅ |
| 권한 2단계 (ADMIN/MANAGER) | ✅ |
| 일정 종료일자 | ✅ |
| 관리자 본인/타인 일정 추가 | ✅ |
| 비밀번호 변경 (사용자/관리자) | ✅ |
| 역할 변경 (ADMIN 전용) | ✅ |
| 관리자 일정 조회 (목록/캘린더) | ✅ |

---

## 12. 미구현 / 개선 필요 항목

### 기능 관련

| 항목 | 우선순위 | 설명 |
|------|---------|------|
| HTTPS / SSL 인증서 | 높음 | Let's Encrypt or 자체서명 인증서 적용 |
| OCI 포트 오픈 | 높음 | Security List 80/443 인바운드 규칙 추가 |
| Refresh Token | 중간 | 현재 Access Token만 사용, 24시간 만료 후 재로그인 필요 |
| 알림/공지 기능 | 중간 | 관리자 → 전체 사용자 공지 발송 |
| 일정 반복 설정 | 중간 | 매주/매월 반복 일정 |
| 일정 공유 | 낮음 | 특정 사용자에게 일정 공유 |
| 일정 댓글/메모 강화 | 낮음 | 이미지 첨부, 링크 |
| 통계/대시보드 강화 | 낮음 | 월별 관람 횟수, 카테고리별 통계 |
| 다국어 지원 | 낮음 | i18n |
| PWA 지원 | 낮음 | 모바일 앱처럼 홈 화면 추가 가능 |

### 기술/보안 관련

| 항목 | 우선순위 | 설명 |
|------|---------|------|
| Rate Limiting | 높음 | 로그인 시도 횟수 제한 (Brute Force 방어) |
| 입력값 검증 강화 | 높음 | XSS 방지, SQL Injection 방지 (현재 JPA 사용으로 SQL Injection은 낮음) |
| 로그 시스템 | 중간 | 접근 로그, 에러 로그 수집 (ELK or 파일) |
| 백업 정책 | 중간 | PostgreSQL 자동 백업 스크립트 |
| 환경변수 관리 | 중간 | .env 파일 또는 Docker secrets 사용 |
| 테스트 코드 | 중간 | 단위 테스트, 통합 테스트 (현재 미작성) |
| CI/CD | 낮음 | GitHub Actions → 자동 빌드/배포 |
| 모니터링 | 낮음 | 서버 리소스, API 응답시간 모니터링 |
| Ubuntu EOL 대응 | 낮음 | Ubuntu 20.04 → 22.04 업그레이드 |

### UX/UI 관련

| 항목 | 우선순위 | 설명 |
|------|---------|------|
| 로딩 상태 표시 | 중간 | API 호출 중 스피너/스켈레톤 UI |
| 에러 메시지 개선 | 중간 | 네트워크 오류, 서버 오류 안내 |
| 모바일 최적화 검증 | 중간 | 다양한 기기 해상도 테스트 |
| 날짜 범위 일정 캘린더 표시 | 낮음 | 종료일이 있는 일정을 캘린더에서 바 형태로 표시 |
| 일정 드래그 이동 | 낮음 | 캘린더에서 드래그로 날짜 변경 |
| 다크모드 시스템 연동 | 낮음 | OS 다크모드 자동 감지 |

---

## 부록: 환경변수 목록

| 변수명 | 설명 | 예시 |
|--------|------|------|
| `DB_HOST` | PostgreSQL 호스트 | `postgres` (Docker 내부) |
| `DB_NAME` | DB 이름 | `scheduler` |
| `DB_USER` | DB 사용자 | `scheduler` |
| `DB_PASSWORD` | DB 비밀번호 | 강한 비밀번호 사용 |
| `JWT_SECRET` | JWT 서명 키 | 64자 이상 랜덤 문자열 |
| `JWT_EXPIRATION` | 토큰 만료(ms) | `86400000` (24시간) |
| `VITE_API_BASE_URL` | 프론트 API 베이스 URL | 빈 값 (상대경로 사용) |

---

*이 문서는 프로젝트의 현재 상태를 기반으로 작성됐습니다. 기능 추가/변경 시 함께 업데이트가 필요합니다.*
