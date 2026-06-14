CREATE TABLE IF NOT EXISTS abcdish.background_jobs (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payload_json TEXT NOT NULL,
    dedupe_key VARCHAR(255) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,
    available_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    locked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT uk_background_jobs_dedupe_key UNIQUE (dedupe_key)
);

CREATE INDEX IF NOT EXISTS idx_background_jobs_claim
ON abcdish.background_jobs(status, available_at, id);

CREATE INDEX IF NOT EXISTS idx_background_jobs_type_status
ON abcdish.background_jobs(type, status);
