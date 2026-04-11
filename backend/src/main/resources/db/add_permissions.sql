-- 添加新权限到现有数据库
-- 运行此脚本添加缺失的权限

-- 插入缺失的权限
INSERT IGNORE INTO permission (name, code, path, method, description, status) VALUES
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
('变更记录', 'CHANGE_LOG_VIEW', '/api/change-logs', 'GET', '查看变更记录', 1),
('快速代码', 'QUICK_CODE_MANAGE', '/api/quick-codes', 'GET', '管理快速代码', 1);

-- 为管理员角色添加所有新权限
INSERT IGNORE INTO role_permission (role_id, permission_id) 
SELECT r.id, p.id FROM role r, permission p 
WHERE r.code = 'ADMIN' AND p.code IN (
    'CONTRACT_MANAGE', 'CONTRACT_APPROVE', 'REMINDER_MANAGE', 'FAVORITE_MANAGE',
    'RENEWAL_MANAGE', 'REPORT_VIEW', 'TAG_MANAGE', 'REMINDER_RULE_MANAGE',
    'ROLE_MANAGE', 'USER_MANAGE', 'SETTING_MANAGE', 'TYPE_FIELD_CONFIG', 'CHANGE_LOG_VIEW',
    'QUICK_CODE_MANAGE'
);

-- 为法务角色添加新权限
INSERT IGNORE INTO role_permission (role_id, permission_id) 
SELECT r.id, p.id FROM role r, permission p 
WHERE r.code = 'LEGAL' AND p.code IN (
    'CONTRACT_MANAGE', 'CONTRACT_APPROVE', 'REMINDER_MANAGE', 'FAVORITE_MANAGE',
    'RENEWAL_MANAGE', 'REPORT_VIEW', 'TAG_MANAGE', 'REMINDER_RULE_MANAGE', 'CHANGE_LOG_VIEW',
    'QUICK_CODE_MANAGE'
);
