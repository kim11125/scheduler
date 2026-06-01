# 개인 스케줄 관리 웹앱

관리자 승인 기반의 폐쇄형 개인 스케줄 관리 웹앱이다.
회원가입 신청 후 관리자가 승인해야 서비스를 이용할 수 있다.
각 사용자는 본인의 일정만 관리하며, 관리자는 전체 일정을 조회하고 관리한다.

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| Frontend | Vue 3 + TypeScript + Vite + Pinia + Vue Router 4 |
| Backend | Java 17 + Spring Boot 3.5 |
| Database | PostgreSQL 15 |
| Auth | Spring Security + JWT (Access Token, 24시간 만료) |
| Password | BCrypt |
| ORM | Spring Data JPA + Hibernate |
| Migration | Flyway |
| Infra | Oracle Cloud (Ubuntu 20.04) + Docker Compose + Nginx |
| Build | Gradle |
| API Style | REST API |

---

## 권한 구조

| Role | 명칭 | 권한 |
|------|------|------|
| `ADMIN` | 최고 관리자 | 모든 권한 + 관리자 임명/해제 |
| `MANAGER` | 일반 관리자 | 회원 관리 + 일정 관리 (임명 불가) |
| `USER` | 일반 사용자 | 본인 일정만 |

| Status | 설명 |
|--------|------|
| `PENDING` | 가입 승인 대기 |
| `ACTIVE` | 정상 이용 가능 |
| `REJECTED` | 가입 거절 |
| `DISABLED` | 비활성화 |

---

## 주요 기능

### 일반 사용자
- 회원가입 신청 → 관리자 승인 후 이용 가능
- 월별 캘린더 뷰로 일정 조회
- 일정 추가/수정/삭제 (시작일·종료일, 카테고리, 메모)
- 카테고리: 야구(홈/원정), 농구, 축구, 여자배구, 남자배구, 기타
- 3가지 테마 (라이트 / 다크 / 오렌지)
- 프로필 모달에서 비밀번호 변경

### 관리자
- 회원가입 승인 / 거절 / 비활성화 / 재활성화
- 사용자 역할 변경 (ADMIN 전용)
- 사용자 비밀번호 변경
- 전체 일정 조회 (목록 뷰 / 캘린더 뷰)
- 사용자별 일정 필터링
- 관리자 본인 일정 + 다른 사용자 대신 일정 추가 가능

---

## 프로젝트 구조

```
scheduler/
├── docs/                        # 설계 문서
├── frontend/                    # Vue 3 프론트엔드
│   ├── src/
│   │   ├── api/                 # axios API 클라이언트
│   │   ├── stores/              # Pinia 상태 관리
│   │   ├── views/               # 페이지 컴포넌트
│   │   │   └── admin/           # 관리자 화면
│   │   ├── router/              # Vue Router
│   │   ├── composables/         # useCalendar 등
│   │   ├── styles/              # 테마 CSS 변수
│   │   └── types/               # TypeScript 타입 정의
│   └── dist/                    # 빌드 결과물
├── backend/backend/             # Spring Boot 백엔드
│   └── src/main/
│       ├── java/com/scheduler/backend/
│       │   ├── domain/          # auth, schedule, user, admin
│       │   └── global/          # jwt, config, exception
│       └── resources/
│           └── db/migration/    # Flyway SQL (V1~V5)
├── nginx/
│   └── nginx.conf               # 프론트 서빙 + API 프록시
├── docker-compose.yml           # postgres + backend + nginx
└── PROGRESS.md                  # 진행 현황
```

---

## 로컬 실행 (Docker)

### 사전 요구사항
- Docker Desktop
- Java 17 (백엔드 빌드용)
- Node.js 18+ (프론트엔드 빌드용)

### 실행 방법

```bash
# 1. 프론트엔드 빌드
cd frontend
npm install
npm run build

# 2. 백엔드 빌드
cd ../backend/backend
./gradlew bootJar -x test

# 3. 전체 스택 실행
cd ../..
docker compose up --build -d

# 4. 브라우저 접속
http://localhost
```

### 기본 계정
- 초기 관리자 계정은 `V3__insert_admin_user.sql` 마이그레이션으로 생성됩니다.
- 배포 후 반드시 비밀번호를 변경하세요.
- 일반 사용자는 회원가입 후 관리자 승인이 필요합니다.

---

## 서버 배포 (Oracle Cloud)

| 항목 | 값 |
|------|-----|
| 서버 | Oracle Cloud Free Tier (E2.1.Micro) |
| OS | Ubuntu 20.04 |
| IP | 168.107.15.194 |
| 접속 | SSH (ubuntu) |

### 서버 환경
- Docker 28.1.1 + Docker Compose v2.35.1
- Swap 2GB 설정 완료
- 방화벽 포트: 80, 443, 8080

> OCI Security List에서 포트를 추가로 열어야 외부 접속 가능 (현재 MFA 문제로 콘솔 접근 불가)

---

## 설계 문서 목록

| 문서 | 내용 |
|------|------|
| [01. 요구사항 정의](docs/01-requirements.md) | 서비스 목적, 사용자 유형, 핵심 기능, MVP 범위 |
| [02. 시스템 아키텍처](docs/02-architecture.md) | 전체 구조, 배포 구조, 인증 흐름 |
| [03. 데이터베이스 설계](docs/03-database-design.md) | ERD, 테이블 정의, Flyway 계획 |
| [04. API 설계](docs/04-api-design.md) | 전체 API 목록, Request/Response 명세 |
| [05. 화면 흐름 설계](docs/05-screen-flow.md) | 화면 목록, 라우팅, UX 흐름 |
| [06. 권한 정책](docs/06-permission-policy.md) | Role/Status 정의, 접근 권한 |
| [07. 배포 설계](docs/07-deployment.md) | Docker Compose, Nginx, 배포 절차 |
| [08. 일정 도메인 정책](docs/08-schedule-domain.md) | 일정 규칙, 카테고리 정책 |

---

## 진행 현황

- [x] 설계 문서 작성
- [x] Frontend 구현 (Vue 3 + Pinia)
- [x] Backend 구현 (Spring Boot 3 + JWT)
- [x] DB 스키마 Flyway 마이그레이션 (V1~V5)
- [x] Docker Compose 구성
- [x] Nginx 설정 (프론트 서빙 + API 프록시)
- [x] 로컬 Docker 환경에서 전체 스택 동작 확인
- [x] OCI 서버 세팅 완료 (Docker 설치, Swap 설정)
- [ ] OCI Security List 포트 오픈 (MFA 해결 후)
- [ ] 서버 배포
