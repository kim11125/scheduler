# 02. 시스템 아키텍처

## 1. 전체 시스템 구조

```
[사용자 브라우저 (모바일/PC)]
        |
        | HTTP (80)
        ↓
[Nginx (Reverse Proxy)]
  - /api/*  → Spring Boot (8080)
  - /*      → Vue 3 정적 파일 서빙
        |
        ├── [Spring Boot 3 (8080)]
        |       - REST API
        |       - Spring Security + JWT
        |       - Spring Data JPA
        |       - Flyway Migration
        |       ↓
        |   [PostgreSQL (5432)]
        |       - 볼륨 마운트: /var/lib/postgresql/data
        |
        └── [Vue 3 빌드 산출물]
                - Nginx가 정적 파일 직접 서빙
```

---

## 2. 컴포넌트별 역할

| 컴포넌트 | 역할 |
|----------|------|
| **Nginx** | 리버스 프록시. `/api/*` 요청은 Spring Boot로 전달. 나머지는 Vue 빌드 파일 서빙. SPA 히스토리 모드 지원 (`try_files`) |
| **Vue 3 + Vite** | 프론트엔드 SPA. 캘린더 UI, 일정 입력 폼, 관리자 화면. 빌드 후 Nginx가 서빙 |
| **Spring Boot 3** | REST API 서버. 인증, 일정 관리, 사용자 관리, 권한 검증 담당 |
| **PostgreSQL** | 데이터 영구 저장. Docker 볼륨으로 컨테이너 재시작 시에도 데이터 보존 |

---

## 3. Oracle Linux 서버 배포 구조

```
Oracle Linux 서버 (단일 서버)
├── Docker Engine
└── Docker Compose
    ├── nginx (container)
    │   - ports: "80:80"
    │   - volumes: ./frontend/dist:/usr/share/nginx/html
    │              ./nginx/nginx.conf:/etc/nginx/nginx.conf
    │
    ├── backend (container)
    │   - ports: expose 8080 (내부 네트워크만)
    │   - depends_on: db
    │   - env_file: .env
    │
    └── db (container)
        - ports: expose 5432 (내부 네트워크만)
        - volumes: postgres_data:/var/lib/postgresql/data
        - env_file: .env
```

- 외부에서 접근 가능한 포트: **80번만 노출**
- backend, db는 Docker 내부 네트워크에서만 통신
- 서버 IP로 직접 접근: `http://<서버IP>`

---

## 4. 요청 흐름

### 4-1. 정적 파일 요청 (프론트엔드)

```
브라우저 → Nginx(80) → /usr/share/nginx/html/index.html (또는 JS/CSS)
```

### 4-2. API 요청

```
브라우저 → Nginx(80) /api/* → Spring Boot(8080) → PostgreSQL(5432)
                                    ↓
                             JSON 응답 반환
                                    ↓
                          Nginx → 브라우저
```

### 4-3. SPA 라우팅

```
브라우저가 /schedules/new 직접 접근
→ Nginx try_files $uri /index.html
→ Vue Router가 클라이언트 사이드 라우팅 처리
```

---

## 5. 인증 흐름

```
1. POST /api/auth/login
   - username + password 전송
   - 서버: BCrypt 비밀번호 검증
   - 서버: JWT Access Token 생성 (만료: 24시간)
   - 응답: { accessToken, user: { id, username, name, role, status } }

2. 클라이언트: accessToken을 localStorage에 저장

3. 이후 모든 API 요청 헤더에 포함:
   Authorization: Bearer <accessToken>

4. 서버: JwtAuthenticationFilter가 토큰 검증
   - 유효하면 SecurityContext에 사용자 정보 세팅
   - 만료/유효하지 않으면 401 응답

5. POST /api/auth/logout
   - 클라이언트에서 localStorage의 accessToken 삭제
   - 서버 측 별도 처리 없음 (Stateless)
```

> Refresh Token은 사용하지 않는다. Access Token 만료 시 재로그인이 필요하다.

---

## 6. 권한 체크 흐름

```
요청 수신
   ↓
JwtAuthenticationFilter
   - Authorization 헤더 파싱
   - 토큰 유효성 검증
   - SecurityContext에 사용자 세팅
   ↓
Spring Security FilterChain
   - 인증 필요 경로: 토큰 없으면 401
   - 공개 경로: /api/auth/login, /api/auth/register
   ↓
Controller/Service
   - @PreAuthorize 또는 서비스 레이어에서 Role 검증
   - 일정 API: userId 일치 여부 추가 검증 (ADMIN은 예외)
   - status == ACTIVE 여부 검증
```

---

## 7. 디렉토리 구조

```
project-root/
├── docker-compose.yml
├── docker-compose.dev.yml
├── .env.example
├── .env                      # 실제 환경변수 (gitignore)
├── nginx/
│   └── nginx.conf
├── docs/
│   ├── 01-requirements.md
│   ├── 02-architecture.md
│   ├── 03-database-design.md
│   ├── 04-api-design.md
│   ├── 05-screen-flow.md
│   ├── 06-permission-policy.md
│   ├── 07-deployment.md
│   └── 08-schedule-domain.md
├── backend/                  # Spring Boot 프로젝트
│   ├── build.gradle
│   ├── settings.gradle
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/com/example/app/
│       │   │   ├── auth/
│       │   │   ├── user/
│       │   │   ├── schedule/
│       │   │   ├── admin/
│       │   │   ├── audit/
│       │   │   ├── config/
│       │   │   └── common/
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-dev.yml
│       │       ├── application-prod.yml
│       │       └── db/migration/
│       │           ├── V1__init_schema.sql
│       │           └── V2__init_admin.sql
│       └── test/
└── frontend/                 # Vue 3 프로젝트
    ├── package.json
    ├── vite.config.ts
    ├── tsconfig.json
    ├── Dockerfile
    └── src/
        ├── main.ts
        ├── App.vue
        ├── router/
        ├── stores/           # Pinia
        ├── api/
        ├── components/
        │   ├── common/
        │   ├── calendar/
        │   └── schedule/
        ├── views/
        │   ├── LoginView.vue
        │   ├── RegisterView.vue
        │   ├── PendingView.vue
        │   ├── HomeView.vue
        │   ├── ScheduleNewView.vue
        │   ├── ScheduleEditView.vue
        │   └── admin/
        │       ├── AdminDashboardView.vue
        │       └── AdminSchedulesView.vue
        └── types/
```

---

## 8. 개발/운영 환경 구분

| 항목 | 개발 환경 | 운영 환경 |
|------|-----------|-----------|
| 실행 방법 | `docker-compose -f docker-compose.dev.yml up` | `docker-compose up -d` |
| Spring 프로파일 | `dev` | `prod` |
| CORS 허용 출처 | `http://localhost:5173` | `http://<서버IP>` |
| DB | Docker PostgreSQL 또는 로컬 PostgreSQL | Docker PostgreSQL (볼륨 영구 보존) |
| 프론트 실행 | Vite dev server (5173) | Nginx 정적 서빙 |
| 로그 레벨 | DEBUG | INFO |
| JWT Secret | 개발용 임시값 | 환경변수로 강한 값 설정 |

---

## 9. 확인 필요 사항

없음. 모든 구조 결정 완료.
