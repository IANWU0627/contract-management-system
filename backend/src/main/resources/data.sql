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

-- 初始化模板变量（与 TemplateController 内置五套模板占位符、init-defaults API 一致，共 81 条）
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('contractNo', '合同编号', 'Contract Number', '合同编号', 'text', 'system', '自动生成的合同编号', 0, 1, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('title', '合同名称', 'Contract Title', '合同名称', 'text', 'system', '合同的标题', 0, 2, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('amount', '合同金额', 'Contract Amount', '合同金额', 'number', 'system', '合同总金额', 0, 3, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('amountChinese', '金额大写', 'Amount in Words', '金额大写', 'text', 'system', '金额中文大写', 0, 4, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('startDate', '开始日期', 'Start Date', '起始日期', 'date', 'system', '合同开始/起始日期', 0, 5, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('endDate', '结束日期', 'End Date', '结束日期', 'date', 'system', '合同结束日期', 0, 6, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('signDate', '签订日期', 'Signing Date', '签订日期', 'date', 'system', '合同签订日期', 0, 7, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('today', '当前日期', 'Today''s Date', '当前日期', 'text', 'system', '当前日期', 0, 8, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('partyA', '甲方名称', 'Party A Name', '甲方公司名称', 'text', 'party', '甲方公司全称', 0, 10, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('partyB', '乙方名称', 'Party B Name', '乙方公司名称', 'text', 'party', '乙方公司全称', 0, 11, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('partyAId', '甲方证件号', 'Party A ID Number', '甲方证件号码', 'text', 'party', '甲方身份证明号码', 0, 12, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('partyBId', '乙方证件号', 'Party B ID Number', '乙方证件号码', 'text', 'party', '乙方身份证明号码', 0, 13, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('partyAContact', '甲方联系人', 'Party A Contact', '甲方联系人', 'text', 'party', '甲方联系人姓名', 0, 14, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('partyAPhone', '甲方电话', 'Party A Phone', '甲方联系电话', 'text', 'party', '甲方联系电话', 0, 15, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('partyAEmail', '甲方邮箱', 'Party A Email', '甲方邮箱', 'text', 'party', '甲方电子邮箱', 0, 16, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('partyAAddress', '甲方地址', 'Party A Address', '甲方地址', 'text', 'party', '甲方公司地址', 0, 17, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('partyBContact', '乙方联系人', 'Party B Contact', '乙方联系人', 'text', 'party', '乙方联系人姓名', 0, 18, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('partyBPhone', '乙方电话', 'Party B Phone', '乙方联系电话', 'text', 'party', '乙方联系电话', 0, 19, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('partyBEmail', '乙方邮箱', 'Party B Email', '乙方邮箱', 'text', 'party', '乙方电子邮箱', 0, 20, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('partyBAddress', '乙方地址', 'Party B Address', '乙方地址', 'text', 'party', '乙方公司地址', 0, 21, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('partyASign', '甲方签章', 'Party A Signature', '甲方签章', 'text', 'party', '甲方签章栏', 0, 22, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('partyBSign', '乙方签章', 'Party B Signature', '乙方签章', 'text', 'party', '乙方签章栏', 0, 23, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('productName', '产品名称', 'Product Name', '产品名称', 'text', 'purchase', '采购/代理产品名称', 0, 30, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('quantity', '数量', 'Quantity', '数量', 'number', 'purchase', '采购数量', 0, 31, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('unitPrice', '单价', 'Unit Price', '单价', 'number', 'purchase', '产品单价', 0, 32, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('totalPrice', '总价', 'Total Price', '总价', 'number', 'purchase', '产品总价', 0, 33, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('deliveryDays', '交货天数', 'Delivery Days', '交货天数', 'number', 'purchase', '交货期限（天）', 0, 34, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('deliveryAddress', '交货地址', 'Delivery Address', '交货地址', 'text', 'purchase', '交货/收货详细地址（与表单 delivery_address 对齐）', 0, 35, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('paymentDays', '付款天数', 'Payment Days', '付款周期', 'number', 'purchase', '付款期限（天）', 0, 36, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('penaltyRate', '违约金比例', 'Penalty Rate (%)', '违约金比例', 'number', 'purchase', '违约金百分比', 0, 37, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('projectName', '项目名称', 'Project Name', '项目名称', 'text', 'service', '服务项目名称', 0, 40, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('projectDesc', '项目描述', 'Project Description', '项目描述', 'textarea', 'service', '项目详细描述', 0, 41, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('deliverables', '交付成果', 'Deliverables', '交付成果', 'textarea', 'service', '交付成果物', 0, 42, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('totalDays', '总工期', 'Total Duration (Days)', '总工期', 'number', 'service', '项目总工期（工作日）', 0, 43, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('totalAmount', '合同总金额', 'Total Contract Amount', '合同总金额', 'number', 'service', '服务合同总金额（数字）', 0, 44, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('totalAmountChinese', '合同总金额(大写)', 'Total Amount (Chinese)', '合同总金额大写', 'text', 'service', '服务合同总金额中文大写', 0, 45, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('firstPayment', '首付款', 'Down Payment', '首付款', 'number', 'service', '首付款金额', 0, 46, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('progressPayment', '进度款', 'Progress Payment', '进度款', 'number', 'service', '进度款金额', 0, 47, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('finalPayment', '尾款', 'Final Payment', '尾款', 'number', 'service', '尾款金额', 0, 48, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('address', '房屋地址', 'Property Address', '房屋地址', 'text', 'lease', '租赁房屋地址', 0, 50, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('area', '建筑面积', 'Floor Area (sqm)', '建筑面积', 'number', 'lease', '建筑面积（平方米）', 0, 51, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('leaseMonths', '租赁月数', 'Lease Term (Months)', '租赁月数', 'number', 'lease', '租赁期限（月）', 0, 52, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('monthlyRent', '月租金', 'Monthly Rent', '月租金', 'number', 'lease', '每月租金', 0, 53, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('monthlyRentChinese', '月租金(大写)', 'Monthly Rent (Chinese)', '月租金大写', 'text', 'lease', '月租金中文大写', 0, 54, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('deposit', '押金', 'Security Deposit', '押金', 'number', 'lease', '租赁押金', 0, 55, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('depositChinese', '押金(大写)', 'Deposit (Chinese)', '押金大写', 'text', 'lease', '押金中文大写', 0, 56, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('noticeDays', '提前通知天数', 'Notice Period (Days)', '提前通知天数', 'number', 'lease', '提前解约书面通知天数', 0, 57, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('penaltyMonths', '违约金月数', 'Penalty (Months of Rent)', '违约金月数', 'number', 'lease', '提前解约违约金（月租金倍数）', 0, 58, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('companyName', '公司名称', 'Company Name', '公司名称', 'text', 'employment', '用人单位名称', 0, 60, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('employeeName', '员工姓名', 'Employee Name', '员工姓名', 'text', 'employment', '劳动者姓名', 0, 61, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('employeeId', '身份证号', 'ID Number', '身份证号', 'text', 'employment', '劳动者身份证号码', 0, 62, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('employeePhone', '联系电话', 'Phone Number', '联系电话', 'text', 'employment', '劳动者联系电话', 0, 63, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('position', '职位', 'Position', '岗位/职位', 'text', 'employment', '工作岗位', 0, 64, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('workContent', '工作内容', 'Job Duties', '工作内容', 'textarea', 'employment', '工作内容描述', 0, 65, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('workLocation', '工作地点', 'Work Location', '工作地点', 'text', 'employment', '工作地点', 0, 66, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('salary', '月薪(通用)', 'Monthly Salary (Generic)', '月薪', 'number', 'employment', '通用月薪（可与月工资字段并存）', 0, 67, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('monthlySalary', '月工资', 'Monthly Salary', '月工资', 'number', 'employment', '劳动合同月工资', 0, 68, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('monthlySalaryChinese', '月工资(大写)', 'Monthly Salary (Chinese)', '月工资大写', 'text', 'employment', '月工资中文大写', 0, 69, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('payDay', '工资发放日', 'Pay Day', '工资发放日', 'number', 'employment', '每月发薪日（日）', 0, 70, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('probationPeriod', '试用期', 'Probation Period (Months)', '试用期', 'number', 'employment', '试用期（月）', 0, 71, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('nonCompeteMonths', '竞业限制月数', 'Non-compete Period (Months)', '竞业限制月数', 'number', 'employment', '竞业限制期限（月）', 0, 72, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('nonCompeteCompensation', '竞业限制补偿金', 'Non-compete Compensation', '竞业限制补偿金', 'number', 'employment', '竞业限制补偿（元/月）', 0, 73, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('companySign', '用人单位签章', 'Company Signature', '公司签章', 'text', 'employment', '用人单位签章栏', 0, 74, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('employeeSign', '员工签章', 'Employee Signature', '员工签章', 'text', 'employment', '劳动者签章栏', 0, 75, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('brand', '品牌', 'Brand', '品牌', 'text', 'agency', '代理产品品牌', 0, 80, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('specification', '型号规格', 'Model / Specification', '型号规格', 'text', 'agency', '产品型号规格', 0, 81, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('region', '代理区域', 'Territory', '代理区域', 'text', 'agency', '销售代理区域', 0, 82, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('annualTask', '年度销售任务', 'Annual Sales Target', '年度销售任务', 'number', 'agency', '年度销售任务金额', 0, 83, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('quarterlyTask', '季度任务', 'Quarterly Target', '季度任务', 'number', 'agency', '季度销售任务金额', 0, 84, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('consecutiveQuarters', '连续季度数', 'Consecutive Quarters', '连续季度数', 'number', 'agency', '考核连续未完成季度数', 0, 85, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('taskPercentage', '任务完成比例', 'Task Completion (%)', '任务完成比例', 'number', 'agency', '任务完成比例阈值（%）', 0, 86, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('minRetailPrice', '最低零售价', 'Minimum Retail Price', '最低零售价', 'number', 'agency', '最低零售价格', 0, 87, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('priceNoticeDays', '价格调整通知天数', 'Price Change Notice (Days)', '价格调整通知天数', 'number', 'agency', '价格调整提前通知天数', 0, 88, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('commissionRate', '佣金比例', 'Commission Rate (%)', '佣金比例', 'number', 'agency', '佣金比例（%）', 0, 89, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('salesTarget1', '销售目标1', 'Sales Target 1', '销售目标1', 'number', 'agency', '返利档位销售额1（万）', 0, 90, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('rebateRate1', '返利比例1', 'Rebate Rate 1 (%)', '返利比例1', 'number', 'agency', '档位1返利比例（%）', 0, 91, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('salesTarget2', '销售目标2', 'Sales Target 2', '销售目标2', 'number', 'agency', '返利档位销售额2（万）', 0, 92, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('rebateRate2', '返利比例2', 'Rebate Rate 2 (%)', '返利比例2', 'number', 'agency', '档位2返利比例（%）', 0, 93, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('samplesCount', '样品套数', 'Sample Sets', '样品数量', 'number', 'agency', '市场支持样品套数', 0, 94, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('promoMaterialsCount', '宣传物料份数', 'Promo Materials (Copies)', '宣传物料数量', 'number', 'agency', '宣传物料份数', 0, 95, 1);
INSERT IGNORE INTO template_variable (code, name, name_en, label, type, category, description, required, sort_order, status) VALUES ('adSupportAmount', '广告支持金额', 'Ad Support Amount', '广告支持金额', 'number', 'agency', '广告支持金额（元）', 0, 96, 1);

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
SELECT h.id, 'agency', '代理/渠道', 'Agency / Channel', '销售代理、经销相关变量', 'Sales agency and distribution variables', 'variable-category', true, 8 
FROM quick_code_header h WHERE h.code = 'TEMPLATE_VARIABLE_CATEGORY';
INSERT IGNORE INTO quick_code_item (header_id, code, meaning, meaning_en, description, description_en, tag, enabled, sort_order) 
SELECT h.id, 'custom', '自定义变量', 'Custom Variables', '用户自定义的模板变量', 'User-defined template variables', 'variable-category', true, 9 
FROM quick_code_header h WHERE h.code = 'TEMPLATE_VARIABLE_CATEGORY';
