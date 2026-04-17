CREATE TABLE IF NOT EXISTS contract_version_diff_analysis (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    base_version_id BIGINT NOT NULL,
    target_version_id BIGINT NOT NULL,
    diff_json LONGTEXT,
    risk_json LONGTEXT,
    overall_risk VARCHAR(20) DEFAULT 'LOW',
    model_name VARCHAR(100) DEFAULT 'rule-engine',
    prompt_version VARCHAR(32) DEFAULT 'v1',
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_contract_diff_contract_versions
ON contract_version_diff_analysis(contract_id, base_version_id, target_version_id);

CREATE INDEX idx_contract_diff_updated_at
ON contract_version_diff_analysis(updated_at);
