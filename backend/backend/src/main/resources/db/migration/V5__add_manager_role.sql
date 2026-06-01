-- Role 컬럼 타입을 VARCHAR로 변경 (enum → varchar로 관리)
-- PostgreSQL enum은 ALTER가 복잡하므로 VARCHAR로 처리
ALTER TABLE users ALTER COLUMN role TYPE VARCHAR(20);

-- 기존 ADMIN은 그대로 유지
-- MANAGER 역할 추가 가능 상태
