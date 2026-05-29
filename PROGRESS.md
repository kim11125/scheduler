# Scheduler 프로젝트 진행 현황

## 프로젝트 개요
Oracle Linux 서버 한 대에 배포하는 관리자 승인 기반 폐쇄형 개인 스케줄 관리 웹앱

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| Frontend | Vue 3 + TypeScript + Vite + Pinia + Vue Router 4 |
| Backend | Spring Boot 3 + Java 17 |
| DB | PostgreSQL |
| 인증 | JWT (Access Token, 24h) |
| 배포 | Docker Compose + Nginx |
| 서버 | Oracle Cloud Free Tier (Ubuntu 20.04) |

---

## 완료된 작업

### 설계 문서 (`docs/`)
- `01-requirements.md` — 기능 요구사항
- `02-architecture.md` — 시스템 아키텍처
- `03-database-design.md` — DB 스키마
- `04-api-design.md` — REST API 설계
- `05-screen-flow.md` — 화면 흐름
- `06-permission-policy.md` — 권한 정책
- `07-deployment.md` — 배포 전략
- `08-schedule-domain.md` — 스케줄 도메인 설계

### Frontend (`frontend/`)
- Vue 3 + TypeScript 프로젝트 구성 완료
- 3가지 테마 (라이트/다크/오렌지) CSS 변수 기반 구현
- 주요 화면 구현 완료:
  - `/login` — 로그인
  - `/register` — 회원가입 신청
  - `/pending` — 승인 대기
  - `/` — 홈 (캘린더 + 일정 관리)
  - `/admin` — 관리자 대시보드 (회원 승인/거절/관리)
  - `/admin/schedules` — 전체 일정 조회
- 현재는 Mock 데이터로 동작 (백엔드 연동 전)

### GitHub
- 저장소: https://github.com/kim11125/scheduler
- `develop` 브랜치: 전체 소스 push 완료
- `prod` 브랜치: 생성 완료

### OCI 서버
- 인스턴스: `scheduler-server`
- IP: `168.107.15.194`
- Shape: VM.Standard.E2.1.Micro (1 OCPU, 1GB RAM)
- OS: Ubuntu 20.04
- Swap 2GB 설정 완료
- 방화벽 포트 오픈 완료 (80, 443, 8080)

---

## 서버 세팅 완료 ✅

| 항목 | 결과 |
|------|------|
| OS 업데이트 | 완료 |
| Swap 2GB | 완료 |
| 방화벽 포트 (80, 443, 8080) | 완료 |
| Docker 28.1.1 | 완료 |
| Docker Compose v2.35.1 | 완료 |
| PostgreSQL 동작 확인 | 완료 |

---

## 앞으로 해야 할 작업

### 1. Backend 구현 (Spring Boot) — 예상 4~6시간
```
[ ] 프로젝트 생성 (Spring Initializr)
      - spring-boot-starter-web
      - spring-boot-starter-data-jpa
      - spring-boot-starter-security
      - jjwt (JWT)
      - postgresql driver
      - flyway
      - lombok
[ ] Flyway 마이그레이션 작성
      - V1: users 테이블
      - V2: schedules 테이블
      - V3: 초기 ADMIN 계정 INSERT
[ ] 주요 API 구현
      - POST /api/auth/login
      - POST /api/auth/register
      - GET/POST/PUT/DELETE /api/schedules
      - GET /api/admin/users
      - PUT /api/admin/users/{id}/approve|reject|disable
      - GET /api/admin/schedules
[ ] JWT 필터 구현
[ ] Spring Boot 메모리 튜닝 (-Xms128m -Xmx256m)
```

### 3. Frontend 백엔드 연동
```
[ ] axios 설치 및 API client 구성
[ ] Mock 데이터 → 실제 API 호출로 교체
[ ] JWT 토큰 저장/갱신 처리
[ ] 환경변수 (.env) 설정
```

### 4. Docker Compose 구성
```yaml
# 구성 예정
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: scheduler
      POSTGRES_USER: scheduler
      POSTGRES_PASSWORD: [비밀번호]
    volumes:
      - postgres_data:/var/lib/postgresql/data

  backend:
    build: ./backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/scheduler
      JWT_SECRET: [시크릿키]
    depends_on:
      - postgres

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./frontend/dist:/usr/share/nginx/html
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
    depends_on:
      - backend
```

### 5. Nginx 설정
```nginx
# 구성 예정
# / → Vue 정적 파일 서빙
# /api → Spring Boot 8080 프록시
```

### 6. 배포
```
[ ] 서버에 git clone
[ ] frontend npm build
[ ] docker compose up -d
[ ] 방화벽 최종 확인
[ ] 브라우저에서 168.107.15.194 접속 테스트
```

---

## 서버 접속 정보
| 항목 | 값 |
|------|-----|
| IP | 168.107.15.194 |
| 포트 | 22 |
| 유저 | ubuntu |
| 인증 | 비밀번호 또는 SSH Key |

---

## 디렉토리 구조 (최종 목표)
```
scheduler/
├── docs/                  # 설계 문서
├── frontend/              # Vue 3 소스
│   └── dist/              # 빌드 결과물
├── backend/               # Spring Boot 소스
├── nginx/
│   └── nginx.conf
├── docker-compose.yml
└── PROGRESS.md
```
