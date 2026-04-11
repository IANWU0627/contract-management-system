-- 创建用户表
CREATE TABLE IF NOT EXISTS "user" (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) NOT NULL,
    status INTEGER DEFAULT 1
);

-- 创建合同表
CREATE TABLE IF NOT EXISTS contract (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_no VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    counterparty VARCHAR(255) NOT NULL,
    counterparties TEXT, -- 存储多个相对方的JSON数据
    amount DECIMAL(18,2),
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) NOT NULL,
    content TEXT,
    attachment VARCHAR(255),
    attachments TEXT, -- 存储附件数组的JSON数据
    remark TEXT,
    creator_id BIGINT,
    create_time TIMESTAMP,
    update_time TIMESTAMP
);

-- 创建合同模板表
CREATE TABLE IF NOT EXISTS contract_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(20) NOT NULL,
    content TEXT,
    variables TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
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
    reminded_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (contract_id) REFERENCES contract(id)
);

-- 创建合同版本表
CREATE TABLE IF NOT EXISTS contract_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    version VARCHAR(20) NOT NULL,
    content TEXT,
    change_desc TEXT,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    created_at TIMESTAMP,
    FOREIGN KEY (contract_id) REFERENCES contract(id)
);

-- 创建审批记录表
CREATE TABLE IF NOT EXISTS approval_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    approver_id BIGINT NOT NULL,
    approver_name VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    comment TEXT,
    create_time TIMESTAMP,
    FOREIGN KEY (contract_id) REFERENCES contract(id)
);

-- 创建操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    module VARCHAR(50) NOT NULL,
    content TEXT,
    ip VARCHAR(50),
    create_time TIMESTAMP
);
