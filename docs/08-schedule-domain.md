# 08. 일정 도메인 상세 정책

## 1. 일정 도메인 개요

일정은 날짜 단위로 관리한다. 시간(시작/종료) 개념은 없다.
같은 날짜에 여러 일정을 등록할 수 있다.
각 사용자는 본인의 일정만 조회/수정/삭제할 수 있다. (관리자 제외)

---

## 2. 일정 데이터 모델 (확정)

```
Schedule
├── id           BIGSERIAL       PK
├── user_id      BIGINT          FK → users(id), NOT NULL
├── title        VARCHAR(100)    NOT NULL
├── category     VARCHAR(20)     NOT NULL (enum)
├── baseball_type VARCHAR(10)    NULL (category=BASEBALL일 때만 값 존재)
├── date         DATE            NOT NULL
├── memo         TEXT            NULL
├── created_at   TIMESTAMP       NOT NULL
├── updated_at   TIMESTAMP       NOT NULL
└── deleted_at   TIMESTAMP       NULL (soft delete)
```

---

## 3. 카테고리 정책

| 값 | 표시명 | 설명 |
|----|--------|------|
| `BASEBALL` | 야구 | 홈/원정 추가 입력 필수 |
| `BASKETBALL` | 농구 | 추가 입력 없음 |
| `SOCCER` | 축구 | 추가 입력 없음 |
| `WOMENS_VOLLEYBALL` | 여자배구 | 추가 입력 없음 |
| `MENS_VOLLEYBALL` | 남자배구 | 추가 입력 없음 |
| `ETC` | 기타 | 추가 입력 없음. 메모로 내용 입력 |

- 카테고리는 일정 등록/수정 시 반드시 선택해야 한다.
- 카테고리는 라디오 버튼으로 입력한다.
- `ETC` 선택 시 별도 텍스트 필드를 표시하지 않는다. 메모 필드에 기타 내용을 입력한다.

---

## 4. 카테고리별 조건부 입력 정책

### 4-1. 야구(BASEBALL) 선택 시

- 홈/원정 라디오 버튼이 추가로 표시된다.
- 홈/원정 선택은 **필수**이다. 선택하지 않으면 저장이 불가능하다.
- 저장 시 `baseball_type` 값: `HOME` 또는 `AWAY`

### 4-2. 야구 외 카테고리 선택 시

- 홈/원정 입력 항목을 표시하지 않는다.
- 저장 시 `baseball_type` 값: `NULL`
- 이전에 야구를 선택하다가 다른 카테고리로 변경하면, 홈/원정 입력이 즉시 숨겨지고 `baseball_type`은 null로 처리된다.

### 4-3. 프론트엔드 동작 정리

```
카테고리 선택
  ├── BASEBALL → [홈/원정 라디오 표시]  (baseballType 선택 필수)
  └── 그 외   → [홈/원정 숨김]          (baseballType = null)
```

---

## 5. 일정 입력 항목 상세

| 필드 | 입력 유형 | 필수 | 규칙 |
|------|-----------|------|------|
| 날짜 | 날짜 선택기 | 필수 | `YYYY-MM-DD` 형식 |
| 제목 | 텍스트 입력 | 필수 | 1~100자 |
| 카테고리 | 라디오 버튼 | 필수 | 6개 중 1개 선택 |
| 홈/원정 | 라디오 버튼 | 조건부 필수 | 야구 선택 시만 표시 및 필수 |
| 메모 | 텍스트 영역 | 선택 | 최대 2000자. 빈 값 허용 |

---

## 6. 날짜 선택 및 입력 UX

### 6-1. 캘린더에서 날짜 탭 → 일정 등록 진입

1. 메인 화면(`/`) 월간 캘린더에서 날짜 셀을 탭한다.
2. 하단 패널이 해당 날짜의 일정 목록으로 전환된다.
3. 패널 내 **[+ 일정 추가]** 버튼을 탭한다.
4. `/schedules/new?date=2026-06-01` 화면으로 이동한다.
5. 날짜 필드에 `2026-06-01`이 기본값으로 세팅된다.

