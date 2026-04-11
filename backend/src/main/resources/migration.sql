-- =====================================================
-- Contract Management System - Migration Script
-- Version: 1.0
-- Date: 2026-03-31
-- Description: Migration for new features:
--   - Contract type field configuration
--   - Contract change log
--   - Hybrid privacy for tags and reminder rules
-- =====================================================

-- =====================================================
-- STEP 1: Add creator_id and is_public to contract_tag
-- =====================================================
ALTER TABLE contract_tag ADD COLUMN IF NOT EXISTS creator_id BIGINT;
ALTER TABLE contract_tag ADD COLUMN IF NOT EXISTS is_public BOOLEAN DEFAULT TRUE;
CREATE INDEX IF NOT EXISTS idx_tag_creator ON contract_tag(creator_id);
CREATE INDEX IF NOT EXISTS idx_tag_public ON contract_tag(is_public);

-- =====================================================
-- STEP 2: Add creator_id and is_public to reminder_rule
-- =====================================================
ALTER TABLE reminder_rule ADD COLUMN IF NOT EXISTS creator_id BIGINT;
ALTER TABLE reminder_rule ADD COLUMN IF NOT EXISTS is_public BOOLEAN DEFAULT TRUE;
CREATE INDEX IF NOT EXISTS idx_rule_creator ON reminder_rule(creator_id);
CREATE INDEX IF NOT EXISTS idx_rule_public ON reminder_rule(is_public);

-- Enlarge contract_type column to support multiple types
ALTER TABLE reminder_rule ALTER COLUMN contract_type VARCHAR(200);

-- =====================================================
-- STEP 3: Create contract_change_log table
-- =====================================================
CREATE TABLE IF NOT EXISTS contract_change_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    change_type VARCHAR(50) NOT NULL,
    field_name VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    remark TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_change_contract ON contract_change_log(contract_id);
CREATE INDEX IF NOT EXISTS idx_change_operator ON contract_change_log(operator_id);
CREATE INDEX IF NOT EXISTS idx_change_type ON contract_change_log(change_type);
CREATE INDEX IF NOT EXISTS idx_change_created ON contract_change_log(created_at);

-- =====================================================
-- STEP 4: Create contract_type_field table
-- =====================================================
CREATE TABLE IF NOT EXISTS contract_type_field (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_type VARCHAR(50) NOT NULL,
    field_key VARCHAR(50) NOT NULL,
    field_label VARCHAR(100),
    field_label_en VARCHAR(100),
    field_type VARCHAR(20) NOT NULL DEFAULT 'text',
    required BOOLEAN DEFAULT FALSE,
    show_in_list BOOLEAN DEFAULT TRUE,
    show_in_form BOOLEAN DEFAULT TRUE,
    field_order INT DEFAULT 0,
    placeholder VARCHAR(200),
    placeholder_en VARCHAR(200),
    default_value VARCHAR(200),
    options TEXT,
    min_value DECIMAL(18,2),
    max_value DECIMAL(18,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_type_field_type ON contract_type_field(contract_type);
CREATE UNIQUE INDEX IF NOT EXISTS idx_type_field_key ON contract_type_field(contract_type, field_key);

-- =====================================================
-- STEP 5: Initialize default field configurations
-- =====================================================

-- Common fields (applicable to all contract types)
INSERT INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) VALUES
('COMMON', 'contact_person', '联系人', 'Contact Person', 'text', false, true, true, 1),
('COMMON', 'contact_phone', '联系电话', 'Contact Phone', 'text', false, true, true, 2),
('COMMON', 'contact_email', '联系邮箱', 'Contact Email', 'text', false, true, true, 3),
('COMMON', 'bank_name', '开户银行', 'Bank Name', 'text', false, true, true, 4),
('COMMON', 'bank_account', '银行账号', 'Bank Account', 'text', false, true, true, 5),
('COMMON', 'tax_rate', '税率(%)', 'Tax Rate (%)', 'number', false, true, true, 6),
('COMMON', 'tax_included', '是否含税', 'Tax Included', 'select', false, true, true, 7),
('COMMON', 'invoice_type', '发票类型', 'Invoice Type', 'select', false, true, true, 8),
('COMMON', 'signing_location', '签约地点', 'Signing Location', 'text', false, true, true, 9),
('COMMON', 'effective_date', '生效日期', 'Effective Date', 'date', false, true, true, 10),
('COMMON', 'termination_clause', '终止条款', 'Termination Clause', 'textarea', false, false, true, 11),
('COMMON', 'penalty_clause', '违约条款', 'Penalty Clause', 'textarea', false, false, true, 12),
('COMMON', 'confidentiality', '保密条款', 'Confidentiality', 'select', false, true, true, 13),
('COMMON', 'exclusive_clause', '独家条款', 'Exclusive Clause', 'select', false, true, true, 14)
ON DUPLICATE KEY UPDATE field_label = VALUES(field_label);

-- Purchase contract fields
INSERT INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) VALUES
('PURCHASE', 'supplier', '供应商', 'Supplier', 'text', true, true, true, 1),
('PURCHASE', 'supplier_contact', '供应商联系人', 'Supplier Contact', 'text', false, true, true, 2),
('PURCHASE', 'supplier_phone', '供应商电话', 'Supplier Phone', 'text', false, true, true, 3),
('PURCHASE', 'delivery_date', '交货日期', 'Delivery Date', 'date', false, true, true, 4),
('PURCHASE', 'delivery_address', '交货地址', 'Delivery Address', 'text', false, true, true, 5),
('PURCHASE', 'warranty_period', '保修期', 'Warranty Period', 'text', false, true, true, 6),
('PURCHASE', 'quality_standard', '质量标准', 'Quality Standard', 'text', false, true, true, 7),
('PURCHASE', 'quantity', '数量', 'Quantity', 'number', true, true, true, 8),
('PURCHASE', 'unit_price', '单价', 'Unit Price', 'currency', true, true, true, 9),
('PURCHASE', 'discount', '折扣率(%)', 'Discount (%)', 'number', false, true, true, 10),
('PURCHASE', 'payment_terms', '付款条款', 'Payment Terms', 'select', false, true, true, 11),
('PURCHASE', 'transport_fee', '运输费用', 'Transport Fee', 'currency', false, true, true, 12)
ON DUPLICATE KEY UPDATE field_label = VALUES(field_label);

