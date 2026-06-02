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
| Auth | Spring Security + JWT (Access Token, 24시간 만료, token_version 검증) |
| Password | BCrypt |
| ORM | Spring Data JPA + Hibernate |
| Migration | Flyway (V1~V15) |
| Infra | Oracle Cloud (Ubuntu 20.04) + Docker Compose + Nginx |
| Build | Gradle |
| API Style | REST API |

---

## 권한 구조

| Role | 명칭 | 권한 |
|------|------|------|
| `ADMIN` | 최고 관리자 | 모든 권한 + 관리자 임명/해제 + 로그인 로그 조회 |
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
- 헤더 **내 정보** / **로그아웃** 버튼 분리
- 프로필 사진 업로드/변경/삭제
- 로그인 아이디 변경 (변경 후 JWT 자동 무효화)
- 비밀번호 변경

### 관리자
- 회원가입 승인 / 거절 / 비활성화 / 재활성화
- 사용자 역할 변경 (ADMIN 전용)
- 사용자 비밀번호 / 로그인 아이디 강제 변경
- 사용자 프로필 사진 관리
- 전체 일정 조회 (목록 뷰 / 캘린더 뷰)
- 사용자별 / 카테고리별 일정 필터링
- 다른 사용자 대신 일정 추가
- 로그인/로그아웃 이력 조회 (ADMIN 전용)
- **회사 관리** (등록 / 수정 / 활성화·비활성화)
- **팀 관리** (등록 / 수정 / 활성화·비활성화)
- **회사-팀 연결** 관리
- **사용자-회사 연결** (1:1)
- **사용자-팀 연결** (다수)

---

## DB 스키마 (Flyway 마이그레이션)

| 버전 | 내용 |
|------|------|
| V1 | users 테이블 생성 |
| V2 | schedules 테이블 생성 |
| V3 | 초기 ADMIN 계정 INSERT |
| V4 | schedules.end_date 추가 |
| V5 | users.role VARCHAR 변환 (MANAGER 추가) |
| V6 | login_logs 테이블 생성 |
| V7 | users.profile_image_url, token_version 추가 |
| V8 | schedules 확장 (team_id, start_time, end_time, location, status 등) |
| V9 | companies 테이블 생성 |
| V10 | teams 테이블 생성 |
| V11 | company_teams 연결 테이블 생성 |
| V12 | user_companies 연결 테이블 생성 |
| V13 | user_teams 연결 테이블 생성 |
| V14 | user_profile_change_logs 테이블 생성 |
| V15 | 인덱스 및 FK 추가 |

---

## 프로젝트 구조

```
scheduler/
├── docs/                        # 설계 문서
├── frontend/                    # Vue 3 프론트엔드
│   └── src/
│       ├── api/                 # axios API 클라이언트
│       ├── stores/              # Pinia 상태 관리
│       ├── views/               # 페이지 컴포넌트
│       │   └── admin/           # 관리자 화면
│       ├── router/              # Vue Router
│       ├── composables/
│       ├── styles/              # 테마 CSS 변수
│       └── types/
├── backend/backend/             # Spring Boot 백엔드
│   └── src/main/
│       ├── java/com/scheduler/backend/
│       │   ├── domain/          # auth, schedule, user, admin, company, team, log
│       │   └── global/          # jwt, config, exception, storage
│       └── resources/
│           └── db/migration/    # Flyway SQL (V1~V15)
├── nginx/
│   └── nginx.conf
├── docker-compose.yml
├── PROGRESS.md
└── README.md
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
cd frontend && npm install && npm run build && cd ..

# 2. 백엔드 빌드
cd backend/backend && ./gradlew bootJar -x test && cd ../..

# 3. 전체 스택 실행
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
- 방화벽 포트: 80, 443 (OCI Security List + OS iptables)

### 배포 스크립트

```bash
# 프론트엔드만 업데이트
sh ~/scheduler/deploy_frontend.sh

# 전체 업데이트 (백엔드 포함)
sh ~/scheduler/deploy_all.sh

# 상태 확인
sh ~/scheduler/status.sh
```

---

## 진행 현황

- [x] 설계 문서 작성
- [x] Frontend 구현 (Vue 3 + Pinia)
- [x] Backend 구현 (Spring Boot 3 + JWT)
- [x] DB 스키마 Flyway 마이그레이션 (V1~V15)
- [x] Docker Compose 구성
- [x] Nginx 설정 (프론트 서빙 + API 프록시)
- [x] 로컬 Docker 환경 전체 스택 동작 확인
- [x] OCI 서버 배포 완료 (http://168.107.15.194)
- [x] OCI Security List 80/443 포트 오픈
- [x] 회사/팀 관리 기능
- [x] 프로필 사진 업로드
- [x] 로그인 아이디 변경 + JWT 무효화
- [x] 로그인/로그아웃 이력 관리
- [ ] HTTPS (도메인 필요)
- [ ] 자동 백업
