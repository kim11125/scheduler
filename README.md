# 개인 스케줄 관리 웹앱

관리자 승인 기반의 폐쇄형 개인 스케줄 관리 웹앱이다.
회원가입 신청 후 관리자가 승인해야 서비스를 이용할 수 있다.
각 사용자는 본인의 일정만 관리하며, 관리자는 전체 일정을 조회하고 관리한다.

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| Frontend | Vue 3 + TypeScript + Vite |
| Backend | Java 21 + Spring Boot 3 |
| Database | PostgreSQL 16 |
| Auth | Spring Security + JWT (Access Token, 24시간 만료) |
| Password | BCrypt |
| ORM | Spring Data JPA |
| Migration | Flyway |
| Infra | Oracle Linux + Docker Compose + Nginx |
| Build | Gradle |
| API Style | REST API |

---

## 설계 문서 목록

| 문서 | 내용 |
|------|------|
| [01. 요구사항 정의](docs/01-requirements.md) | 서비스 목적, 사용자 유형, 핵심 기능, MVP 범위, 향후 확장 |
| [02. 시스템 아키텍처](docs/02-architecture.md) | 전체 구조, 배포 구조, 요청 흐름, 인증 흐름, 디렉토리 구조 |
| [03. 데이터베이스 설계](docs/03-database-design.md) | ERD, 테이블 정의, 컬럼/타입/제약조건, 인덱스, Flyway 계획 |
| [04. API 설계](docs/04-api-design.md) | 전체 API 목록, Request/Response 명세, 권한 요약표 |
| [05. 화면 흐름 설계](docs/05-screen-flow.md) | 화면 목록, 라우팅, 상태별 분기, 모바일 UI 구성, UX 흐름 |
| [06. 권한 정책](docs/06-permission-policy.md) | Role/Status 정의, API·화면별 접근 권한, 예외 케이스 |
| [07. 배포 설계](docs/07-deployment.md) | Docker Compose 구성, Nginx 설정, 환경변수, 배포 절차 |
| [08. 일정 도메인 정책](docs/08-schedule-domain.md) | 일정 입력/수정/삭제 규칙, 카테고리 정책, 조회 정책 |

---

## 개발 시작 전 확인해야 할 사항

### 1. 환경변수 설정

```bash
cp .env.example .env
# .env 파일에서 아래 항목을 반드시 설정한다
# - DB_PASSWORD: 강한 비밀번호
# - JWT_SECRET: 64자 이상 랜덤 문자열
# - CORS_ALLOWED_ORIGINS: 실제 서버 IP
```

### 2. 초기 관리자 계정

- `backend/src/main/resources/db/migration/V2__init_admin.sql`에서 초기 ADMIN 계정을 설정한다.
- `password_hash`는 배포 전 BCrypt로 직접 생성하여 입력한다.
- 배포 직후 초기 비밀번호를 변경한다. (비밀번호 변경 기능은 향후 구현 예정)

### 3. 운영 환경 주의사항

- `.env` 파일을 절대 git에 커밋하지 않는다.
- `docker-compose down -v` 명령어를 운영 환경에서 사용하지 않는다. (PostgreSQL 볼륨 삭제됨)
- 외부에서 5432, 8080 포트가 노출되지 않도록 방화벽 설정을 확인한다.

### 4. 설계 문서 확인 순서 (신규 개발자)

1. `01-requirements.md`: 서비스 목적과 기능 범위 파악
2. `06-permission-policy.md`: Role/Status 구조 및 권한 정책 숙지
3. `08-schedule-domain.md`: 일정 도메인 상세 규칙 확인
4. `03-database-design.md`: DB 스키마 파악
5. `04-api-design.md`: API 명세 확인
6. `02-architecture.md`: 전체 시스템 구조 파악
7. `05-screen-flow.md`: 화면 흐름 및 UX 정책 확인
8. `07-deployment.md`: 배포 절차 확인

---

## 현재 상태

- [x] 설계 문서 작성 완료
- [ ] 백엔드 프로젝트 초기화
- [ ] 프론트엔드 프로젝트 초기화
- [ ] DB 스키마 Flyway migration 작성
- [ ] Docker Compose 구성
- [ ] Nginx 설정
