-- 完整的数据库初始化脚本
-- 适用于H2数据库（MODE=MySQL）

-- 创建用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar LONGTEXT,
    role VARCHAR(20) NOT NULL,
    status INTEGER DEFAULT 1
);

-- 创建角色表
CREATE TABLE IF NOT EXISTS role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    status INTEGER DEFAULT 1
);

-- 创建权限表
CREATE TABLE IF NOT EXISTS permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    path VARCHAR(255),
    method VARCHAR(20),
    description VARCHAR(255),
    status INTEGER DEFAULT 1
);

-- 创建角色权限关联表
CREATE TABLE IF NOT EXISTS role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_permission_pair (role_id, permission_id)
);

-- 创建合同表
CREATE TABLE IF NOT EXISTS contract (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_no VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    counterparty VARCHAR(255) NOT NULL,
    counterparties TEXT,
    amount DECIMAL(18,2),
    currency VARCHAR(10) DEFAULT 'CNY',
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) NOT NULL,
    content TEXT,
    template_variables TEXT,
    dynamic_field_values TEXT,
    template_id BIGINT,
    content_mode VARCHAR(32),
    attachment VARCHAR(255),
    attachments TEXT,
    remark TEXT,
    creator_id BIGINT,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    folder_id BIGINT,
    starred BOOLEAN DEFAULT FALSE,
    timezone VARCHAR(64),
    deleted INTEGER DEFAULT 0
);

-- 创建合同附件表
CREATE TABLE IF NOT EXISTS contract_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT,
    file_type VARCHAR(255),
    description VARCHAR(255),
    file_category VARCHAR(20) DEFAULT 'support',
    uploader_id BIGINT,
    uploader_name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建合同分类表
CREATE TABLE IF NOT EXISTS contract_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    name_en VARCHAR(100),
    code VARCHAR(50) NOT NULL UNIQUE,
    icon VARCHAR(100),
    color VARCHAR(50),
    description VARCHAR(255),
    sort_order INTEGER DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    status INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建合同变更日志表
CREATE TABLE IF NOT EXISTS contract_change_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    version VARCHAR(20),
    change_type VARCHAR(50),
    change_content TEXT,
    field_name VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    remark TEXT,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建合同评论表
CREATE TABLE IF NOT EXISTS contract_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    parent_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建合同相对方表
CREATE TABLE IF NOT EXISTS contract_counterparty (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    type VARCHAR(50) DEFAULT 'partyA',
    name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(100),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(255),
    sort_order INTEGER DEFAULT 0,
    description TEXT,
    status INTEGER DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建合同动态字段值表
CREATE TABLE IF NOT EXISTS contract_field_value (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    field_key VARCHAR(50) NOT NULL,
    field_value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建合同文件夹表
CREATE TABLE IF NOT EXISTS contract_folder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT,
    description VARCHAR(255),
    color VARCHAR(50),
    created_by BIGINT,
    created_by_name VARCHAR(100),
    creator_id BIGINT,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建合同收藏表
CREATE TABLE IF NOT EXISTS contract_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建合同提醒表
CREATE TABLE IF NOT EXISTS contract_reminder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT,
    contract_no VARCHAR(50),
    contract_title VARCHAR(255),
    expire_date TIMESTAMP,
    remind_days INTEGER,
    reminder_type INTEGER,
    status INTEGER DEFAULT 0,
    is_read BOOLEAN DEFAULT FALSE,
    reminded_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- 创建合同续约表
CREATE TABLE IF NOT EXISTS contract_renewal (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    original_end_date DATE,
    old_end_date DATE,
    new_end_date DATE,
    renewal_reason TEXT,
    renewal_type VARCHAR(50),
    status VARCHAR(20),
    remark TEXT,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建合同标签表
CREATE TABLE IF NOT EXISTS contract_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(20),
    description VARCHAR(255) DEFAULT '',
    creator_id BIGINT,
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建合同标签关联表
CREATE TABLE IF NOT EXISTS contract_tag_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建合同模板表
CREATE TABLE IF NOT EXISTS contract_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(20) NOT NULL,
    description VARCHAR(255),
    content TEXT,
    variables TEXT,
    usage_count BIGINT DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- 创建合同类型字段配置表
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

-- 创建合同版本表
CREATE TABLE IF NOT EXISTS contract_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    version VARCHAR(20) NOT NULL,
    content TEXT,
    attachments TEXT,
    change_desc TEXT,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    created_at TIMESTAMP
);

-- 创建审批记录表
CREATE TABLE IF NOT EXISTS approval_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    approver_id BIGINT NOT NULL,
    approver_name VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    comment TEXT,
    create_time TIMESTAMP
);

-- 创建操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    module VARCHAR(50) NOT NULL,
    operation VARCHAR(100) NOT NULL,
    target_id BIGINT,
    description TEXT,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    ip VARCHAR(50),
    detail TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT,
    config_type VARCHAR(20) DEFAULT 'string',
    description VARCHAR(255),
    user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_config_user (config_key, user_id)
);

-- 创建用户会话表
CREATE TABLE IF NOT EXISTS user_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    token VARCHAR(500),
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    device VARCHAR(255),
    location VARCHAR(255),
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_active_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_user_session_user_id ON user_session(user_id);
CREATE INDEX IF NOT EXISTS idx_user_session_last_active ON user_session(last_active_time);
CREATE INDEX IF NOT EXISTS idx_user_session_token ON user_session(token(191));

-- 创建提醒规则表
CREATE TABLE IF NOT EXISTS reminder_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contract_type VARCHAR(255),
    remind_type VARCHAR(50),
    min_amount DECIMAL(18,2),
    max_amount DECIMAL(18,2),
    remind_days INTEGER NOT NULL,
    is_enabled BOOLEAN DEFAULT TRUE,
    enabled BOOLEAN DEFAULT TRUE,
    creator_id BIGINT,
    is_public BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建快速代码表头表
CREATE TABLE IF NOT EXISTS quick_code_header (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    description VARCHAR(255),
    description_en VARCHAR(255),
    status INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建快速代码项表
CREATE TABLE IF NOT EXISTS quick_code_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    header_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    meaning VARCHAR(100) NOT NULL,
    meaning_en VARCHAR(100),
    description VARCHAR(255),
    description_en VARCHAR(255),
    tag VARCHAR(100),
    valid_from DATE,
    valid_to DATE,
    enabled BOOLEAN DEFAULT TRUE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_quick_code_item_header_code (header_id, code)
);

-- 创建模板变量表
CREATE TABLE IF NOT EXISTS template_variable (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    label VARCHAR(100),
    type VARCHAR(20) NOT NULL,
    quick_code_code VARCHAR(50),
    category VARCHAR(50),
    default_value VARCHAR(255),
    description VARCHAR(255),
    required INTEGER DEFAULT 0,
    sort_order INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_comment_contract ON contract_comment(contract_id);
CREATE INDEX IF NOT EXISTS idx_comment_user ON contract_comment(user_id);
CREATE INDEX IF NOT EXISTS idx_comment_parent ON contract_comment(parent_id);
CREATE INDEX IF NOT EXISTS idx_type_field_type ON contract_type_field(contract_type);
CREATE UNIQUE INDEX IF NOT EXISTS idx_type_field_key ON contract_type_field(contract_type, field_key);
CREATE INDEX IF NOT EXISTS idx_cfv_contract ON contract_field_value(contract_id);
CREATE INDEX IF NOT EXISTS idx_cfv_field ON contract_field_value(contract_id, field_key);
