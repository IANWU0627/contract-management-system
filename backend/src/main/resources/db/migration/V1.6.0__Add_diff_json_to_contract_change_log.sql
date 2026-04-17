-- 合同关键字段审计：结构化 diff
ALTER TABLE contract_change_log
ADD COLUMN IF NOT EXISTS diff_json TEXT;
