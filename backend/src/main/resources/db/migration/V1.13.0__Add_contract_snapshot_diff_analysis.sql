CREATE TABLE IF NOT EXISTS contract_snapshot_diff_analysis (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    base_snapshot_id BIGINT NOT NULL,
    target_snapshot_id BIGINT NOT NULL,
    diff_json LONGTEXT,
    risk_json LONGTEXT,
    overall_risk VARCHAR(20) DEFAULT 'LOW',
    model_name VARCHAR(100) DEFAULT 'rule-engine',
    prompt_version VARCHAR(32) DEFAULT 'v1',
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_snapshot_diff_contract_snapshots
ON contract_snapshot_diff_analysis(contract_id, base_snapshot_id, target_snapshot_id);

CREATE INDEX idx_snapshot_diff_updated_at
ON contract_snapshot_diff_analysis(updated_at);
