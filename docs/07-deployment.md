# 07. 배포 설계

## 1. Oracle Linux 서버 기준 배포 구조

- 서버: Oracle Linux 단일 서버
- 컨테이너: Docker + Docker Compose
- 외부 노출 포트: **80 (HTTP)**
- 내부 통신: Docker 브리지 네트워크 (`app-network`)

```
외부 (브라우저)
    |
    | :80 (HTTP)
    ↓
[Nginx 컨테이너]
    ├── /api/* → [backend 컨테이너:8080]
    │                   ↓
    │           [db 컨테이너:5432]
    │
    └── /* → 정적 파일 (Vue 빌드 산출물)
```

---

## 2. Docker Compose 구성 계획

### 2-1. 운영용 `docker-compose.yml`

```yaml
version: '3.8'

services:
  db:
    image: postgres:16
    container_name: schedule-db
    environment:
      POSTGRES_DB: ${DB_NAME}
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - app-network
    restart: unless-stopped

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: schedule-backend
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: db
      DB_PORT: 5432
      DB_NAME: ${DB_NAME}
      DB_USER: ${DB_USER}
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION_MS: ${JWT_EXPIRATION_MS}
      CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS}
    depends_on:
      - db
    networks:
      - app-network
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    container_name: schedule-nginx
    ports:
      - "80:80"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./frontend/dist:/usr/share/nginx/html:ro
    depends_on:
      - backend
    networks:
      - app-network
    restart: unless-stopped

volumes:
  postgres_data:

networks:
  app-network:
    driver: bridge
```

### 2-2. 개발용 `docker-compose.dev.yml`

```yaml
version: '3.8'

services:
  db:
    image: postgres:16
    container_name: schedule-db-dev
    environment:
      POSTGRES_DB: scheduledb_dev
      POSTGRES_USER: devuser
      POSTGRES_PASSWORD: devpass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data_dev:/var/lib/postgresql/data
    networks:
      - app-network-dev

volumes:
  postgres_data_dev:

networks:
  app-network-dev:
    driver: bridge
```

개발 시 백엔드와 프론트는 로컬에서 직접 실행한다:
- 백엔드: `./gradlew bootRun --args='--spring.profiles.active=dev'`
- 프론트: `npm run dev` (Vite dev server, localhost:5173)

---

## 3. Nginx Reverse Proxy 구조

### `nginx/nginx.conf`

```nginx
server {
    listen 80;
    server_name _;

    # Vue SPA 정적 파일
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # API 역방향 프록시
    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 60s;
        proxy_connect_timeout 10s;
    }

    # 향후 HTTPS 적용 시 아래 주석 해제 후 수정
    # listen 443 ssl;
    # ssl_certificate /etc/nginx/certs/fullchain.pem;
    # ssl_certificate_key /etc/nginx/certs/privkey.pem;
}
```

---

## 4. PostgreSQL 볼륨 전략

| 항목 | 내용 |
|------|------|
| 볼륨 이름 | `postgres_data` (Docker named volume) |
| 마운트 경로 | 컨테이너 내부 `/var/lib/postgresql/data` |
| 호스트 경로 | Docker 관리 영역 (`/var/lib/docker/volumes/postgres_data/`) |
| 컨테이너 재시작 시 | 데이터 보존됨 |
| 컨테이너 삭제 시 | `docker-compose down` → 볼륨 유지 / `docker-compose down -v` → 볼륨 삭제 주의 |

> 운영 환경에서는 `docker-compose down -v` 명령어를 절대 사용하지 않는다.

---

## 5. 환경변수 목록

`.env.example` 파일에 아래 항목을 정의하고, `.env` 파일은 `.gitignore`에 추가한다.

```dotenv
# Database
DB_NAME=scheduledb
DB_USER=scheduleuser
DB_PASSWORD=<강한 비밀번호>

# JWT
JWT_SECRET=<최소 256비트 이상의 랜덤 문자열>
JWT_EXPIRATION_MS=86400000

# CORS
CORS_ALLOWED_ORIGINS=http://<서버IP>
```

