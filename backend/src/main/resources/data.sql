-- 初始化角色数据
INSERT IGNORE INTO role (name, code, description, status) VALUES ('系统管理员', 'ADMIN', '拥有系统所有权限', 1);
INSERT IGNORE INTO role (name, code, description, status) VALUES ('法务人员', 'LEGAL', '拥有合同管理和审批权限', 1);
INSERT IGNORE INTO role (name, code, description, status) VALUES ('普通用户', 'USER', '拥有基础合同查看权限', 1);

-- 初始化权限数据
INSERT IGNORE INTO permission (name, code, description, status) VALUES ('合同管理', 'CONTRACT_MANAGE', '管理合同的增删改查', 1);
INSERT IGNORE INTO permission (name, code, description, status) VALUES ('合同审批', 'CONTRACT_APPROVE', '审批合同', 1);
INSERT IGNORE INTO permission (name, code, description, status) VALUES ('模板管理', 'TEMPLATE_MANAGE', '管理合同模板', 1);
INSERT IGNORE INTO permission (name, code, description, status) VALUES ('用户管理', 'USER_MANAGE', '管理用户', 1);
INSERT IGNORE INTO permission (name, code, description, status) VALUES ('角色管理', 'ROLE_MANAGE', '管理角色', 1);
INSERT IGNORE INTO permission (name, code, description, status) VALUES ('统计报表', 'REPORT_VIEW', '查看统计报表', 1);
INSERT IGNORE INTO permission (name, code, description, status) VALUES ('提醒管理', 'REMINDER_MANAGE', '管理合同到期提醒', 1);
INSERT IGNORE INTO permission (name, code, description, status) VALUES ('我的收藏', 'FAVORITE_MANAGE', '管理收藏的合同', 1);
INSERT IGNORE INTO permission (name, code, description, status) VALUES ('续约管理', 'RENEWAL_MANAGE', '管理合同续约', 1);
INSERT IGNORE INTO permission (name, code, description, status) VALUES ('标签管理', 'TAG_MANAGE', '管理合同标签', 1);
INSERT IGNORE INTO permission (name, code, description, status) VALUES ('提醒规则', 'REMINDER_RULE_MANAGE', '管理提醒规则', 1);
INSERT IGNORE INTO permission (name, code, description, status) VALUES ('系统设置', 'SETTING_MANAGE', '管理系统设置', 1);
INSERT IGNORE INTO permission (name, code, description, status) VALUES ('文件夹管理', 'FOLDER_MANAGE', '管理合同文件夹', 1);
INSERT IGNORE INTO permission (name, code, description, status) VALUES ('变量管理', 'VARIABLE_MANAGE', '管理模板变量', 1);
INSERT IGNORE INTO permission (name, code, description, status) VALUES ('类型管理', 'CATEGORY_MANAGE', '管理合同类型', 1);
INSERT IGNORE INTO permission (name, code, description, status) VALUES ('快速代码', 'QUICK_CODE_MANAGE', '管理快速代码', 1);

-- 初始化角色权限关联数据
-- 系统管理员拥有所有权限
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (1, 1);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (1, 2);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (1, 3);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (1, 4);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (1, 5);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (1, 6);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (1, 7);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (1, 8);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (1, 9);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (1, 10);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (1, 11);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (1, 12);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (1, 13);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (1, 14);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (1, 15);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (1, 16);

-- 法务人员拥有合同管理、审批、模板管理、提醒管理、续约管理、标签管理、文件夹管理和统计报表权限
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (2, 1);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (2, 2);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (2, 3);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (2, 6);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (2, 7);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (2, 9);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (2, 10);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (2, 13);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (2, 15);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (2, 16);

-- 普通用户拥有合同管理和我的收藏权限
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (3, 1);
INSERT IGNORE INTO role_permission (role_id, permission_id) VALUES (3, 8);