-- Sales contract fields
INSERT INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) VALUES
('SALES', 'customer_name', '客户名称', 'Customer Name', 'text', true, true, true, 1),
('SALES', 'customer_contact', '客户联系人', 'Customer Contact', 'text', false, true, true, 2),
('SALES', 'customer_phone', '客户电话', 'Customer Phone', 'text', false, true, true, 3),
('SALES', 'shipping_date', '发货日期', 'Shipping Date', 'date', false, true, true, 4),
('SALES', 'sales_region', '销售区域', 'Sales Region', 'text', false, true, true, 5),
('SALES', 'channel', '销售渠道', 'Sales Channel', 'select', false, true, true, 6)
ON DUPLICATE KEY UPDATE field_label = VALUES(field_label);

-- Lease contract fields
INSERT INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) VALUES
('LEASE', 'property_address', '物业地址', 'Property Address', 'text', true, true, true, 1),
('LEASE', 'property_area', '面积(㎡)', 'Area (㎡)', 'number', true, true, true, 2),
('LEASE', 'monthly_rent', '月租金', 'Monthly Rent', 'currency', true, true, true, 3),
('LEASE', 'quarterly_rent', '季度租金', 'Quarterly Rent', 'currency', false, true, true, 4),
('LEASE', 'annual_rent', '年租金', 'Annual Rent', 'currency', false, true, true, 5),
('LEASE', 'deposit_amount', '押金', 'Deposit', 'currency', true, true, true, 6),
('LEASE', 'payment_method', '付款方式', 'Payment Method', 'select', false, true, true, 7),
('LEASE', 'property_type', '物业类型', 'Property Type', 'select', false, true, true, 8),
('LEASE', 'renovation_allowance', '装修免租期', 'Decoration Allowance', 'text', false, true, true, 9),
('LEASE', 'agent_fee', '中介费', 'Agent Fee', 'currency', false, true, true, 10)
ON DUPLICATE KEY UPDATE field_label = VALUES(field_label);

