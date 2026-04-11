-- MySQL Schema for Contract Management System
-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `nickname` VARCHAR(50) NOT NULL,
    `email` VARCHAR(100),
    `phone` VARCHAR(20),
    `avatar` MEDIUMTEXT,
    `role` VARCHAR(20) NOT NULL,
    `status` INT DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_role ON `user`(`role`);
CREATE INDEX idx_user_status ON `user`(`status`);

-- 创建合同表
CREATE TABLE IF NOT EXISTS `contract` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `contract_no` VARCHAR(50) NOT NULL UNIQUE,
    `title` VARCHAR(255) NOT NULL,
    `type` VARCHAR(20) NOT NULL,
    `counterparty` VARCHAR(255) NOT NULL,
    `counterparties` TEXT,
    `amount` DECIMAL(18,2),
    `start_date` DATE,
    `end_date` DATE,
    `status` VARCHAR(20) NOT NULL,
    `content` TEXT,
    `attachment` VARCHAR(255),
    `attachments` TEXT,
    `remark` TEXT,
    `creator_id` BIGINT,
    `folder_id` BIGINT,
    `starred` INT DEFAULT 0,
    `timezone` VARCHAR(50),
    `template_variables` TEXT,
    `dynamic_field_values` TEXT,
    `template_id` BIGINT,
    `content_mode` VARCHAR(20) DEFAULT 'template',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_contract_no ON `contract`(`contract_no`);
CREATE INDEX idx_contract_type ON `contract`(`type`);
CREATE INDEX idx_contract_status ON `contract`(`status`);
CREATE INDEX idx_contract_end_date ON `contract`(`end_date`);
CREATE INDEX idx_contract_creator ON `contract`(`creator_id`);
CREATE INDEX idx_contract_create_time ON `contract`(`create_time`);
CREATE INDEX idx_contract_folder ON `contract`(`folder_id`);
CREATE INDEX idx_contract_template ON `contract`(`template_id`);

-- 创建合同相对方表
CREATE TABLE IF NOT EXISTS `contract_counterparty` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `contract_id` BIGINT NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `type` VARCHAR(50),
    `contact_person` VARCHAR(100),
    `contact_phone` VARCHAR(50),
    `contact_email` VARCHAR(100),
    `address` VARCHAR(500),
    `sort_order` INT DEFAULT 0,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_counterparty_contract ON `contract_counterparty`(`contract_id`);

-- 创建合同附件表
CREATE TABLE IF NOT EXISTS `contract_attachment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `contract_id` BIGINT NOT NULL,
    `file_name` VARCHAR(255) NOT NULL,
    `file_url` VARCHAR(500) NOT NULL,
    `file_size` BIGINT,
    `file_type` VARCHAR(100),
    `description` VARCHAR(500),
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_attachment_contract ON `contract_attachment`(`contract_id`);

-- 创建合同文件夹表
CREATE TABLE IF NOT EXISTS `contract_folder` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `parent_id` BIGINT,
    `description` TEXT,
    `color` VARCHAR(20) DEFAULT '#667eea',
    `created_by` BIGINT,
    `created_by_name` VARCHAR(50),
    `sort_order` INT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_folder_parent ON `contract_folder`(`parent_id`);
CREATE INDEX idx_folder_creator ON `contract_folder`(`created_by`);

-- 创建合同模板表
CREATE TABLE IF NOT EXISTS `contract_template` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `category` VARCHAR(20) NOT NULL,
    `description` VARCHAR(2000),
    `content` TEXT,
    `variables` TEXT,
    `usage_count` BIGINT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_template_category ON `contract_template`(`category`);

-- 创建合同提醒表
CREATE TABLE IF NOT EXISTS `contract_reminder` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `contract_id` BIGINT,
    `contract_no` VARCHAR(50),
    `contract_title` VARCHAR(255),
    `expire_date` TIMESTAMP,
    `remind_days` INT,
    `reminder_type` INT,
    `status` INT DEFAULT 0,
    `is_read` TINYINT(1) DEFAULT 0,
    `reminded_at` TIMESTAMP,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_reminder_contract ON `contract_reminder`(`contract_id`);
CREATE INDEX idx_reminder_expire ON `contract_reminder`(`expire_date`);
CREATE INDEX idx_reminder_status ON `contract_reminder`(`status`);

-- 创建合同版本表
CREATE TABLE IF NOT EXISTS `contract_version` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `contract_id` BIGINT NOT NULL,
    `version` VARCHAR(20) NOT NULL,
    `content` TEXT,
    `attachments` TEXT,
    `change_desc` TEXT,
    `operator_id` BIGINT,
    `operator_name` VARCHAR(50),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_version_contract ON `contract_version`(`contract_id`);
CREATE INDEX idx_version_created ON `contract_version`(`created_at`);

-- 创建审批记录表
CREATE TABLE IF NOT EXISTS `approval_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `contract_id` BIGINT NOT NULL,
    `approver_id` BIGINT NOT NULL,
    `approver_name` VARCHAR(50) NOT NULL,
    `status` VARCHAR(20) NOT NULL,
    `comment` TEXT,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_approval_contract ON `approval_record`(`contract_id`);
