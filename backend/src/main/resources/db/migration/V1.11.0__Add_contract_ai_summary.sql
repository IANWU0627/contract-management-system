CREATE TABLE IF NOT EXISTS contract_ai_summary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    snapshot_id BIGINT NULL,
    summary_text LONGTEXT,
    key_terms_json LONGTEXT,
    risks_json LONGTEXT,
    confidence_score INT DEFAULT 0,
    model_name VARCHAR(100),
    summary_version VARCHAR(32) DEFAULT 'v1',
    status VARCHAR(32) DEFAULT 'SUCCESS',
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_contract_ai_summary_contract_updated
ON contract_ai_summary(contract_id, updated_at);

CREATE INDEX idx_contract_ai_summary_snapshot
ON contract_ai_summary(snapshot_id);
