-- 用户部门：用于非管理员/法务的合同数据范围（同部门可见）
ALTER TABLE `user` ADD COLUMN department VARCHAR(100) DEFAULT NULL;