CREATE INDEX idx_approval_approver ON `approval_record`(`approver_id`);
CREATE INDEX idx_approval_create ON `approval_record`(`create_time`);

-- 创建合同动态字段值表
CREATE TABLE IF NOT EXISTS `contract_field_value` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `contract_id` BIGINT NOT NULL,
    `field_key` VARCHAR(50) NOT NULL,
    `field_value` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cfv_contract ON `contract_field_value`(`contract_id`);
CREATE INDEX idx_cfv_field ON `contract_field_value`(`contract_id`, `field_key`);

-- 创建操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `username` VARCHAR(50) NOT NULL,
    `action` VARCHAR(100) NOT NULL,
    `module` VARCHAR(50) NOT NULL,
    `content` TEXT,
    `detail` TEXT,
    `ip` VARCHAR(50),
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_log_user ON `operation_log`(`user_id`);
CREATE INDEX idx_log_action ON `operation_log`(`action`);
CREATE INDEX idx_log_module ON `operation_log`(`module`);
CREATE INDEX idx_log_create ON `operation_log`(`create_time`);

-- 创建合同收藏表
CREATE TABLE IF NOT EXISTS `contract_favorite` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `contract_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_contract_user (`contract_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_favorite_user ON `contract_favorite`(`user_id`);
CREATE INDEX idx_favorite_contract ON `contract_favorite`(`contract_id`);