### 6-2. 일정 등록/수정 화면에서 날짜 변경

- 날짜 필드는 편집 가능하다.
- 날짜 선택기(date picker)를 통해 원하는 날짜로 변경할 수 있다.
- 날짜를 변경해도 카테고리, 제목, 메모 입력값은 유지된다.

### 6-3. 시간 입력 없음

- `startTime`, `endTime` 필드는 존재하지 않는다.
- 모든 일정은 날짜(DATE) 단위로만 관리한다.

---

## 7. 일정 생성 규칙

- 로그인한 사용자의 userId가 자동으로 `user_id`에 저장된다. 클라이언트에서 userId를 지정할 수 없다.
- `category = BASEBALL`이고 `baseball_type`이 없으면 서버에서 400 오류 반환.
- `category ≠ BASEBALL`이면 요청에 `baseball_type`이 포함되어 있어도 서버에서 `null`로 강제 저장한다.
- 메모가 빈 문자열(`""`)로 전송되면 `NULL`로 저장한다.
- 같은 날짜에 여러 일정 등록이 허용된다. 유니크 제약 없음.
- `created_at`, `updated_at`은 서버에서 자동 설정. 클라이언트에서 지정 불가.

---

## 8. 일정 수정 규칙

- `PUT /api/schedules/{id}`: 전체 교체(full replacement) 방식. 모든 필드를 요청에 포함해야 한다.
- 작성자 본인 또는 ADMIN만 수정 가능하다.
- 수정 성공 시 `updated_at`이 현재 시각으로 갱신된다.
- 수정 시에도 생성 규칙과 동일한 유효성 검사가 적용된다.

---

## 9. 일정 삭제 규칙

- Soft Delete 방식: `deleted_at`에 현재 시각을 기록한다. DB에서 실제 행이 삭제되지 않는다.
- 삭제된 일정은 모든 목록 조회 API에서 제외된다 (`deleted_at IS NULL` 조건 적용).
- 삭제된 일정 ID로 상세 조회, 수정, 삭제 재시도 시 `404 NOT_FOUND` 반환.
- 삭제된 일정은 복구할 수 없다. (MVP 범위 외)

---

## 10. 일정 목록 정렬 기준

- 기본 정렬: `created_at DESC` (등록일 내림차순)
- 날짜 필터 사용 시에도 동일 정렬 기준 적용

---

## 11. 관리자 일정 조회 정책

- ADMIN은 전체 사용자의 일정을 조회할 수 있다.
- 관리자 조회 API(`/api/admin/schedules`)는 응답에 `userId`, `userName` 필드를 포함한다.
- 필터 옵션: `userId`, `date`, `category`
- 삭제된 일정(`deleted_at IS NOT NULL`)은 관리자 조회에서도 제외한다.

---

## 12. 일반 사용자 일정 조회 정책

- `/api/schedules`는 본인의 일정만 반환한다. 쿼리 파라미터로 다른 사용자 지정 불가.
- 월간 조회: `?month=2026-06` → 해당 월 1일~말일의 모든 일정 반환. 캘린더 렌더링에 사용.
- 날짜 조회: `?date=2026-06-01` → 해당 날짜의 모든 일정 반환. 하단 패널에 사용.
- `month`와 `date`를 동시에 지정한 경우 `date`를 우선한다.

---

## 13. AuditLog 기록 정책

일정 관련 행위는 아래와 같이 AuditLog에 기록한다.

| 행위 | action_type | target_type | target_id |
|------|-------------|-------------|-----------|
| 일정 생성 | `SCHEDULE_CREATED` | `SCHEDULE` | schedule.id |
| 일정 수정 | `SCHEDULE_UPDATED` | `SCHEDULE` | schedule.id |
| 일정 삭제 | `SCHEDULE_DELETED` | `SCHEDULE` | schedule.id |

- `actor_user_id`: 행위를 수행한 사용자의 id (관리자가 삭제해도 관리자 id 기록)

---

## 14. 확인 필요 사항

없음. 모든 일정 도메인 정책이 확정되었음.
