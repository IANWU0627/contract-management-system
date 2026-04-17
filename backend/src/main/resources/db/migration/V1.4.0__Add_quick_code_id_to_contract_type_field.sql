-- 为合同类型字段配置补充 quick_code_id 列
-- 兼容旧库，修复更新/排序时报 Unknown column 'quick_code_id' 的 500 错误
ALTER TABLE contract_type_field
ADD COLUMN IF NOT EXISTS quick_code_id VARCHAR(50);
