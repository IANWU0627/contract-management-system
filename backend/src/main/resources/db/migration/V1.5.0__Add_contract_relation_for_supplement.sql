-- 合同关系字段：支持主合同与补充协议关联
ALTER TABLE contract
ADD COLUMN IF NOT EXISTS parent_contract_id BIGINT;

ALTER TABLE contract
ADD COLUMN IF NOT EXISTS relation_type VARCHAR(20) DEFAULT 'MAIN';
