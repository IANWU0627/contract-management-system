CREATE TABLE IF NOT EXISTS contract_clause (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    name_en VARCHAR(255),
    category VARCHAR(100),
    content TEXT,
    description VARCHAR(500),
    status TINYINT DEFAULT 1,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_contract_clause_code
ON contract_clause(code);

CREATE INDEX IF NOT EXISTS idx_contract_clause_category
ON contract_clause(category);

CREATE INDEX IF NOT EXISTS idx_contract_clause_status
ON contract_clause(status);