| 변수명 | 설명 | 예시 |
|--------|------|------|
| `DB_NAME` | PostgreSQL DB 이름 | `scheduledb` |
| `DB_USER` | PostgreSQL 사용자명 | `scheduleuser` |
| `DB_PASSWORD` | PostgreSQL 비밀번호 | 강한 랜덤값 |
| `JWT_SECRET` | JWT 서명 시크릿 (HMAC-SHA256) | 64자 이상 랜덤 문자열 |
| `JWT_EXPIRATION_MS` | JWT 만료 시간 (밀리초) | `86400000` (24시간) |
| `CORS_ALLOWED_ORIGINS` | 허용 출처 | `http://192.168.0.100` |

---

## 6. 로그 관리

| 컨테이너 | 로그 저장 방식 |
|----------|----------------|
| nginx | Docker 기본 로그 드라이버 (`docker logs schedule-nginx`) |
| backend | Logback → 표준 출력. Docker 기본 로그 드라이버 수집 |
| db | Docker 기본 로그 드라이버 |

**운영 로그 레벨:**
- `INFO` 이상만 출력
- `com.example.app`: `INFO`
- `org.springframework.security`: `WARN`
- `org.hibernate.SQL`: `WARN` (운영에서 SQL 로그 비활성화)

**로그 조회 명령어:**
```bash
docker logs schedule-backend --tail 100 -f
docker logs schedule-nginx --tail 50 -f
```

---

## 7. 백업 전략

MVP 단계에서 자동 백업은 구현하지 않는다.
수동 백업이 필요할 때 아래 명령어를 사용한다.

```bash
# PostgreSQL 수동 덤프
docker exec schedule-db pg_dump -U $DB_USER $DB_NAME > backup_$(date +%Y%m%d_%H%M%S).sql

# 복원
docker exec -i schedule-db psql -U $DB_USER $DB_NAME < backup_YYYYMMDD_HHMMSS.sql
```

향후 확장: cron + 쉘 스크립트로 자동화 가능.

---

## 8. 빌드 및 배포 절차

### 최초 배포

```bash
# 1. 프로젝트 클론
git clone <repo-url>
cd <project-dir>

# 2. 환경변수 파일 생성
cp .env.example .env
# .env 파일 편집 (비밀번호, JWT 시크릿 등 설정)

# 3. 프론트엔드 빌드
cd frontend
npm install
npm run build
cd ..

# 4. 백엔드 + DB + Nginx 실행
docker-compose up -d --build

# 5. Flyway migration 확인 (백엔드 기동 시 자동 실행)
docker logs schedule-backend | grep -i flyway
```

### 업데이트 배포

```bash
# 1. 코드 업데이트
git pull

# 2. 프론트엔드 재빌드
cd frontend && npm run build && cd ..

# 3. 백엔드 재빌드 및 재시작
docker-compose up -d --build backend

# 4. Nginx 재시작 (프론트 변경 시)
docker-compose restart nginx
```

---

## 9. 운영 전 체크리스트

- [ ] `.env` 파일 생성 완료 및 모든 필수 환경변수 설정
- [ ] `JWT_SECRET`이 충분히 강한 값(64자 이상 랜덤)으로 설정됨
- [ ] `DB_PASSWORD`가 강한 값으로 설정됨
- [ ] `.env` 파일이 `.gitignore`에 포함됨
- [ ] 프론트엔드 `npm run build` 성공 및 `dist/` 폴더 생성 확인
- [ ] `docker-compose up -d` 실행 후 모든 컨테이너 `Up` 상태 확인
- [ ] `docker logs schedule-backend`에서 Flyway migration 성공 확인
- [ ] `http://<서버IP>/api/auth/me` 접근 시 401 응답 확인 (API 정상 작동)
- [ ] `http://<서버IP>/` 접근 시 Vue 앱 로딩 확인
- [ ] 초기 admin 계정으로 로그인 성공 확인
- [ ] 5432 포트가 외부에 열려있지 않은지 방화벽 확인
- [ ] 8080 포트가 외부에 열려있지 않은지 방화벽 확인

---

## 10. 향후 HTTPS 적용 계획

도메인 확보 후 아래 절차로 Let's Encrypt 인증서 적용:

1. Certbot 설치 및 인증서 발급
2. `nginx.conf`에 443 SSL 설정 추가
3. HTTP → HTTPS 리다이렉트 설정
4. 자동 인증서 갱신 cron 설정
5. `CORS_ALLOWED_ORIGINS`를 `https://<도메인>`으로 변경

---

## 11. 확인 필요 사항

없음. 모든 배포 항목이 확정되었음.
