# 数据库文档

## 概述

合同管理系统使用MySQL数据库，通过Flyway进行数据库版本管理和迁移。

## 数据库版本

- **数据库类型**: MySQL 8.0+
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci

## Flyway 迁移脚本

Flyway迁移脚本位于 `backend/src/main/resources/db/migration/` 目录下。

### 迁移脚本列表（与仓库一致）

> 说明：历史文档中的 `V1.0.x` 命名已合并进当前主线；新环境以 `V1__init_schema.sql` + 下列增量脚本为准。

| 版本 | 文件名 | 说明 |
|------|--------|------|
| V1 | V1__init_schema.sql | 初始化全量基线（含后续演进中已并入的核心表，具体以文件为准） |
| V1.1.0 | V1.1.0__Add_performance_indexes.sql | 性能相关索引 |
| V1.2.0 | V1.2.0__Add_quick_code_to_template_variable.sql | 模板变量与快速代码 |
| V1.3.0 | V1.3.0__Add_name_en_to_template_variable.sql | 模板变量英文名 |
| V1.4.0 | V1.4.0__Add_quick_code_id_to_contract_type_field.sql | 合同类型字段关联快速代码 |
| V1.5.0 | V1.5.0__Add_contract_clause_library.sql | 条款库 |
| V1.5.0 | V1.5.0__Add_contract_relation_for_supplement.sql | 主合同/补充协议关系 |
| V1.6.0 | V1.6.0__Add_diff_json_to_contract_change_log.sql | 变更记录 diff JSON |
| V1.7.0 | V1.7.0__Deprecate_contract_counterparty_column.sql | 相对方字段演进 |
| V1.8.0 | V1.8.0__Add_user_department.sql | 用户部门等 |
| V1.9.0 | V1.9.0__Extract_approval_snapshot_to_dedicated_table.sql | 审批快照独立表 `contract_snapshot` |
| V1.9.1 | V1.9.1__Backfill_contract_snapshot_from_legacy_columns.sql | 快照数据回填 |
| V1.10.0 | V1.10.0__Split_contract_payload_from_contract.sql | 大字段拆分至 `contract_payload` |
| V1.11.0 | V1.11.0__Add_contract_ai_summary.sql | 审批摘要等 AI 结果表 |
| V1.12.0 | V1.12.0__Add_contract_version_diff_analysis.sql | 版本对比分析缓存 |
| V1.13.0 | V1.13.0__Add_contract_snapshot_diff_analysis.sql | 快照对比分析缓存 |
| V1.14.0 | V1.14.0__Ensure_diff_analysis_schema_compat.sql | 对比分析表/索引幂等补齐 |

同步参考：`backend/src/main/resources/schema.sql`（开发/文档用全量结构说明）。

### 执行迁移

启动后端应用时，Flyway会自动执行迁移脚本。

也可以手动执行：
```bash
cd backend
mvn flyway:migrate
```

## 数据库表结构

### 大字段与快照（重要）

- **`contract_payload`**：与 `contract.id` 一对一（业务上按合同 ID 关联），承载 `content`、`template_variables`、`dynamic_field_values`、`attachments`、内容哈希等，避免主表行过大。
- **`contract_snapshot`**：审批/关键节点冻结的正文与变量快照，用于审计与「快照对比」。
- **`contract_ai_summary`**：审批摘要等生成结果，可与 `snapshot_id` 关联。
- **`contract_version_diff_analysis` / `contract_snapshot_diff_analysis`**：对比结果缓存（JSON），减少重复计算。

### 核心表

#### user (用户表)
存储系统用户信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名 |
| password | VARCHAR(255) | 密码（加密存储） |
| nickname | VARCHAR(100) | 昵称 |
| email | VARCHAR(100) | 邮箱 |
| phone | VARCHAR(20) | 手机号 |
| avatar | VARCHAR(255) | 头像URL |
| status | INT | 状态：1-启用，0-禁用 |
| role_id | BIGINT | 角色ID |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

---

#### role (角色表)
存储用户角色信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(100) | 角色名称 |
| name_en | VARCHAR(100) | 角色英文名称 |
| description | VARCHAR(500) | 角色描述 |
| description_en | VARCHAR(500) | 角色英文描述 |
| status | INT | 状态：1-启用，0-禁用 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

