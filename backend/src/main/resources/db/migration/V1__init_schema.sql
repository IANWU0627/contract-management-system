-- 初始化脚本
-- 运行此脚本添加缺失的字段和表

-- 检查 contract_tag 表是否存在 description 列，不存在则添加
ALTER TABLE contract_tag ADD COLUMN IF NOT EXISTS description VARCHAR(255) DEFAULT '';

-- 检查 contract_template 表是否存在 usage_count 列，不存在则添加
ALTER TABLE contract_template ADD COLUMN IF NOT EXISTS usage_count BIGINT DEFAULT 0;

-- 检查 contract_reminder 表是否存在 is_read 列，不存在则添加
ALTER TABLE contract_reminder ADD COLUMN IF NOT EXISTS is_read BOOLEAN DEFAULT FALSE;

-- 检查 contract_comment 表是否存在，不存在则创建
CREATE TABLE IF NOT EXISTS contract_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    parent_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引（如果不存在）
CREATE INDEX IF NOT EXISTS idx_comment_contract ON contract_comment(contract_id);
CREATE INDEX IF NOT EXISTS idx_comment_user ON contract_comment(user_id);
CREATE INDEX IF NOT EXISTS idx_comment_parent ON contract_comment(parent_id);

-- 创建合同动态字段配置表（如果不存在）
CREATE TABLE IF NOT EXISTS contract_type_field (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_type VARCHAR(50) NOT NULL,
    field_key VARCHAR(50) NOT NULL,
    field_label VARCHAR(100) NOT NULL,
    field_label_en VARCHAR(100),
    field_type VARCHAR(20) NOT NULL DEFAULT 'text',
    quick_code_id VARCHAR(50),
    required BOOLEAN DEFAULT FALSE,
    show_in_list BOOLEAN DEFAULT FALSE,
    show_in_form BOOLEAN DEFAULT TRUE,
    field_order INT DEFAULT 0,
    placeholder VARCHAR(200),
    placeholder_en VARCHAR(200),
    default_value VARCHAR(200),
    options TEXT,
    min_value DECIMAL(15,2),
    max_value DECIMAL(15,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_type_field_type ON contract_type_field(contract_type);
CREATE UNIQUE INDEX IF NOT EXISTS idx_type_field_key ON contract_type_field(contract_type, field_key);

-- 创建合同动态字段值表（如果不存在）
CREATE TABLE IF NOT EXISTS contract_field_value (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    field_key VARCHAR(50) NOT NULL,
    field_value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cfv_contract ON contract_field_value(contract_id);
CREATE INDEX IF NOT EXISTS idx_cfv_field ON contract_field_value(contract_id, field_key);

-- 初始化合同类型字段配置
INSERT INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) VALUES
('PURCHASE', 'unit_price', '单价', 'Unit Price', 'currency', true, false, true, 0),
('PURCHASE', 'quantity', '数量', 'Quantity', 'number', true, false, true, 1),
('PURCHASE', 'delivery_date', '交货日期', 'Delivery Date', 'date', false, false, true, 2),
('PURCHASE', 'payment_days', '付款天数', 'Payment Days', 'number', false, false, true, 3),
('PURCHASE', 'supplier_name', '供应商名称', 'Supplier Name', 'text', true, true, true, 4),
('PURCHASE', 'contract_no', '采购合同号', 'PO Number', 'text', false, true, true, 5),
('SALES', 'unit_price', '单价', 'Unit Price', 'currency', true, false, true, 0),
('SALES', 'quantity', '数量', 'Quantity', 'number', true, false, true, 1),
('SALES', 'sales_person', '销售人员', 'Sales Person', 'text', true, true, true, 2),
('SALES', 'customer_name', '客户名称', 'Customer Name', 'text', true, true, true, 3),
('SERVICE', 'service_provider', '服务提供商', 'Service Provider', 'text', true, true, true, 0),
('SERVICE', 'service_level', '服务等级', 'Service Level', 'select', false, false, true, 1),
('SERVICE', 'response_time', '响应时间(小时)', 'Response Time (hours)', 'number', false, false, true, 2),
('LEASE', 'property_address', '物业地址', 'Property Address', 'text', true, true, true, 0),
('LEASE', 'monthly_rent', '月租金', 'Monthly Rent', 'currency', true, true, true, 1),
('LEASE', 'deposit', '押金', 'Deposit', 'currency', false, false, true, 2),
('EMPLOYMENT', 'employee_name', '员工姓名', 'Employee Name', 'text', true, true, true, 0),
('EMPLOYMENT', 'department', '部门', 'Department', 'text', false, true, true, 1),
('EMPLOYMENT', 'position', '职位', 'Position', 'text', true, true, true, 2),
('EMPLOYMENT', 'salary', '月薪', 'Monthly Salary', 'currency', true, true, true, 3)
ON DUPLICATE KEY UPDATE
field_label = VALUES(field_label),
field_label_en = VALUES(field_label_en),
field_type = VALUES(field_type),
required = VALUES(required),
show_in_list = VALUES(show_in_list),
show_in_form = VALUES(show_in_form),
field_order = VALUES(field_order);

-- 检查 contract_tag_relation 表是否存在，不存在则创建
CREATE TABLE IF NOT EXISTS contract_tag_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_contract_tag ON contract_tag_relation(contract_id, tag_id);

-- 检查 approval_record 表是否存在，不存在则创建
CREATE TABLE IF NOT EXISTS approval_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    approver_id BIGINT NOT NULL,
    approver_name VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    comment TEXT,
    create_time TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_approval_contract ON approval_record(contract_id);
CREATE INDEX IF NOT EXISTS idx_approval_approver ON approval_record(approver_id);
CREATE INDEX IF NOT EXISTS idx_approval_create ON approval_record(create_time);

-- 用户会话表索引（会话校验/超时判断的关键路径）
CREATE INDEX IF NOT EXISTS idx_user_session_user_id ON user_session(user_id);
CREATE INDEX IF NOT EXISTS idx_user_session_last_active ON user_session(last_active_time);
CREATE INDEX IF NOT EXISTS idx_user_session_token ON user_session(token(191));

-- reminder_rule 字段兼容（历史字段 enabled/remind_type 与当前业务字段并存）
ALTER TABLE reminder_rule ADD COLUMN IF NOT EXISTS contract_type VARCHAR(255);
ALTER TABLE reminder_rule ADD COLUMN IF NOT EXISTS min_amount DECIMAL(18,2);
ALTER TABLE reminder_rule ADD COLUMN IF NOT EXISTS max_amount DECIMAL(18,2);
ALTER TABLE reminder_rule ADD COLUMN IF NOT EXISTS is_enabled BOOLEAN DEFAULT TRUE;
ALTER TABLE reminder_rule ADD COLUMN IF NOT EXISTS creator_id BIGINT;
ALTER TABLE reminder_rule ADD COLUMN IF NOT EXISTS is_public BOOLEAN DEFAULT TRUE;

UPDATE reminder_rule SET is_enabled = enabled WHERE is_enabled IS NULL;

CREATE INDEX IF NOT EXISTS idx_reminder_rule_enabled ON reminder_rule(is_enabled);
CREATE INDEX IF NOT EXISTS idx_reminder_rule_creator ON reminder_rule(creator_id);
CREATE INDEX IF NOT EXISTS idx_reminder_rule_contract_type ON reminder_rule(contract_type(50));

-- contract 字段兼容（实体已使用这些字段，旧库缺失会触发 500）
ALTER TABLE contract ADD COLUMN IF NOT EXISTS template_id BIGINT;
ALTER TABLE contract ADD COLUMN IF NOT EXISTS content_mode VARCHAR(32);
ALTER TABLE contract ADD COLUMN IF NOT EXISTS starred BOOLEAN DEFAULT FALSE;
ALTER TABLE contract ADD COLUMN IF NOT EXISTS timezone VARCHAR(64);
ALTER TABLE contract ADD COLUMN IF NOT EXISTS currency VARCHAR(10) DEFAULT 'CNY';
ALTER TABLE contract ADD COLUMN IF NOT EXISTS parent_contract_id BIGINT;
ALTER TABLE contract ADD COLUMN IF NOT EXISTS relation_type VARCHAR(20) DEFAULT 'MAIN';
ALTER TABLE contract ADD COLUMN IF NOT EXISTS submitted_at TIMESTAMP NULL;
ALTER TABLE contract ADD COLUMN IF NOT EXISTS current_approver_name VARCHAR(100);

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
CREATE INDEX IF NOT EXISTS idx_contract_snapshot_contract_time ON contract_snapshot(contract_id, created_at);
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
CREATE INDEX IF NOT EXISTS idx_snapshot_diff_contract_snapshots ON contract_snapshot_diff_analysis(contract_id, base_snapshot_id, target_snapshot_id);
CREATE INDEX IF NOT EXISTS idx_snapshot_diff_updated_at ON contract_snapshot_diff_analysis(updated_at);
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
CREATE INDEX IF NOT EXISTS idx_contract_ai_summary_contract_updated ON contract_ai_summary(contract_id, updated_at);
CREATE INDEX IF NOT EXISTS idx_contract_ai_summary_snapshot ON contract_ai_summary(snapshot_id);
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
CREATE UNIQUE INDEX IF NOT EXISTS uk_contract_payload_contract_id ON contract_payload(contract_id);
CREATE INDEX IF NOT EXISTS idx_contract_payload_updated_at ON contract_payload(updated_at);

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

ALTER TABLE contract MODIFY COLUMN counterparty VARCHAR(255) NULL;

-- contract_category 字段兼容（前台分类接口依赖）
ALTER TABLE contract_category ADD COLUMN IF NOT EXISTS name_en VARCHAR(100);
ALTER TABLE contract_category ADD COLUMN IF NOT EXISTS icon VARCHAR(100);
ALTER TABLE contract_category ADD COLUMN IF NOT EXISTS color VARCHAR(50);
ALTER TABLE contract_category ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE;
ALTER TABLE contract_category ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- contract_folder 字段兼容（文件夹管理接口依赖）
ALTER TABLE contract_folder ADD COLUMN IF NOT EXISTS description VARCHAR(255);
ALTER TABLE contract_folder ADD COLUMN IF NOT EXISTS color VARCHAR(50);
ALTER TABLE contract_folder ADD COLUMN IF NOT EXISTS created_by BIGINT;
ALTER TABLE contract_folder ADD COLUMN IF NOT EXISTS created_by_name VARCHAR(100);

-- contract_renewal 字段兼容（续约管理接口依赖）
ALTER TABLE contract_renewal ADD COLUMN IF NOT EXISTS old_end_date DATE;
ALTER TABLE contract_renewal ADD COLUMN IF NOT EXISTS renewal_type VARCHAR(50);
ALTER TABLE contract_renewal ADD COLUMN IF NOT EXISTS status VARCHAR(20);
ALTER TABLE contract_renewal ADD COLUMN IF NOT EXISTS remark TEXT;

-- contract_template 字段兼容（模板管理接口依赖）
ALTER TABLE contract_template ADD COLUMN IF NOT EXISTS description VARCHAR(255);

-- contract_version 字段兼容（合同创建后版本记录依赖）
ALTER TABLE contract_version ADD COLUMN IF NOT EXISTS attachments TEXT;

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
CREATE INDEX IF NOT EXISTS idx_contract_diff_contract_versions ON contract_version_diff_analysis(contract_id, base_version_id, target_version_id);
CREATE INDEX IF NOT EXISTS idx_contract_diff_updated_at ON contract_version_diff_analysis(updated_at);

-- contract_change_log 字段兼容（合同变更日志实体依赖）
ALTER TABLE contract_change_log ADD COLUMN IF NOT EXISTS field_name VARCHAR(100);
ALTER TABLE contract_change_log ADD COLUMN IF NOT EXISTS old_value TEXT;
ALTER TABLE contract_change_log ADD COLUMN IF NOT EXISTS new_value TEXT;
ALTER TABLE contract_change_log ADD COLUMN IF NOT EXISTS diff_json TEXT;
ALTER TABLE contract_change_log ADD COLUMN IF NOT EXISTS remark TEXT;

-- contract_tag 字段兼容（标签管理接口依赖）
ALTER TABLE contract_tag ADD COLUMN IF NOT EXISTS is_public BOOLEAN DEFAULT FALSE;

-- contract_attachment 字段兼容（合同提交时批量附件保存依赖）
ALTER TABLE contract_attachment ADD COLUMN IF NOT EXISTS description VARCHAR(255);
ALTER TABLE contract_attachment ADD COLUMN IF NOT EXISTS file_category VARCHAR(20) DEFAULT 'support';
ALTER TABLE contract_attachment MODIFY COLUMN file_type VARCHAR(255);

-- contract_counterparty 字段兼容（创建/更新合同时同步落独立表依赖）
ALTER TABLE contract_counterparty ADD COLUMN IF NOT EXISTS contract_id BIGINT;
ALTER TABLE contract_counterparty ADD COLUMN IF NOT EXISTS type VARCHAR(50) DEFAULT 'partyA';
ALTER TABLE contract_counterparty ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(20);
ALTER TABLE contract_counterparty ADD COLUMN IF NOT EXISTS contact_email VARCHAR(100);
ALTER TABLE contract_counterparty ADD COLUMN IF NOT EXISTS sort_order INTEGER DEFAULT 0;
ALTER TABLE contract_counterparty ADD COLUMN IF NOT EXISTS create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE contract_counterparty ADD COLUMN IF NOT EXISTS update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- user 字段兼容（头像支持 data URL/base64，避免长度不足）
ALTER TABLE user MODIFY COLUMN avatar LONGTEXT;

-- quick_code_header 字段兼容（合同类型字段配置/模板变量管理依赖）
ALTER TABLE quick_code_header ADD COLUMN IF NOT EXISTS name_en VARCHAR(100);
ALTER TABLE quick_code_header ADD COLUMN IF NOT EXISTS description_en VARCHAR(255);
ALTER TABLE quick_code_header ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- quick_code_item 字段兼容（快速代码明细读写依赖）
ALTER TABLE quick_code_item ADD COLUMN IF NOT EXISTS meaning VARCHAR(100);
ALTER TABLE quick_code_item ADD COLUMN IF NOT EXISTS meaning_en VARCHAR(100);
ALTER TABLE quick_code_item ADD COLUMN IF NOT EXISTS description VARCHAR(255);
ALTER TABLE quick_code_item ADD COLUMN IF NOT EXISTS description_en VARCHAR(255);
ALTER TABLE quick_code_item ADD COLUMN IF NOT EXISTS tag VARCHAR(100);
ALTER TABLE quick_code_item ADD COLUMN IF NOT EXISTS valid_from DATE;
ALTER TABLE quick_code_item ADD COLUMN IF NOT EXISTS valid_to DATE;
ALTER TABLE quick_code_item ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT TRUE;
ALTER TABLE quick_code_item ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- contract_type_field 字段兼容（字段配置/排序接口依赖）
ALTER TABLE contract_type_field ADD COLUMN IF NOT EXISTS quick_code_id VARCHAR(50);

-- 初始化数据去重约束（保证 INSERT IGNORE 幂等）
ALTER TABLE contract_type_field ADD UNIQUE INDEX uk_contract_type_field_type_key (contract_type, field_key);
ALTER TABLE role_permission ADD UNIQUE INDEX uk_role_permission_pair (role_id, permission_id);
ALTER TABLE quick_code_item ADD UNIQUE INDEX uk_quick_code_item_header_code (header_id, code);

SELECT 'Database migration completed successfully!' AS status;
