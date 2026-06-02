ALTER TABLE schedules ADD COLUMN IF NOT EXISTS start_date DATE;
UPDATE schedules SET start_date = date WHERE start_date IS NULL;
ALTER TABLE schedules ALTER COLUMN start_date SET NOT NULL;

ALTER TABLE schedules ADD COLUMN IF NOT EXISTS team_id BIGINT;
ALTER TABLE schedules ADD COLUMN IF NOT EXISTS start_time TIME;
ALTER TABLE schedules ADD COLUMN IF NOT EXISTS end_time TIME;
ALTER TABLE schedules ADD COLUMN IF NOT EXISTS location VARCHAR(200);
ALTER TABLE schedules ADD COLUMN IF NOT EXISTS event_type VARCHAR(20);
ALTER TABLE schedules ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED';
ALTER TABLE schedules ADD COLUMN IF NOT EXISTS created_by BIGINT;
ALTER TABLE schedules ADD COLUMN IF NOT EXISTS updated_by BIGINT;

UPDATE schedules SET event_type = baseball_type WHERE baseball_type IS NOT NULL;
UPDATE schedules SET created_by = user_id WHERE created_by IS NULL;
