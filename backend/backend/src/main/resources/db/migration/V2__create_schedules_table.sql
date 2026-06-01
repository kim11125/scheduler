CREATE TABLE schedules (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users(id),
    title        VARCHAR(100) NOT NULL,
    category     VARCHAR(30)  NOT NULL,
    baseball_type VARCHAR(10) NULL,
    date         DATE         NOT NULL,
    memo         TEXT         NULL,
    deleted_at   TIMESTAMP    NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_schedules_user_id ON schedules(user_id);
CREATE INDEX idx_schedules_date    ON schedules(date);
