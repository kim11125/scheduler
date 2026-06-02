CREATE TABLE IF NOT EXISTS user_profile_change_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    changed_by BIGINT NOT NULL REFERENCES users(id),
    change_type VARCHAR(50) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_profile_logs_user_id ON user_profile_change_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_profile_logs_created_at ON user_profile_change_logs(created_at);
