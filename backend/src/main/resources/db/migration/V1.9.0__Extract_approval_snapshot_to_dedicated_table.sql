-- 将审批快照从 contract 主表拆分到独立表 contract_snapshot，降低主表体积与查询负担

CREATE TABLE IF NOT EXISTS contract_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    snapshot_type VARCHAR(32) NOT NULL,
    content LONGTEXT,
    template_variables LONGTEXT,
    snapshot_meta LONGTEXT,
    content_hash VARCHAR(64),
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_contract_snapshot_contract_time ON contract_snapshot(contract_id, created_at);

-- 兼容旧库：若存在历史快照列，则安全删除（不依赖 MySQL 8 的 DROP COLUMN IF EXISTS）
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE()
       AND table_name = 'contract'
       AND column_name = 'approval_snapshot_content') = 1,
    'ALTER TABLE contract DROP COLUMN approval_snapshot_content',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE()
       AND table_name = 'contract'
       AND column_name = 'template_variables_snapshot') = 1,
    'ALTER TABLE contract DROP COLUMN template_variables_snapshot',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE()
       AND table_name = 'contract'
       AND column_name = 'approval_snapshot_meta') = 1,
    'ALTER TABLE contract DROP COLUMN approval_snapshot_meta',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