-- Employment contract fields
INSERT INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) VALUES
('EMPLOYMENT', 'position', '岗位', 'Position', 'text', true, true, true, 1),
('EMPLOYMENT', 'department', '部门', 'Department', 'text', false, true, true, 2),
('EMPLOYMENT', 'base_salary', '基本工资', 'Base Salary', 'currency', true, true, true, 3),
('EMPLOYMENT', 'monthly_salary', '月薪', 'Monthly Salary', 'currency', false, true, true, 4),
('EMPLOYMENT', 'annual_salary', '年薪', 'Annual Salary', 'currency', false, true, true, 5),
('EMPLOYMENT', 'trial_salary', '试用期工资', 'Trial Salary', 'currency', false, true, true, 6),
('EMPLOYMENT', 'trial_period', '试用期(月)', 'Trial Period (Months)', 'number', false, true, true, 7),
('EMPLOYMENT', 'work_location', '工作地点', 'Work Location', 'text', true, true, true, 8),
('EMPLOYMENT', 'work_hours', '工作时间', 'Work Hours', 'text', false, true, true, 9),
('EMPLOYMENT', 'social_benefits', '社保缴纳', 'Social Benefits', 'select', false, true, true, 10),
('EMPLOYMENT', 'probation_period', '转正考核期', 'Probation Period', 'text', false, true, true, 11),
('EMPLOYMENT', 'contract_period', '合同期限(年)', 'Contract Period (Years)', 'number', false, true, true, 12)
ON DUPLICATE KEY UPDATE field_label = VALUES(field_label);

-- Service contract fields
INSERT INTO contract_type_field (contract_type, field_key, field_label, field_label_en, field_type, required, show_in_list, show_in_form, field_order) VALUES
('SERVICE', 'service_content', '服务内容', 'Service Content', 'textarea', true, true, true, 1),
('SERVICE', 'service_fee', '服务费', 'Service Fee', 'currency', true, true, true, 2),
('SERVICE', 'service_start', '服务开始日期', 'Service Start Date', 'date', false, true, true, 3),
('SERVICE', 'service_end', '服务结束日期', 'Service End Date', 'date', false, true, true, 4),
('SERVICE', 'service_frequency', '服务频率', 'Service Frequency', 'select', false, true, true, 5),
('SERVICE', 'service_scope', '服务范围', 'Service Scope', 'textarea', false, false, true, 6),
('SERVICE', 'service_level', '服务水平要求', 'Service Level', 'select', false, true, true, 7),
('SERVICE', 'response_time', '响应时间', 'Response Time', 'text', false, true, true, 8)
ON DUPLICATE KEY UPDATE field_label = VALUES(field_label);

-- =====================================================
-- STEP 6: Create migration tracking table
-- =====================================================
CREATE TABLE IF NOT EXISTS schema_migrations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version VARCHAR(50) NOT NULL UNIQUE,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT
);

-- Mark this migration as applied
INSERT INTO schema_migrations (version, description) VALUES ('1.0', 'Initial migration for new features')
ON DUPLICATE KEY UPDATE applied_at = CURRENT_TIMESTAMP;

-- =====================================================
-- Verification queries (run these to verify migration)
-- =====================================================
-- SELECT 'contract_tag' as table_name, COUNT(*) as columns FROM contract_tag LIMIT 1;
-- SELECT 'reminder_rule' as table_name, COUNT(*) as columns FROM reminder_rule LIMIT 1;
-- SELECT COUNT(*) as change_log_count FROM contract_change_log;
-- SELECT contract_type, COUNT(*) as field_count FROM contract_type_field GROUP BY contract_type;

-- =====================================================
-- STEP 7: Add templateId and contentMode to contract table
-- =====================================================
ALTER TABLE contract ADD COLUMN IF NOT EXISTS template_id BIGINT;
ALTER TABLE contract ADD COLUMN IF NOT EXISTS content_mode VARCHAR(20) DEFAULT 'template';
CREATE INDEX IF NOT EXISTS idx_contract_template ON contract(template_id);

-- =====================================================
-- STEP 8: Add name_en to template_variable table
-- =====================================================
ALTER TABLE template_variable ADD COLUMN IF NOT EXISTS name_en VARCHAR(100);

-- Mark migration v1.1 as applied
INSERT INTO schema_migrations (version, description) VALUES ('1.1', 'Add template tracking and English names for template variables')
ON DUPLICATE KEY UPDATE applied_at = CURRENT_TIMESTAMP;
