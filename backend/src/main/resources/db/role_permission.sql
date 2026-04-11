-- 角色表
CREATE TABLE IF NOT EXISTS role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    code VARCHAR(20) NOT NULL UNIQUE COMMENT '角色编码',
    description TEXT COMMENT '角色描述',
    status INT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 权限表
CREATE TABLE IF NOT EXISTS permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '权限名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '权限编码',
    path VARCHAR(255) COMMENT 'API路径',
    method VARCHAR(10) COMMENT '请求方法',
    description TEXT COMMENT '权限描述',
    status INT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permission(id) ON DELETE CASCADE
);

-- 初始化角色数据
INSERT INTO role (name, code, description, status) VALUES
('管理员', 'ADMIN', '拥有系统所有权限', 1),
('法务', 'LEGAL', '拥有合同管理和审批权限', 1),
('普通用户', 'USER', '拥有基本的合同查看和创建权限', 1);

-- 初始化权限数据
INSERT INTO permission (name, code, path, method, description, status) VALUES
('工作台查看', 'dashboard_view', '/api/dashboard', 'GET', '查看工作台', 1),
('合同列表查看', 'contracts_view', '/api/contracts', 'GET', '查看合同列表', 1),
('合同创建', 'contracts_create', '/api/contracts', 'POST', '创建合同', 1),
('合同编辑', 'contracts_edit', '/api/contracts/*', 'PUT', '编辑合同', 1),
('合同删除', 'contracts_delete', '/api/contracts/*', 'DELETE', '删除合同', 1),
('合同详情查看', 'contracts_detail', '/api/contracts/*', 'GET', '查看合同详情', 1),
('模板列表查看', 'templates_view', '/api/templates', 'GET', '查看模板列表', 1),
('模板创建', 'templates_create', '/api/templates', 'POST', '创建模板', 1),
('模板编辑', 'templates_edit', '/api/templates/*', 'PUT', '编辑模板', 1),
('模板删除', 'templates_delete', '/api/templates/*', 'DELETE', '删除模板', 1),
('审批管理', 'approvals_view', '/api/approvals', 'GET', '查看审批管理', 1),
('提醒管理', 'reminders_view', '/api/reminders', 'GET', '查看提醒管理', 1),
('数据统计', 'statistics_view', '/api/statistics/*', 'GET', '查看数据统计', 1),
('操作日志', 'logs_view', '/api/logs', 'GET', '查看操作日志', 1),
('用户管理', 'users_view', '/api/users', 'GET', '查看用户管理', 1),
('用户创建', 'users_create', '/api/users', 'POST', '创建用户', 1),
('用户编辑', 'users_edit', '/api/users/*', 'PUT', '编辑用户', 1),
('用户删除', 'users_delete', '/api/users/*', 'DELETE', '删除用户', 1),
('系统设置', 'settings_view', '/api/settings', 'GET', '查看系统设置', 1),
('个人中心', 'profile_view', '/api/users/me', 'GET', '查看个人中心', 1),
('收藏管理', 'favorites_view', '/api/favorites', 'GET', '查看收藏管理', 1),
('标签管理', 'tags_view', '/api/tags', 'GET', '查看标签管理', 1),
('标签创建', 'tags_create', '/api/tags', 'POST', '创建标签', 1),
('标签编辑', 'tags_edit', '/api/tags/*', 'PUT', '编辑标签', 1),
('标签删除', 'tags_delete', '/api/tags/*', 'DELETE', '删除标签', 1),
('提醒规则管理', 'reminder_rules_view', '/api/reminder-rules', 'GET', '查看提醒规则管理', 1),
('提醒规则创建', 'reminder_rules_create', '/api/reminder-rules', 'POST', '创建提醒规则', 1),
('提醒规则编辑', 'reminder_rules_edit', '/api/reminder-rules/*', 'PUT', '编辑提醒规则', 1),
('提醒规则删除', 'reminder_rules_delete', '/api/reminder-rules/*', 'DELETE', '删除提醒规则', 1),
('续约管理', 'renewals_view', '/api/renewals', 'GET', '查看续约管理', 1),
('续约创建', 'renewals_create', '/api/contracts/*/renewals', 'POST', '创建续约', 1),
('续约审批', 'renewals_approve', '/api/contracts/*/renewals/*/approve', 'PUT', '审批续约', 1),
('续约拒绝', 'renewals_reject', '/api/contracts/*/renewals/*/reject', 'PUT', '拒绝续约', 1),
('合同管理', 'CONTRACT_MANAGE', '/api/contracts/*', 'GET', '查看合同管理', 1),
('合同审批', 'CONTRACT_APPROVE', '/api/approvals', 'GET', '审批合同', 1),
('提醒管理', 'REMINDER_MANAGE', '/api/reminders', 'GET', '提醒管理', 1),
('收藏管理', 'FAVORITE_MANAGE', '/api/favorites', 'GET', '收藏管理', 1),
('续约管理', 'RENEWAL_MANAGE', '/api/renewals', 'GET', '续约管理', 1),
('报表查看', 'REPORT_VIEW', '/api/statistics', 'GET', '查看报表', 1),
('标签管理', 'TAG_MANAGE', '/api/tags', 'GET', '标签管理', 1),
('提醒规则管理', 'REMINDER_RULE_MANAGE', '/api/reminder-rules', 'GET', '提醒规则管理', 1),
('角色管理', 'ROLE_MANAGE', '/api/roles', 'GET', '角色管理', 1),
('用户管理', 'USER_MANAGE', '/api/users', 'GET', '用户管理', 1),
('设置管理', 'SETTING_MANAGE', '/api/settings', 'GET', '设置管理', 1),
('类型字段配置', 'TYPE_FIELD_CONFIG', '/api/contract-type-fields', 'GET', '类型字段配置', 1),
('变更记录', 'CHANGE_LOG_VIEW', '/api/change-logs', 'GET', '查看变更记录', 1);

-- 初始化角色权限关联数据
-- 管理员权限
INSERT INTO role_permission (role_id, permission_id) SELECT r.id, p.id FROM role r, permission p WHERE r.code = 'ADMIN';

-- 法务权限
INSERT INTO role_permission (role_id, permission_id) SELECT r.id, p.id FROM role r, permission p 
WHERE r.code = 'LEGAL' AND p.code IN (
    'dashboard_view', 'contracts_view', 'contracts_create', 'contracts_edit', 'contracts_detail',
    'templates_view', 'templates_create', 'templates_edit', 'templates_delete',
    'approvals_view', 'reminders_view', 'statistics_view', 'profile_view', 'favorites_view',
    'tags_view', 'tags_create', 'tags_edit', 'tags_delete', 'renewals_view', 'renewals_create',
    'renewals_approve', 'renewals_reject', 'CONTRACT_MANAGE', 'CONTRACT_APPROVE',
    'REMINDER_MANAGE', 'FAVORITE_MANAGE', 'RENEWAL_MANAGE', 'REPORT_VIEW',
    'TAG_MANAGE', 'REMINDER_RULE_MANAGE', 'CHANGE_LOG_VIEW'
);

-- 普通用户权限
INSERT INTO role_permission (role_id, permission_id) SELECT r.id, p.id FROM role r, permission p 
WHERE r.code = 'USER' AND p.code IN (
    'dashboard_view', 'contracts_view', 'contracts_create', 'contracts_detail',
    'templates_view', 'profile_view', 'favorites_view'
);