---

#### permission (权限表)
存储系统权限信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| code | VARCHAR(50) | 权限编码 |
| name | VARCHAR(100) | 权限名称 |
| name_en | VARCHAR(100) | 权限英文名称 |
| module | VARCHAR(50) | 模块 |
| description | VARCHAR(500) | 权限描述 |
| description_en | VARCHAR(500) | 权限英文描述 |
| status | INT | 状态：1-启用，0-禁用 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

---

#### role_permission (角色权限关联表)
角色与权限的多对多关联。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| role_id | BIGINT | 角色ID |
| permission_id | BIGINT | 权限ID |
| created_at | TIMESTAMP | 创建时间 |

---

#### contract (合同表)
存储合同主信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| contract_no | VARCHAR(50) | 合同编号 |
| title | VARCHAR(200) | 合同标题 |
| type | VARCHAR(50) | 合同类型 |
| party_a | VARCHAR(200) | 甲方 |
| party_b | VARCHAR(200) | 乙方 |
| amount | DECIMAL(18,2) | 合同金额 |
| start_date | DATE | 开始日期 |
| end_date | DATE | 结束日期 |
| status | VARCHAR(20) | 合同状态 |
| content | TEXT | 合同内容 |
| template_id | BIGINT | 模板ID |
| folder_id | BIGINT | 文件夹ID |
| created_by | BIGINT | 创建人ID |
| updated_by | BIGINT | 更新人ID |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

---

#### contract_version (合同版本表)
存储合同版本历史。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| contract_id | BIGINT | 合同ID |
| version | INT | 版本号 |
| content | TEXT | 版本内容 |
| created_by | BIGINT | 创建人ID |
| created_at | TIMESTAMP | 创建时间 |

---

#### contract_renewal (合同续约表)
存储合同续约记录。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| contract_id | BIGINT | 合同ID |
| original_end_date | DATE | 原结束日期 |
| new_end_date | DATE | 新结束日期 |
| renewal_type | VARCHAR(20) | 续约类型 |
| remark | VARCHAR(500) | 备注 |
| status | VARCHAR(20) | 状态 |
| created_by | BIGINT | 创建人ID |
| created_at | TIMESTAMP | 创建时间 |

---

#### contract_template (合同模板表)
存储合同模板。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(200) | 模板名称 |
| type | VARCHAR(50) | 模板类型 |
| content | TEXT | 模板内容 |
| variables | TEXT | 模板变量JSON |
| description | VARCHAR(500) | 描述 |
| status | INT | 状态：1-启用，0-禁用 |
| created_by | BIGINT | 创建人ID |
| updated_by | BIGINT | 更新人ID |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

---

#### template_variable (模板变量表)
存储模板变量定义。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| code | VARCHAR(100) | 变量编码 |
| name | VARCHAR(100) | 变量名称(中文) |
| name_en | VARCHAR(100) | 变量名称(英文) |
| label | VARCHAR(100) | 显示名称 |
| type | VARCHAR(20) | 变量类型 |
| category | VARCHAR(50) | 所属分类 |
| quick_code_code | VARCHAR(100) | 关联的快速代码编码 |
| default_value | VARCHAR(500) | 默认值 |
| description | VARCHAR(500) | 变量描述 |
| required | INT | 是否必填 |
| sort_order | INT | 排序 |
| status | INT | 状态 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

---

#### quick_code_header (快速代码头表)
存储快速代码分类。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(100) | 名称(中文) |
| name_en | VARCHAR(100) | 名称(英文) |
| code | VARCHAR(50) | 代码编码 |
| description | VARCHAR(500) | 描述(中文) |
| description_en | VARCHAR(500) | 描述(英文) |
| status | INT | 状态：1-启用，0-禁用 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

---