-- 初始化用户数据 (所有密码为: admin123)
INSERT IGNORE INTO `user` (username, password, nickname, email, phone, role, status) VALUES ('admin', '$2a$10$2jAxOq9MH/virm6E6ZTa5ONdUaKeOZAxiL5ZCEaswF9ne623Pi7GK', '系统管理员', 'admin@toycontract.com', '13800138000', 'ADMIN', 1);
INSERT IGNORE INTO `user` (username, password, nickname, email, phone, role, status) VALUES ('cain', '$2a$10$2jAxOq9MH/virm6E6ZTa5ONdUaKeOZAxiL5ZCEaswF9ne623Pi7GK', 'Cain管理员', 'cain@toycontract.com', '13800138001', 'ADMIN', 1);
INSERT IGNORE INTO `user` (username, password, nickname, email, phone, role, status) VALUES ('zhangsan', '$2a$10$2jAxOq9MH/virm6E6ZTa5ONdUaKeOZAxiL5ZCEaswF9ne623Pi7GK', '张三法务', 'zhangsan@toycontract.com', '13800138002', 'LEGAL', 1);
INSERT IGNORE INTO `user` (username, password, nickname, email, phone, role, status) VALUES ('lisi', '$2a$10$2jAxOq9MH/virm6E6ZTa5ONdUaKeOZAxiL5ZCEaswF9ne623Pi7GK', '李四法务', 'lisi@toycontract.com', '13800138003', 'LEGAL', 1);
INSERT IGNORE INTO `user` (username, password, nickname, email, phone, role, status) VALUES ('wangwu', '$2a$10$2jAxOq9MH/virm6E6ZTa5ONdUaKeOZAxiL5ZCEaswF9ne623Pi7GK', '王五用户', 'wangwu@toycontract.com', '13800138004', 'USER', 1);
INSERT IGNORE INTO `user` (username, password, nickname, email, phone, role, status) VALUES ('zhaoliu', '$2a$10$2jAxOq9MH/virm6E6ZTa5ONdUaKeOZAxiL5ZCEaswF9ne623Pi7GK', '赵六用户', 'zhaoliu@toycontract.com', '13800138005', 'USER', 1);

