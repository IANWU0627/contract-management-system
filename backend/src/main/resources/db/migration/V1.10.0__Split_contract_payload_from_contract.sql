-- 将合同正文与大字段从 contract 主表拆分到 contract_payload，降低主表行宽

CREATE TABLE IF NOT EXISTS contract_payload (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    content LONGTEXT,
    template_variables LONGTEXT,
    dynamic_field_values LONGTEXT,
    attachments LONGTEXT,
    content_hash VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_contract_payload_contract_id ON contract_payload(contract_id);
CREATE INDEX idx_contract_payload_updated_at ON contract_payload(updated_at);

-- 若旧列存在则回填；不存在则跳过
SET @has_legacy_payload_columns = (
    SELECT CASE WHEN COUNT(*) = 4 THEN 1 ELSE 0 END
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'contract'
      AND column_name IN ('content', 'template_variables', 'dynamic_field_values', 'attachments')
);

SET @backfill_sql = IF(
    @has_legacy_payload_columns = 1,
    'INSERT INTO contract_payload (
         contract_id,
         content,
         template_variables,
         dynamic_field_values,
         attachments,
         content_hash,
         created_at,
         updated_at
     )
     SELECT
         c.id,
         c.content,
         c.template_variables,
         c.dynamic_field_values,
         c.attachments,
         SHA2(COALESCE(c.content, ''''), 256),
         COALESCE(c.create_time, CURRENT_TIMESTAMP),
         COALESCE(c.update_time, c.create_time, CURRENT_TIMESTAMP)
     FROM contract c
     WHERE (c.content IS NOT NULL OR c.template_variables IS NOT NULL OR c.dynamic_field_values IS NOT NULL OR c.attachments IS NOT NULL)
       AND NOT EXISTS (
         SELECT 1 FROM contract_payload p WHERE p.contract_id = c.id
       )',
    'SELECT 1'
);
PREPARE stmt FROM @backfill_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'contract' AND column_name = 'content') = 1,
    'ALTER TABLE contract DROP COLUMN content',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'contract' AND column_name = 'template_variables') = 1,
    'ALTER TABLE contract DROP COLUMN template_variables',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'contract' AND column_name = 'dynamic_field_values') = 1,
    'ALTER TABLE contract DROP COLUMN dynamic_field_values',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'contract' AND column_name = 'attachments') = 1,
    'ALTER TABLE contract DROP COLUMN attachments',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