#### quick_code_item (快速代码项表)
存储快速代码具体项。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| header_id | BIGINT | 快速代码头ID |
| code | VARCHAR(50) | 代码项编码 |
| meaning | VARCHAR(100) | 含义(中文) |
| meaning_en | VARCHAR(100) | 含义(英文) |
| description | VARCHAR(500) | 描述(中文) |
| description_en | VARCHAR(500) | 描述(英文) |
| tag | VARCHAR(50) | 标签 |
| valid_from | DATE | 生效日期 |
| valid_to | DATE | 失效日期 |
| enabled | BOOLEAN | 是否启用 |
| sort_order | INT | 排序 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

---

#### reminder_rule (提醒规则表)
存储到期提醒规则。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(100) | 规则名称 |
| contract_types | VARCHAR(500) | 适用合同类型 |
| min_amount | DECIMAL(18,2) | 最小金额 |
| max_amount | DECIMAL(18,2) | 最大金额 |
| advance_days | INT | 提前天数 |
| status | INT | 状态：1-启用，0-禁用 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

---

#### folder (文件夹表)
存储文件夹信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(100) | 文件夹名称 |
| parent_id | BIGINT | 上级文件夹ID |
| color | VARCHAR(20) | 文件夹颜色 |
| description | VARCHAR(500) | 描述 |
| created_by | BIGINT | 创建人ID |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

---

#### tag (标签表)
存储标签信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(100) | 标签名称 |
| color | VARCHAR(20) | 标签颜色 |
| description | VARCHAR(500) | 描述 |
| created_by | BIGINT | 创建人ID |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

---

#### contract_tag (合同标签关联表)
合同与标签的多对多关联。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| contract_id | BIGINT | 合同ID |
| tag_id | BIGINT | 标签ID |
| created_at | TIMESTAMP | 创建时间 |

---

#### contract_folder (合同文件夹关联表)
合同与文件夹的多对多关联。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| contract_id | BIGINT | 合同ID |
| folder_id | BIGINT | 文件夹ID |
| created_at | TIMESTAMP | 创建时间 |

---

#### operation_log (操作日志表)
存储系统操作日志。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 操作人ID |
| module | VARCHAR(50) | 操作模块 |
| operation | VARCHAR(100) | 操作内容 |
| ip_address | VARCHAR(50) | IP地址 |
| created_at | TIMESTAMP | 创建时间 |

---

## 初始化数据

### 默认用户

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | admin123 | 管理员 | 系统管理员 |
| legal | legal123 | 法务 | 法务人员 |
| user | user123 | 普通用户 | 普通用户 |

### 默认角色

| 角色 | 说明 |
|------|------|
| 管理员 | 拥有全部权限 |
| 法务 | 合同管理、审批、模板、统计权限 |
| 普通用户 | 合同查看、创建、收藏权限 |

### 默认快速代码

#### TEMPLATE_VARIABLE_CATEGORY (模板变量分类)

| 代码 | 含义(中文) | 含义(英文) |
|------|-----------|-----------|
| system | 系统变量 | System Variables |
| party | 相对方信息 | Party Information |
| procurement | 采购合同 | Procurement Contract |
| service | 服务合同 | Service Contract |
| lease | 租赁合同 | Lease Contract |
| labor | 劳动合同 | Labor Contract |
| custom | 自定义变量 | Custom Variables |

---

## 数据库配置

### application.yml 配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/contract_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 0
```

### 本地开发环境设置

1. 创建数据库：
```sql
CREATE DATABASE contract_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改 `backend/src/main/resources/application-local.yml` 中的数据库连接信息

3. 启动后端应用，Flyway会自动执行迁移脚本

---

## 备份与恢复

### 备份数据库

```bash
mysqldump -u root -p contract_management > backup_$(date +%Y%m%d).sql
```

### 恢复数据库

```bash
mysql -u root -p contract_management < backup_20260411.sql
```

---

## 常见问题

### Q: Flyway迁移失败怎么办？
A: 检查数据库连接配置，确认Flyway脚本版本号正确，删除flyway_schema_history表中失败的记录重试。

### Q: 如何重置数据库？
A: 删除数据库后重新创建，然后重启后端应用，Flyway会自动重新执行所有迁移。

### Q: 快速代码如何使用？
A: 在系统管理 -> 快速代码管理中创建快速代码头和代码项，其他模块可以通过代码编码引用。

---

*文档最后更新：2026-04-11*