-- 初始化模板变量数据
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('contractNo', '合同编号', '合同编号', 'text', 'system', '自动生成的合同编号', 0, 1, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('title', '合同名称', '合同名称', 'text', 'system', '合同的标题', 1, 2, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('amount', '合同金额', '合同金额', 'number', 'system', '合同的总金额', 1, 3, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('amountChinese', '金额大写', '金额大写', 'text', 'system', '金额的中文大写形式', 0, 4, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('startDate', '开始日期', '开始日期', 'date', 'system', '合同开始日期', 1, 5, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('endDate', '结束日期', '结束日期', 'date', 'system', '合同结束日期', 1, 6, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('signDate', '签订日期', '签订日期', 'date', 'system', '合同签订日期', 0, 7, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('today', '当前日期', '当前日期', 'text', 'system', '当前日期', 0, 8, 1);

INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('partyA', '甲方名称', '甲方公司名称', 'text', 'party', '甲方公司全称', 1, 10, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('partyAContact', '甲方联系人', '甲方联系人', 'text', 'party', '甲方的联系人姓名', 0, 11, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('partyAPhone', '甲方电话', '甲方联系电话', 'text', 'party', '甲方的联系电话', 0, 12, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('partyAEmail', '甲方邮箱', '甲方邮箱', 'text', 'party', '甲方的电子邮箱', 0, 13, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('partyAAddress', '甲方地址', '甲方地址', 'text', 'party', '甲方的公司地址', 0, 14, 1);

INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('partyB', '乙方名称', '乙方公司名称', 'text', 'party', '乙方公司全称', 1, 20, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('partyBContact', '乙方联系人', '乙方联系人', 'text', 'party', '乙方的联系人姓名', 0, 21, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('partyBPhone', '乙方电话', '乙方联系电话', 'text', 'party', '乙方的联系电话', 0, 22, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('partyBEmail', '乙方邮箱', '乙方邮箱', 'text', 'party', '乙方的电子邮箱', 0, 23, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('partyBAddress', '乙方地址', '乙方地址', 'text', 'party', '乙方的公司地址', 0, 24, 1);

INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('productName', '产品名称', '产品名称', 'text', 'purchase', '采购产品的名称', 1, 30, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('quantity', '数量', '数量', 'number', 'purchase', '采购产品的数量', 1, 31, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('unitPrice', '单价', '单价', 'number', 'purchase', '产品的单价', 1, 32, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('totalPrice', '总价', '总价', 'number', 'purchase', '产品的总价格', 1, 33, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('deliveryDays', '交货天数', '交货天数', 'number', 'purchase', '交货期限（天）', 0, 34, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('paymentDays', '付款天数', '付款天数', 'number', 'purchase', '付款期限（天）', 0, 35, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('penaltyRate', '违约金比例', '违约金比例', 'number', 'purchase', '违约金的百分比', 0, 36, 1);

INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('projectName', '项目名称', '项目名称', 'text', 'service', '服务项目的名称', 1, 40, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('projectDesc', '项目描述', '项目描述', 'textarea', 'service', '项目的详细描述', 0, 41, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('deliverables', '交付成果', '交付成果', 'textarea', 'service', '项目交付的成果物', 0, 42, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('totalDays', '总工期', '总工期', 'number', 'service', '项目总工期（天）', 0, 43, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('totalAmount', '总金额', '服务总金额', 'number', 'service', '服务的总金额', 1, 44, 1);

INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('address', '房屋地址', '房屋地址', 'text', 'lease', '租赁房屋的地址', 1, 50, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('area', '建筑面积', '建筑面积', 'number', 'lease', '房屋的建筑面积（平方米）', 0, 51, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('leaseMonths', '租赁月数', '租赁月数', 'number', 'lease', '租赁的月数', 1, 52, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('monthlyRent', '月租金', '月租金', 'number', 'lease', '每月的租金', 1, 53, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('deposit', '押金', '押金', 'number', 'lease', '租赁押金', 0, 54, 1);

INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('employeeName', '员工姓名', '员工姓名', 'text', 'employment', '员工的全名', 1, 60, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('companyName', '公司名称', '公司名称', 'text', 'employment', '公司的全称', 1, 61, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('position', '职位', '职位', 'text', 'employment', '员工担任的职位', 1, 62, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('salary', '薪资', '月薪', 'number', 'employment', '每月的工资', 1, 63, 1);
INSERT IGNORE INTO template_variable (code, name, label, type, category, description, required, sort_order, status) VALUES ('probationPeriod', '试用期', '试用期', 'number', 'employment', '试用期的月数', 0, 64, 1);

-- 初始化快速代码数据
INSERT IGNORE INTO quick_code_header (name, name_en, code, description, description_en, status) VALUES ('支付方式', 'Payment Method', 'PAYMENT_METHOD', '合同支付方式', 'Contract payment methods', 1);
INSERT IGNORE INTO quick_code_header (name, name_en, code, description, description_en, status) VALUES ('币种', 'Currency', 'CURRENCY', '货币类型', 'Currency types', 1);
INSERT IGNORE INTO quick_code_header (name, name_en, code, description, description_en, status) VALUES ('合同状态', 'Contract Status', 'CONTRACT_STATUS', '合同状态选项', 'Contract status options', 1);
INSERT IGNORE INTO quick_code_header (name, name_en, code, description, description_en, status) VALUES ('模板变量分类', 'Template Variable Category', 'TEMPLATE_VARIABLE_CATEGORY', '模板变量分类配置', 'Template variable category configuration', 1);

-- 支付方式选项
INSERT IGNORE INTO quick_code_item (header_id, code, meaning, meaning_en, description, description_en, tag, enabled, sort_order) 
SELECT h.id, 'PREPAY', '预付款', 'Prepayment', '预付部分款项后执行', 'Pay part of the amount before execution', 'payment', true, 1 
FROM quick_code_header h WHERE h.code = 'PAYMENT_METHOD';
INSERT IGNORE INTO quick_code_item (header_id, code, meaning, meaning_en, description, description_en, tag, enabled, sort_order) 
SELECT h.id, 'ONETIME', '一次性支付', 'One-time Payment', '完成后一次性支付全款', 'Pay the full amount after completion', 'payment', true, 2 
FROM quick_code_header h WHERE h.code = 'PAYMENT_METHOD';
INSERT IGNORE INTO quick_code_item (header_id, code, meaning, meaning_en, description, description_en, tag, enabled, sort_order) 
SELECT h.id, 'INSTALLMENT', '分期付款', 'Installment', '按阶段分期支付', 'Pay in stages', 'payment', true, 3 
FROM quick_code_header h WHERE h.code = 'PAYMENT_METHOD';
INSERT IGNORE INTO quick_code_item (header_id, code, meaning, meaning_en, description, description_en, tag, enabled, sort_order) 
SELECT h.id, 'MONTHLY', '月结', 'Monthly Settlement', '每月结算一次', 'Settle once per month', 'payment', true, 4 
FROM quick_code_header h WHERE h.code = 'PAYMENT_METHOD';

-- 币种选项
INSERT IGNORE INTO quick_code_item (header_id, code, meaning, meaning_en, description, description_en, tag, enabled, sort_order) 
SELECT h.id, 'CNY', '人民币', 'RMB (Chinese Yuan)', '中华人民共和国法定货币', 'Legal currency of China', 'currency', true, 1 
FROM quick_code_header h WHERE h.code = 'CURRENCY';
INSERT IGNORE INTO quick_code_item (header_id, code, meaning, meaning_en, description, description_en, tag, enabled, sort_order) 
SELECT h.id, 'USD', '美元', 'US Dollar', '美国法定货币', 'Legal currency of USA', 'currency', true, 2 
FROM quick_code_header h WHERE h.code = 'CURRENCY';
INSERT IGNORE INTO quick_code_item (header_id, code, meaning, meaning_en, description, description_en, tag, enabled, sort_order) 
SELECT h.id, 'EUR', '欧元', 'Euro', '欧盟法定货币', 'Legal currency of EU', 'currency', true, 3 
FROM quick_code_header h WHERE h.code = 'CURRENCY';
INSERT IGNORE INTO quick_code_item (header_id, code, meaning, meaning_en, description, description_en, tag, enabled, sort_order) 
SELECT h.id, 'HKD', '港币', 'Hong Kong Dollar', '香港法定货币', 'Legal currency of Hong Kong', 'currency', true, 4 
FROM quick_code_header h WHERE h.code = 'CURRENCY';

-- 模板变量分类选项
INSERT IGNORE INTO quick_code_item (header_id, code, meaning, meaning_en, description, description_en, tag, enabled, sort_order) 
SELECT h.id, 'system', '系统变量', 'System Variables', '系统内置的模板变量', 'Built-in system template variables', 'variable-category', true, 1 
FROM quick_code_header h WHERE h.code = 'TEMPLATE_VARIABLE_CATEGORY';
INSERT IGNORE INTO quick_code_item (header_id, code, meaning, meaning_en, description, description_en, tag, enabled, sort_order) 
SELECT h.id, 'party', '相对方信息', 'Counterparty Information', '合同相对方相关变量', 'Contract counterparty related variables', 'variable-category', true, 2 
FROM quick_code_header h WHERE h.code = 'TEMPLATE_VARIABLE_CATEGORY';
INSERT IGNORE INTO quick_code_item (header_id, code, meaning, meaning_en, description, description_en, tag, enabled, sort_order) 
SELECT h.id, 'purchase', '采购合同', 'Purchase Contract', '采购合同相关变量', 'Purchase contract related variables', 'variable-category', true, 3 
FROM quick_code_header h WHERE h.code = 'TEMPLATE_VARIABLE_CATEGORY';
INSERT IGNORE INTO quick_code_item (header_id, code, meaning, meaning_en, description, description_en, tag, enabled, sort_order) 
SELECT h.id, 'service', '服务合同', 'Service Contract', '服务合同相关变量', 'Service contract related variables', 'variable-category', true, 4 
FROM quick_code_header h WHERE h.code = 'TEMPLATE_VARIABLE_CATEGORY';
INSERT IGNORE INTO quick_code_item (header_id, code, meaning, meaning_en, description, description_en, tag, enabled, sort_order) 
SELECT h.id, 'lease', '租赁合同', 'Lease Contract', '租赁合同相关变量', 'Lease contract related variables', 'variable-category', true, 5 
FROM quick_code_header h WHERE h.code = 'TEMPLATE_VARIABLE_CATEGORY';
INSERT IGNORE INTO quick_code_item (header_id, code, meaning, meaning_en, description, description_en, tag, enabled, sort_order) 
SELECT h.id, 'employment', '劳动合同', 'Employment Contract', '劳动合同相关变量', 'Employment contract related variables', 'variable-category', true, 6 
FROM quick_code_header h WHERE h.code = 'TEMPLATE_VARIABLE_CATEGORY';
INSERT IGNORE INTO quick_code_item (header_id, code, meaning, meaning_en, description, description_en, tag, enabled, sort_order) 
SELECT h.id, 'custom', '自定义变量', 'Custom Variables', '用户自定义的模板变量', 'User-defined template variables', 'variable-category', true, 7 
FROM quick_code_header h WHERE h.code = 'TEMPLATE_VARIABLE_CATEGORY';
