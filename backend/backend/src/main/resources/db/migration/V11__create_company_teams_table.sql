CREATE TABLE IF NOT EXISTS company_teams (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    team_id BIGINT NOT NULL REFERENCES teams(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_company_teams UNIQUE (company_id, team_id)
);
CREATE INDEX IF NOT EXISTS idx_company_teams_company_id ON company_teams(company_id);
CREATE INDEX IF NOT EXISTS idx_company_teams_team_id ON company_teams(team_id);
