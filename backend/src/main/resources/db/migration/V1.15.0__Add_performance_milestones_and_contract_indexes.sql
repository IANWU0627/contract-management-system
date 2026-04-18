-- 列表与筛选常用组合索引（幂等）
SET @idx := (SELECT COUNT(1) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'contract' AND index_name = 'idx_contract_status_create_time');
SET @sql := IF(@idx = 0, 'CREATE INDEX idx_contract_status_create_time ON contract(status, create_time)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx := (SELECT COUNT(1) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'contract' AND index_name = 'idx_contract_creator_create_time');
SET @sql := IF(@idx = 0, 'CREATE INDEX idx_contract_creator_create_time ON contract(creator_id, create_time)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx := (SELECT COUNT(1) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'contract' AND index_name = 'idx_contract_folder_create_time');
SET @sql := IF(@idx = 0, 'CREATE INDEX idx_contract_folder_create_time ON contract(folder_id, create_time)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS contract_performance_milestone (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    title VARCHAR(512) NOT NULL,
    due_date DATE NULL,
    offset_days INT NULL,
    anchor_note VARCHAR(64) NULL,
    raw_snippet VARCHAR(1024) NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    source VARCHAR(32) DEFAULT 'EXTRACTED',
    last_reminded_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cpm_contract ON contract_performance_milestone(contract_id);
CREATE INDEX IF NOT EXISTS idx_cpm_due_status ON contract_performance_milestone(due_date, status);
