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

SET @exists := (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'contract_version_diff_analysis'
      AND column_name = 'diff_json'
);
SET @sql := IF(@exists = 0,
    'ALTER TABLE contract_version_diff_analysis ADD COLUMN diff_json LONGTEXT',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists := (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'contract_version_diff_analysis'
      AND column_name = 'risk_json'
);
SET @sql := IF(@exists = 0,
    'ALTER TABLE contract_version_diff_analysis ADD COLUMN risk_json LONGTEXT',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists := (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'contract_snapshot_diff_analysis'
      AND column_name = 'diff_json'
);
SET @sql := IF(@exists = 0,
    'ALTER TABLE contract_snapshot_diff_analysis ADD COLUMN diff_json LONGTEXT',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists := (
    SELECT COUNT(1) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'contract_snapshot_diff_analysis'
      AND column_name = 'risk_json'
);
SET @sql := IF(@exists = 0,
    'ALTER TABLE contract_snapshot_diff_analysis ADD COLUMN risk_json LONGTEXT',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'contract_version_diff_analysis'
      AND index_name = 'idx_contract_diff_contract_versions'
);
SET @sql := IF(@idx_exists = 0,
    'CREATE INDEX idx_contract_diff_contract_versions ON contract_version_diff_analysis(contract_id, base_version_id, target_version_id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'contract_version_diff_analysis'
      AND index_name = 'idx_contract_diff_updated_at'
);
SET @sql := IF(@idx_exists = 0,
    'CREATE INDEX idx_contract_diff_updated_at ON contract_version_diff_analysis(updated_at)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'contract_snapshot_diff_analysis'
      AND index_name = 'idx_snapshot_diff_contract_snapshots'
);
SET @sql := IF(@idx_exists = 0,
    'CREATE INDEX idx_snapshot_diff_contract_snapshots ON contract_snapshot_diff_analysis(contract_id, base_snapshot_id, target_snapshot_id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'contract_snapshot_diff_analysis'
      AND index_name = 'idx_snapshot_diff_updated_at'
);
SET @sql := IF(@idx_exists = 0,
    'CREATE INDEX idx_snapshot_diff_updated_at ON contract_snapshot_diff_analysis(updated_at)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
