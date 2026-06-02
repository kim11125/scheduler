ALTER TABLE schedules ADD CONSTRAINT fk_schedules_team_id FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL;
ALTER TABLE schedules ADD CONSTRAINT fk_schedules_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE schedules ADD CONSTRAINT fk_schedules_updated_by FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_schedules_team_id ON schedules(team_id);
CREATE INDEX IF NOT EXISTS idx_schedules_start_date ON schedules(start_date);
CREATE INDEX IF NOT EXISTS idx_schedules_status ON schedules(status);
