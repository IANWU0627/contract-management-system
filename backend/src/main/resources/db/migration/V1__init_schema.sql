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
MERGE INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) KEY(contract_type, field_key) VALUES ('PURCHASE', 'unit_price', '单价', 'Unit Price', 'currency', true, false, true, 0);
MERGE INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) KEY(contract_type, field_key) VALUES ('PURCHASE', 'quantity', '数量', 'Quantity', 'number', true, false, true, 1);
MERGE INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) KEY(contract_type, field_key) VALUES ('PURCHASE', 'delivery_date', '交货日期', 'Delivery Date', 'date', false, false, true, 2);
MERGE INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) KEY(contract_type, field_key) VALUES ('PURCHASE', 'payment_days', '付款天数', 'Payment Days', 'number', false, false, true, 3);
MERGE INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) KEY(contract_type, field_key) VALUES ('PURCHASE', 'supplier_name', '供应商名称', 'Supplier Name', 'text', true, true, true, 4);
MERGE INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) KEY(contract_type, field_key) VALUES ('PURCHASE', 'contract_no', '采购合同号', 'PO Number', 'text', false, true, true, 5);

MERGE INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) KEY(contract_type, field_key) VALUES ('SALES', 'unit_price', '单价', 'Unit Price', 'currency', true, false, true, 0);
MERGE INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) KEY(contract_type, field_key) VALUES ('SALES', 'quantity', '数量', 'Quantity', 'number', true, false, true, 1);
MERGE INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) KEY(contract_type, field_key) VALUES ('SALES', 'sales_person', '销售人员', 'Sales Person', 'text', true, true, true, 2);
MERGE INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) KEY(contract_type, field_key) VALUES ('SALES', 'customer_name', '客户名称', 'Customer Name', 'text', true, true, true, 3);

MERGE INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) KEY(contract_type, field_key) VALUES ('SERVICE', 'service_provider', '服务提供商', 'Service Provider', 'text', true, true, true, 0);
MERGE INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) KEY(contract_type, field_key) VALUES ('SERVICE', 'service_level', '服务等级', 'Service Level', 'select', false, false, true, 1);
MERGE INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) KEY(contract_type, field_key) VALUES ('SERVICE', 'response_time', '响应时间(小时)', 'Response Time (hours)', 'number', false, false, true, 2);

MERGE INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) KEY(contract_type, field_key) VALUES ('LEASE', 'property_address', '物业地址', 'Property Address', 'text', true, true, true, 0);
MERGE INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) KEY(contract_type, field_key) VALUES ('LEASE', 'monthly_rent', '月租金', 'Monthly Rent', 'currency', true, true, true, 1);
MERGE INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) KEY(contract_type, field_key) VALUES ('LEASE', 'deposit', '押金', 'Deposit', 'currency', false, false, true, 2);

MERGE INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) KEY(contract_type, field_key) VALUES ('EMPLOYMENT', 'employee_name', '员工姓名', 'Employee Name', 'text', true, true, true, 0);
MERGE INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) KEY(contract_type, field_key) VALUES ('EMPLOYMENT', 'department', '部门', 'Department', 'text', false, true, true, 1);
MERGE INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) KEY(contract_type, field_key) VALUES ('EMPLOYMENT', 'position', '职位', 'Position', 'text', true, true, true, 2);
MERGE INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) KEY(contract_type, field_key) VALUES ('EMPLOYMENT', 'salary', '月薪', 'Monthly Salary', 'currency', true, true, true, 3);

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

SELECT 'Database migration completed successfully!' AS status;
