-- 模板变量表添加英文名称字段
-- 执行时间: 2026-04-11

ALTER TABLE template_variable 
ADD COLUMN IF NOT EXISTS name_en VARCHAR(100) 
COMMENT '英文名称';
