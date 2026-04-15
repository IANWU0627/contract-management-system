-- 合同管理系统数据库索引优化脚本
-- 执行时间: 2026-04-10
-- 目的: 提升常用查询的性能

-- ============================================
-- 1. contract 表索引优化
-- ============================================

-- 合同编号索引（唯一）
CREATE UNIQUE INDEX IF NOT EXISTS idx_contracts_contract_no ON contract(contract_no);

-- 合同标题索引（全文搜索）
CREATE INDEX IF NOT EXISTS idx_contracts_title ON contract(title);

-- 状态索引
CREATE INDEX IF NOT EXISTS idx_contracts_status ON contract(status);

-- 类型索引
CREATE INDEX IF NOT EXISTS idx_contracts_type ON contract(type);

-- 对方单位索引
CREATE INDEX IF NOT EXISTS idx_contracts_counterparty ON contract(counterparty);

-- 开始日期索引
CREATE INDEX IF NOT EXISTS idx_contracts_start_date ON contract(start_date);

-- 结束日期索引
CREATE INDEX IF NOT EXISTS idx_contracts_end_date ON contract(end_date);

-- 创建时间索引
CREATE INDEX IF NOT EXISTS idx_contracts_create_time ON contract(create_time);

-- 创建者索引
CREATE INDEX IF NOT EXISTS idx_contracts_creator_id ON contract(creator_id);

-- 文件夹索引
CREATE INDEX IF NOT EXISTS idx_contracts_folder_id ON contract(folder_id);

-- 复合索引：状态 + 创建时间（常用查询）
CREATE INDEX IF NOT EXISTS idx_contracts_status_create_time ON contract(status, create_time DESC);

-- 复合索引：类型 + 结束日期（到期提醒常用）
CREATE INDEX IF NOT EXISTS idx_contracts_type_end_date ON contract(type, end_date);

-- ============================================
-- 2. user 表索引优化
-- ============================================

-- 用户名校验（唯一）
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username ON user(username);

-- 邮箱索引
CREATE INDEX IF NOT EXISTS idx_users_email ON user(email);

-- 状态索引
CREATE INDEX IF NOT EXISTS idx_users_status ON user(status);

-- 角色索引
CREATE INDEX IF NOT EXISTS idx_users_role ON user(role);

-- ============================================
-- 3. operation_log 表索引优化
-- ============================================

-- 操作类型索引
CREATE INDEX IF NOT EXISTS idx_logs_operation ON operation_log(operation);

-- 模块索引
CREATE INDEX IF NOT EXISTS idx_logs_module ON operation_log(module);

-- 用户ID索引
CREATE INDEX IF NOT EXISTS idx_logs_operator_id ON operation_log(operator_id);

-- 操作时间索引（倒序，因为通常查询最新）
CREATE INDEX IF NOT EXISTS idx_logs_created_at ON operation_log(created_at DESC);

-- 复合索引：用户 + 时间
CREATE INDEX IF NOT EXISTS idx_logs_operator_time ON operation_log(operator_id, created_at DESC);

-- ============================================
-- 4. contract_category 表索引优化
-- ============================================

-- 分类编码索引（唯一）
CREATE UNIQUE INDEX IF NOT EXISTS idx_categories_code ON contract_category(code);

-- 状态索引
CREATE INDEX IF NOT EXISTS idx_categories_status ON contract_category(status);

-- 排序索引
CREATE INDEX IF NOT EXISTS idx_categories_sort_order ON contract_category(sort_order);

-- ============================================
-- 5. contract_folder 表索引优化
-- ============================================

-- 父文件夹索引
CREATE INDEX IF NOT EXISTS idx_folders_parent_id ON contract_folder(parent_id);

-- 创建者索引
CREATE INDEX IF NOT EXISTS idx_folders_creator_id ON contract_folder(creator_id);

-- ============================================
-- 6. contract_reminder 表索引优化
-- ============================================

-- 合同ID索引
CREATE INDEX IF NOT EXISTS idx_reminders_contract_id ON contract_reminder(contract_id);

-- 用户ID索引
CREATE INDEX IF NOT EXISTS idx_reminders_status ON contract_reminder(status);

-- 提醒日期索引
CREATE INDEX IF NOT EXISTS idx_reminders_expire_date ON contract_reminder(expire_date);

-- ============================================
-- 7. contract_renewal 表索引优化
-- ============================================

-- 合同ID索引
CREATE INDEX IF NOT EXISTS idx_renewals_contract_id ON contract_renewal(contract_id);

-- 状态索引
CREATE INDEX IF NOT EXISTS idx_renewals_created_at ON contract_renewal(created_at DESC);

-- ============================================
-- 8. approval_record 表索引优化
-- ============================================

-- 合同ID索引
CREATE INDEX IF NOT EXISTS idx_approvals_contract_id ON approval_record(contract_id);

-- 审批人索引
CREATE INDEX IF NOT EXISTS idx_approvals_approver_id ON approval_record(approver_id);

-- 状态索引
CREATE INDEX IF NOT EXISTS idx_approvals_status ON approval_record(status);

-- 创建时间索引
CREATE INDEX IF NOT EXISTS idx_approvals_create_time ON approval_record(create_time DESC);

-- ============================================
-- 9. contract_template 表索引优化
-- ============================================

-- 分类索引
CREATE INDEX IF NOT EXISTS idx_templates_category ON contract_template(category);

-- ============================================
-- 10. quick_code_header / quick_code_item 表索引优化
-- ============================================

CREATE UNIQUE INDEX IF NOT EXISTS idx_quick_code_header_code ON quick_code_header(code);

CREATE INDEX IF NOT EXISTS idx_quick_code_header_status ON quick_code_header(status);
CREATE INDEX IF NOT EXISTS idx_quick_code_item_header_id ON quick_code_item.header_id;
CREATE INDEX IF NOT EXISTS idx_quick_code_item_status ON quick_code_item.status;

-- ============================================
-- 索引创建完成提示
-- ============================================

-- 执行完成后，可以使用以下命令查看索引：
-- SELECT tablename, indexname, indexdef 
-- FROM pg_indexes 
-- WHERE schemaname = 'public'
-- ORDER BY tablename, indexname;

-- 分析表以更新统计信息（PostgreSQL）
ANALYZE;
