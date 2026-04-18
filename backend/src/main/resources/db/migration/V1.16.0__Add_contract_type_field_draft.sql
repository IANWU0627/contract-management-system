CREATE TABLE IF NOT EXISTS contract_type_field_draft (
    contract_type VARCHAR(50) PRIMARY KEY,
    fields_json LONGTEXT NOT NULL,
    draft_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL,
    publish_version INT DEFAULT 0
);
