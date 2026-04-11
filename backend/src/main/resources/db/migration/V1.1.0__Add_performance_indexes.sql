-- 合同管理系统数据库索引优化脚本
-- 执行时间: 2026-04-10
-- 目的: 提升常用查询的性能

-- ============================================
-- 1. contracts 表索引优化
-- ============================================

-- 合同编号索引（唯一）
CREATE UNIQUE INDEX IF NOT EXISTS idx_contracts_contract_no ON contracts(contract_no);

-- 合同标题索引（全文搜索）
CREATE INDEX IF NOT EXISTS idx_contracts_title ON contracts(title);

-- 状态索引
CREATE INDEX IF NOT EXISTS idx_contracts_status ON contracts(status);

-- 类型索引
CREATE INDEX IF NOT EXISTS idx_contracts_type ON contracts(type);

-- 对方单位索引
CREATE INDEX IF NOT EXISTS idx_contracts_counterparty ON contracts(counterparty);

-- 开始日期索引
CREATE INDEX IF NOT EXISTS idx_contracts_start_date ON contracts(start_date);

-- 结束日期索引
CREATE INDEX IF NOT EXISTS idx_contracts_end_date ON contracts(end_date);

-- 创建时间索引
CREATE INDEX IF NOT EXISTS idx_contracts_created_at ON contracts(created_at);

-- 创建者索引
CREATE INDEX IF NOT EXISTS idx_contracts_created_by ON contracts(created_by);

-- 文件夹索引
CREATE INDEX IF NOT EXISTS idx_contracts_folder_id ON contracts(folder_id);

-- 复合索引：状态 + 创建时间（常用查询）
CREATE INDEX IF NOT EXISTS idx_contracts_status_created ON contracts(status, created_at DESC);

-- 复合索引：类型 + 结束日期（到期提醒常用）
CREATE INDEX IF NOT EXISTS idx_contracts_type_end_date ON contracts(type, end_date);

-- ============================================
-- 2. users 表索引优化
-- ============================================

-- 用户名校验（唯一）
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- 邮箱索引
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- 状态索引
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

-- 角色索引
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- ============================================
-- 3. operation_logs 表索引优化
-- ============================================

-- 操作类型索引
CREATE INDEX IF NOT EXISTS idx_logs_operation ON operation_logs(operation);

-- 模块索引
CREATE INDEX IF NOT EXISTS idx_logs_module ON operation_logs(module);

-- 用户ID索引
CREATE INDEX IF NOT EXISTS idx_logs_user_id ON operation_logs(user_id);

-- 操作时间索引（倒序，因为通常查询最新）
CREATE INDEX IF NOT EXISTS idx_logs_created_at ON operation_logs(created_at DESC);

-- 复合索引：用户 + 时间
CREATE INDEX IF NOT EXISTS idx_logs_user_time ON operation_logs(user_id, created_at DESC);

-- ============================================
-- 4. contract_categories 表索引优化
-- ============================================

-- 分类编码索引（唯一）
CREATE UNIQUE INDEX IF NOT EXISTS idx_categories_code ON contract_categories(code);

-- 状态索引
CREATE INDEX IF NOT EXISTS idx_categories_active ON contract_categories(active);

-- 排序索引
CREATE INDEX IF NOT EXISTS idx_categories_sort_order ON contract_categories(sort_order);

-- ============================================
-- 5. contract_folders 表索引优化
-- ============================================

-- 父文件夹索引
CREATE INDEX IF NOT EXISTS idx_folders_parent_id ON contract_folders(parent_id);

-- 创建者索引
CREATE INDEX IF NOT EXISTS idx_folders_created_by ON contract_folders(created_by);

-- ============================================
-- 6. contract_reminders 表索引优化
-- ============================================

-- 合同ID索引
CREATE INDEX IF NOT EXISTS idx_reminders_contract_id ON contract_reminders(contract_id);

-- 用户ID索引
CREATE INDEX IF NOT EXISTS idx_reminders_user_id ON contract_reminders(user_id);

-- 状态索引
CREATE INDEX IF NOT EXISTS idx_reminders_status ON contract_reminders(status);

-- 提醒日期索引
CREATE INDEX IF NOT EXISTS idx_reminders_remind_date ON contract_reminders(remind_date);

-- ============================================
-- 7. contract_renewals 表索引优化
-- ============================================

-- 合同ID索引
CREATE INDEX IF NOT EXISTS idx_renewals_contract_id ON contract_renewals(contract_id);

-- 状态索引
CREATE INDEX IF NOT EXISTS idx_renewals_status ON contract_renewals(status);

-- 创建时间索引
CREATE INDEX IF NOT EXISTS idx_renewals_created_at ON contract_renewals(created_at DESC);

-- ============================================
-- 8. contract_approvals 表索引优化
-- ============================================

-- 合同ID索引
CREATE INDEX IF NOT EXISTS idx_approvals_contract_id ON contract_approvals(contract_id);

-- 审批人索引
CREATE INDEX IF NOT EXISTS idx_approvals_approver_id ON contract_approvals(approver_id);

-- 状态索引
CREATE INDEX IF NOT EXISTS idx_approvals_status ON contract_approvals(status);

-- 创建时间索引
CREATE INDEX IF NOT EXISTS idx_approvals_created_at ON contract_approvals(created_at DESC);

-- ============================================
-- 9. contract_templates 表索引优化
-- ============================================

-- 模板编码索引（唯一）
CREATE UNIQUE INDEX IF NOT EXISTS idx_templates_code ON contract_templates(code);

-- 分类索引
CREATE INDEX IF NOT EXISTS idx_templates_category ON contract_templates(category);

-- 状态索引
CREATE INDEX IF NOT EXISTS idx_templates_active ON contract_templates(active);

-- ============================================
-- 10. quick_codes 表索引优化
-- ============================================

-- 编码索引（唯一）
CREATE UNIQUE INDEX IF NOT EXISTS idx_quick_codes_code ON quick_codes(code);

-- 状态索引
CREATE INDEX IF NOT EXISTS idx_quick_codes_status ON quick_codes(status);

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
