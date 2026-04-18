-- 合同类型字段草稿表：与 V1.16.0 定义一致。
-- 旧环境若曾由应用内不完整 DDL 建表，可能缺列；此处用 information_schema 判断后按需 ADD，避免重复列错误。
-- 应用代码不再在运行时 ALTER 本表。

CREATE TABLE IF NOT EXISTS contract_type_field_draft (
    contract_type VARCHAR(50) PRIMARY KEY,
    fields_json LONGTEXT NOT NULL,
    draft_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL,
    publish_version INT DEFAULT 0
);

SET @sch := DATABASE();

-- fields_json
SET @c := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @sch AND TABLE_NAME = 'contract_type_field_draft' AND COLUMN_NAME = 'fields_json'
);
SET @t := (
    SELECT COUNT(*) FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = @sch AND TABLE_NAME = 'contract_type_field_draft'
);
SET @q := IF(@c = 0 AND @t > 0,
    'ALTER TABLE contract_type_field_draft ADD COLUMN fields_json LONGTEXT NOT NULL',
    'SELECT 1');
PREPARE _stmt FROM @q;
EXECUTE _stmt;
DEALLOCATE PREPARE _stmt;

-- draft_updated_at
SET @c := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @sch AND TABLE_NAME = 'contract_type_field_draft' AND COLUMN_NAME = 'draft_updated_at'
);
SET @q := IF(@c = 0 AND @t > 0,
    'ALTER TABLE contract_type_field_draft ADD COLUMN draft_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP',
    'SELECT 1');
PREPARE _stmt FROM @q;
EXECUTE _stmt;
DEALLOCATE PREPARE _stmt;

-- published_at
SET @c := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @sch AND TABLE_NAME = 'contract_type_field_draft' AND COLUMN_NAME = 'published_at'
);
SET @q := IF(@c = 0 AND @t > 0,
    'ALTER TABLE contract_type_field_draft ADD COLUMN published_at TIMESTAMP NULL',
    'SELECT 1');
PREPARE _stmt FROM @q;
EXECUTE _stmt;
DEALLOCATE PREPARE _stmt;

-- publish_version
SET @c := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @sch AND TABLE_NAME = 'contract_type_field_draft' AND COLUMN_NAME = 'publish_version'
);
SET @q := IF(@c = 0 AND @t > 0,
    'ALTER TABLE contract_type_field_draft ADD COLUMN publish_version INT DEFAULT 0',
    'SELECT 1');
PREPARE _stmt FROM @q;
EXECUTE _stmt;
DEALLOCATE PREPARE _stmt;
