-- 模板变量表添加快速代码关联字段
-- 执行时间: 2026-04-10

ALTER TABLE template_variable 
ADD COLUMN IF NOT EXISTS quick_code_code VARCHAR(100) 
COMMENT '关联的快速代码编码';

-- 添加索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_template_variable_quick_code 
ON template_variable(quick_code_code);
