-- date 컬럼 NOT NULL 제약 제거 (start_date로 대체됨)
ALTER TABLE schedules ALTER COLUMN date DROP NOT NULL;
