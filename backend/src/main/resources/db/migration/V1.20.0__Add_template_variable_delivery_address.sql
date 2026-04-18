-- 采购类模板变量补全：交货地址（与合同动态字段 delivery_address、前端 camelCase deliveryAddress 对齐）
-- 已执行过旧版 V1.18.0（无本变量）的库通过本脚本幂等补齐；sort_order 与 paymentDays / penaltyRate 顺延

INSERT INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES
('deliveryAddress', '交货地址', 'Delivery Address', '交货地址', 'text', 'purchase', '交货/收货详细地址（与表单 delivery_address 对齐）', 0, 35, 1)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  name_en = VALUES(name_en),
  label = VALUES(label),
  type = VALUES(type),
  category = VALUES(category),
  description = VALUES(description),
  required = VALUES(required),
  sort_order = VALUES(sort_order),
  status = VALUES(status);

UPDATE template_variable SET sort_order = 36 WHERE code = 'paymentDays' AND category = 'purchase';
UPDATE template_variable SET sort_order = 37 WHERE code = 'penaltyRate' AND category = 'purchase';
