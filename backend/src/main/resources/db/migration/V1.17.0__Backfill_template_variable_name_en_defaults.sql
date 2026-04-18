-- 为内置模板变量补全英文名称（与 TemplateVariableController.initDefaultVariables / data.sql 一致）
-- 仅回填 name_en 为空的记录，避免覆盖用户已维护的英文名称

UPDATE template_variable SET name_en = 'Contract Number' WHERE code = 'contractNo' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Contract Title' WHERE code = 'title' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Contract Amount' WHERE code = 'amount' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Amount in Words' WHERE code = 'amountChinese' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Start Date' WHERE code = 'startDate' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'End Date' WHERE code = 'endDate' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Signing Date' WHERE code = 'signDate' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Today''s Date' WHERE code = 'today' AND (name_en IS NULL OR TRIM(name_en) = '');

UPDATE template_variable SET name_en = 'Party A Name' WHERE code = 'partyA' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Party A Contact' WHERE code = 'partyAContact' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Party A Phone' WHERE code = 'partyAPhone' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Party A Email' WHERE code = 'partyAEmail' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Party A Address' WHERE code = 'partyAAddress' AND (name_en IS NULL OR TRIM(name_en) = '');

UPDATE template_variable SET name_en = 'Party B Name' WHERE code = 'partyB' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Party B Contact' WHERE code = 'partyBContact' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Party B Phone' WHERE code = 'partyBPhone' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Party B Email' WHERE code = 'partyBEmail' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Party B Address' WHERE code = 'partyBAddress' AND (name_en IS NULL OR TRIM(name_en) = '');

UPDATE template_variable SET name_en = 'Product Name' WHERE code = 'productName' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Quantity' WHERE code = 'quantity' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Unit Price' WHERE code = 'unitPrice' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Total Price' WHERE code = 'totalPrice' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Delivery Days' WHERE code = 'deliveryDays' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Payment Days' WHERE code = 'paymentDays' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Penalty Rate (%)' WHERE code = 'penaltyRate' AND (name_en IS NULL OR TRIM(name_en) = '');

UPDATE template_variable SET name_en = 'Project Name' WHERE code = 'projectName' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Project Description' WHERE code = 'projectDesc' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Deliverables' WHERE code = 'deliverables' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Total Duration (Days)' WHERE code = 'totalDays' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Total Amount' WHERE code = 'totalAmount' AND (name_en IS NULL OR TRIM(name_en) = '');

UPDATE template_variable SET name_en = 'Property Address' WHERE code = 'address' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Floor Area (sqm)' WHERE code = 'area' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Lease Term (Months)' WHERE code = 'leaseMonths' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Monthly Rent' WHERE code = 'monthlyRent' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Security Deposit' WHERE code = 'deposit' AND (name_en IS NULL OR TRIM(name_en) = '');

UPDATE template_variable SET name_en = 'Employee Name' WHERE code = 'employeeName' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Company Name' WHERE code = 'companyName' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Position' WHERE code = 'position' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Monthly Salary' WHERE code = 'salary' AND (name_en IS NULL OR TRIM(name_en) = '');
UPDATE template_variable SET name_en = 'Probation Period (Months)' WHERE code = 'probationPeriod' AND (name_en IS NULL OR TRIM(name_en) = '');