-- 创建合同标签表
CREATE TABLE IF NOT EXISTS `contract_tag` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `color` VARCHAR(20) DEFAULT '#667eea',
    `description` VARCHAR(255) DEFAULT '',
    `creator_id` BIGINT,
    `is_public` TINYINT(1) DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY idx_tag_name (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_tag_creator ON `contract_tag`(`creator_id`);
CREATE INDEX idx_tag_public ON `contract_tag`(`is_public`);

-- 创建合同标签关联表
CREATE TABLE IF NOT EXISTS `contract_tag_relation` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `contract_id` BIGINT NOT NULL,
    `tag_id` BIGINT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_contract_tag (`contract_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_relation_contract ON `contract_tag_relation`(`contract_id`);
CREATE INDEX idx_relation_tag ON `contract_tag_relation`(`tag_id`);

-- 创建提醒规则表
CREATE TABLE IF NOT EXISTS `reminder_rule` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `contract_type` VARCHAR(200),
    `min_amount` DECIMAL(18,2),
    `max_amount` DECIMAL(18,2),
    `remind_days` INT NOT NULL DEFAULT 30,
    `is_enabled` INT DEFAULT 1,
    `creator_id` BIGINT,
    `is_public` TINYINT(1) DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_rule_type ON `reminder_rule`(`contract_type`);
CREATE INDEX idx_rule_enabled ON `reminder_rule`(`is_enabled`);
CREATE INDEX idx_rule_creator ON `reminder_rule`(`creator_id`);
CREATE INDEX idx_rule_public ON `reminder_rule`(`is_public`);

-- 创建合同续约记录表
CREATE TABLE IF NOT EXISTS `contract_renewal` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `contract_id` BIGINT NOT NULL,
    `old_end_date` DATE,
    `new_end_date` DATE,
    `renewal_type` VARCHAR(20) NOT NULL,
    `status` VARCHAR(20) DEFAULT 'PENDING',
    `remark` TEXT,
    `operator_id` BIGINT,
    `operator_name` VARCHAR(50),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_renewal_contract ON `contract_renewal`(`contract_id`);
CREATE INDEX idx_renewal_status ON `contract_renewal`(`status`);

-- 创建合同评论表
CREATE TABLE IF NOT EXISTS `contract_comment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `contract_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `username` VARCHAR(50) NOT NULL,
    `content` TEXT NOT NULL,
    `parent_id` BIGINT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_comment_contract ON `contract_comment`(`contract_id`);
CREATE INDEX idx_comment_user ON `contract_comment`(`user_id`);
CREATE INDEX idx_comment_parent ON `contract_comment`(`parent_id`);

-- 创建角色表
CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `code` VARCHAR(50) NOT NULL UNIQUE,
    `description` TEXT,
    `status` INT DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_role_code ON `role`(`code`);
CREATE INDEX idx_role_status ON `role`(`status`);

-- 创建权限表
CREATE TABLE IF NOT EXISTS `permission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `code` VARCHAR(50) NOT NULL UNIQUE,
    `path` VARCHAR(255),
    `method` VARCHAR(10),
    `description` TEXT,
    `status` INT DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_permission_code ON `permission`(`code`);
CREATE INDEX idx_permission_status ON `permission`(`status`);

-- 创建角色权限关联表
CREATE TABLE IF NOT EXISTS `role_permission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `role_id` BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_permission (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_role_permission_role ON `role_permission`(`role_id`);
CREATE INDEX idx_role_permission_permission ON `role_permission`(`permission_id`);

-- 创建合同变更记录表
CREATE TABLE IF NOT EXISTS `contract_change_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `contract_id` BIGINT NOT NULL,
    `change_type` VARCHAR(50) NOT NULL,
    `field_name` VARCHAR(100),
    `old_value` TEXT,
    `new_value` TEXT,
    `operator_id` BIGINT,
    `operator_name` VARCHAR(50),
    `remark` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_change_contract ON `contract_change_log`(`contract_id`);
CREATE INDEX idx_change_operator ON `contract_change_log`(`operator_id`);
CREATE INDEX idx_change_type ON `contract_change_log`(`change_type`);
CREATE INDEX idx_change_created ON `contract_change_log`(`created_at`);

-- 创建合同类型字段配置表
CREATE TABLE IF NOT EXISTS `contract_type_field` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `contract_type` VARCHAR(50) NOT NULL,
    `field_key` VARCHAR(50) NOT NULL,
    `field_label` VARCHAR(100),
    `field_label_en` VARCHAR(100),
    `field_type` VARCHAR(20) NOT NULL DEFAULT 'text',
    `quick_code_id` VARCHAR(50),
    `required` TINYINT(1) DEFAULT 0,
    `show_in_list` TINYINT(1) DEFAULT 1,
    `show_in_form` TINYINT(1) DEFAULT 1,
    `field_order` INT DEFAULT 0,
    `placeholder` VARCHAR(200),
    `placeholder_en` VARCHAR(200),
    `default_value` VARCHAR(200),
    `options` TEXT,
    `min_value` DECIMAL(18,2),
    `max_value` DECIMAL(18,2),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY idx_type_field_key (`contract_type`, `field_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_type_field_type ON `contract_type_field`(`contract_type`);

-- 创建模板变量表
CREATE TABLE IF NOT EXISTS `template_variable` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `code` VARCHAR(100) NOT NULL UNIQUE,
    `name` VARCHAR(100) NOT NULL,
    `label` VARCHAR(100),
    `type` VARCHAR(20) NOT NULL DEFAULT 'text',
    `category` VARCHAR(50),
    `default_value` VARCHAR(500),
    `description` VARCHAR(500),
    `required` INT DEFAULT 0,
    `sort_order` INT DEFAULT 0,
    `status` INT DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_variable_category ON `template_variable`(`category`);
CREATE INDEX idx_variable_status ON `template_variable`(`status`);
CREATE INDEX idx_variable_code ON `template_variable`(`code`);

-- 创建合同分类表
CREATE TABLE IF NOT EXISTS `contract_category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `name_en` VARCHAR(100),
    `code` VARCHAR(50) NOT NULL UNIQUE,
    `icon` VARCHAR(50),
    `color` VARCHAR(20) DEFAULT '#409eff',
    `sort_order` INT DEFAULT 0,
    `active` TINYINT(1) DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_contract_category_code ON `contract_category`(`code`);
CREATE INDEX idx_contract_category_active ON `contract_category`(`active`);

-- 创建快速代码头表
CREATE TABLE IF NOT EXISTS `quick_code_header` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `name_en` VARCHAR(100),
    `code` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(500),
    `description_en` VARCHAR(500),
    `status` INT DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_quick_code_header_code ON `quick_code_header`(`code`);
CREATE INDEX idx_quick_code_header_status ON `quick_code_header`(`status`);

-- 创建快速代码行表
CREATE TABLE IF NOT EXISTS `quick_code_item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `header_id` BIGINT NOT NULL,
    `code` VARCHAR(50) NOT NULL,
    `meaning` VARCHAR(100) NOT NULL,
    `meaning_en` VARCHAR(100),
    `description` VARCHAR(500),
    `description_en` VARCHAR(500),
    `tag` VARCHAR(50),
    `valid_from` DATE,
    `valid_to` DATE,
    `enabled` TINYINT(1) DEFAULT 1,
    `sort_order` INT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`header_id`) REFERENCES `quick_code_header`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_quick_code_item_header ON `quick_code_item`(`header_id`);
CREATE INDEX idx_quick_code_item_code ON `quick_code_item`(`code`);
CREATE INDEX idx_quick_code_item_enabled ON `quick_code_item`(`enabled`);
