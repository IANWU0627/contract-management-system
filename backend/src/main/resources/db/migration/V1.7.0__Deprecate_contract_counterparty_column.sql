-- contract.counterparty 已废弃，仅保留兼容；改为可空，避免业务继续依赖该列
ALTER TABLE contract MODIFY COLUMN counterparty VARCHAR(255) NULL;
