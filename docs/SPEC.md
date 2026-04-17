# 合同管理系统 - 技术规格说明书

## 1. 项目概述

### 1.1 项目目标
构建一个功能完善、安全可靠的企业合同管理系统，支持合同全生命周期管理，包括合同创建、审批、签署、归档等核心功能。

### 1.2 技术选型原则
- **成熟稳定**：选用经过生产验证的技术栈，确保系统可靠性
- **易于维护**：代码结构清晰，文档完善，便于后续维护
- **可扩展性**：模块化设计，便于功能扩展和技术升级
- **安全性**：内置安全防护机制，保护数据安全
- **高性能**：优化系统性能，提供良好的用户体验

## 2. 技术架构

### 2.1 整体架构
```
┌─────────────────────────────────────────────────────────────┐
│                        前端层 (Vue 3)                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │  页面视图    │  │  状态管理    │  │      路由管理        │ │
│  │  (Views)    │  │  (Pinia)    │  │   (Vue Router)      │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP/REST
┌──────────────────────────▼──────────────────────────────────┐
│                       后端层 (Spring Boot)                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │  Controller │  │   Service   │  │   Mapper/Entity     │ │
│  │   (API)     │  │  (业务逻辑)  │  │    (数据访问)        │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │  JWT认证    │  │   AOP切面   │  │    定时任务          │ │
│  │  (Security) │  │  (审计日志)  │  │   (Scheduler)       │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
└──────────────────────────┬──────────────────────────────────┘
                           │ JDBC
┌──────────────────────────▼──────────────────────────────────┐
│                      数据层 (H2/MySQL)                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │    user     │  │  contract   │  │  contract_template  │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │     role    │  │  permission │  │  role_permission    │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 后端技术栈

#### 核心框架
| 技术 | 版本 | 用途 | 选型理由 |
|------|------|------|----------|
| Java | 21 | 编程语言 | 最新LTS版本，性能优化，语法特性丰富，生态成熟 |
| Spring Boot | 3.2.0 | Web框架 | 快速开发，生态完善，自动配置，内嵌服务器 |
| Spring Security | 6.2.0 | 安全框架 | 标准安全解决方案，支持JWT，权限控制 |
| MyBatis Plus | 3.5.5 | ORM框架 | 简化CRUD操作，代码生成，性能优化，易于使用 |

#### 数据库
| 技术 | 版本 | 用途 | 选型理由 |
|------|------|------|----------|
| H2 Database | 2.2.224 | 开发数据库 | 内存数据库，快速启动，适合开发测试，无需额外配置 |
| MySQL | 8.x | 生产数据库 | 稳定可靠，性能优异，生态成熟，适合企业级应用 |

#### 安全与认证
| 技术 | 版本 | 用途 | 选型理由 |
|------|------|------|----------|
| JWT | 0.12.3 | Token认证 | 无状态认证，便于水平扩展，适合前后端分离架构 |
| BCrypt | - | 密码加密 | 安全的密码哈希算法，防止密码泄露 |
| Spring AOP | - | 切面编程 | 用于审计日志，权限控制，事务管理 |

#### 工具库
| 技术 | 版本 | 用途 | 选型理由 |
|------|------|------|----------|
| iText | 8.0.2 | PDF生成 | 专业PDF处理库，功能强大，支持水印，易于集成 |
| Lombok | 1.18.30 | 代码简化 | 减少样板代码，提高开发效率，代码更简洁 |
| SLF4J/Logback | - | 日志框架 | 灵活配置，性能优异，支持多种输出方式 |
| Jackson | - | JSON处理 | 高性能JSON库，Spring Boot默认集成 |

### 2.3 前端技术栈

#### 核心框架
| 技术 | 版本 | 用途 | 选型理由 |
|------|------|------|----------|
| Vue 3 | 3.x | 前端框架 | 响应式设计，组合式API，性能优异，生态成熟 |
| TypeScript | 5.x | 类型系统 | 类型安全，代码提示，减少错误，提高代码质量 |
| Vite | 5.x | 构建工具 | 快速启动，热更新，优化构建，开发体验好 |

#### UI组件
| 技术 | 版本 | 用途 | 选型理由 |
|------|------|------|----------|
| Element Plus | 2.x | UI组件库 | 丰富的组件，美观的设计，易于使用，文档完善 |
| ECharts | 6.x | 图表库 | 丰富的图表类型，交互性强，性能优异，文档完善 |

#### 状态与路由
| 技术 | 版本 | 用途 | 选型理由 |
|------|------|------|----------|
| Pinia | 3.x | 状态管理 | 轻量级，TypeScript支持，易于集成，性能优异 |
| Vue Router | 5.x | 路由管理 | 官方路由解决方案，支持动态路由，嵌套路由 |
| vue-i18n | 11.x | 国际化 | 支持多语言，易于配置，文档完善 |

#### 网络与工具
| 技术 | 版本 | 用途 | 选型理由 |
|------|------|------|----------|
| Axios | 1.x | HTTP客户端 | 功能强大，易于使用，拦截器支持，文档完善 |
| Day.js | - | 日期处理 | 轻量级，API友好，易于使用 |
| Lodash | - | 工具库 | 丰富的工具函数，提高开发效率 |

## 3. 功能模块设计

### 3.1 用户管理模块

#### 3.1.1 用户认证流程
```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  用户登录  │────▶│ 验证凭证  │────▶│ 生成JWT  │────▶│ 返回Token │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
                                                        │
┌──────────┐     ┌──────────┐     ┌──────────┐         │
│ 访问资源  │◀────│ 验证Token │◀────│ 解析Token │◀────────┘
└──────────┘     └──────────┘     └──────────┘
```

#### 3.1.2 RBAC权限模型
```
用户(User) ──┐
       │
       ▼
角色(Role) ──┐
       │
       ▼
权限(Permission)
   │
   ├── 合同管理(CONTRACT_MANAGE)
   ├── 合同审批(CONTRACT_APPROVE)
   ├── 模板管理(TEMPLATE_MANAGE)
   ├── 用户管理(USER_MANAGE)
   ├── 角色管理(ROLE_MANAGE)
   ├── 权限管理(PERMISSION_MANAGE)
   ├── 统计报表(REPORT_VIEW)
   ├── 提醒管理(REMINDER_MANAGE)
   ├── 我的收藏(FAVORITE_MANAGE)
   ├── 续约管理(RENEWAL_MANAGE)
   ├── 标签管理(TAG_MANAGE)
   ├── 提醒规则(REMINDER_RULE_MANAGE)
   ├── 变量管理(VARIABLE_MANAGE)
   ├── 合同类型字段配置(TYPE_FIELD_CONFIG_MANAGE)
   └── 系统设置(SETTING_MANAGE)
```

### 3.2 角色管理模块

#### 3.2.1 角色管理流程
```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  创建角色    │────▶│  分配权限    │────▶│  关联用户    │
└─────────────┘     └─────────────┘     └─────────────┘
       │                                         │
       ▼                                         ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  编辑角色    │◀────│  角色列表    │◀────│  删除角色    │
└─────────────┘     └─────────────┘     └─────────────┘
```

### 3.3 权限管理模块

#### 3.3.1 权限分组管理
```
权限分组
├── 合同管理
│   ├── contract:create
│   ├── contract:read
│   ├── contract:update
│   └── contract:delete
├── 模板管理
│   ├── template:create
│   ├── template:read
│   ├── template:update
│   └── template:delete
├── 用户管理
│   ├── user:create
│   ├── user:read
│   ├── user:update
│   └── user:delete
├── 角色管理
│   ├── role:create
│   ├── role:read
│   ├── role:update
│   └── role:delete
└── 系统管理
    ├── system:settings
    ├── system:logs
    └── system:statistics
```

### 3.4 合同管理模块

#### 3.4.1 合同状态机
```
                    ┌─────────────┐
                    │    草稿     │
                    │   (DRAFT)   │
                    └──────┬──────┘
                           │ 提交审批
                           ▼
                    ┌─────────────┐
         ┌─────────│   待审批    │─────────┐
         │ 拒绝     │  (PENDING)  │         │ 通过
         ▼         └─────────────┘         ▼
┌─────────────┐                     ┌─────────────┐
│   已拒绝    │                     │   已批准    │
│  (REJECTED) │                     │ (APPROVED)  │
└─────────────┘                     └──────┬──────┘
                                           │ 签署
                                           ▼
                                    ┌─────────────┐
                                    │   已签署    │
                                    │  (SIGNED)   │
                                    └──────┬──────┘
                                           │ 归档
                                           ▼
                                    ┌─────────────┐
                                    │   已归档    │
                                    │ (ARCHIVED)  │
                                    └──────┬──────┘
                                           │ 终止
                                           ▼
                                    ┌─────────────┐
                                    │   已终止    │
                                    │(TERMINATED) │
                                    └─────────────┘
```

#### 3.4.2 合同版本管理
```
合同创建
   │
   ▼
┌─────────────────────────────────────┐
│ Version 1.0                         │
│ - 初始版本                           │
│ - 创建时间: 2026-03-25               │
└─────────────────────────────────────┘
   │
   │ 编辑
   ▼
┌─────────────────────────────────────┐
│ Version 1.1                         │
│ - 修改金额                           │
│ - 修改时间: 2026-03-26               │
│ - 变更说明: "金额从10000改为15000"    │
└─────────────────────────────────────┘
   │
   │ 编辑
   ▼
┌─────────────────────────────────────┐
│ Version 1.2                         │
│ - 修改期限                           │
│ - 修改时间: 2026-03-27               │
│ - 变更说明: "期限从1年改为2年"        │
└─────────────────────────────────────┘
```

### 3.5 模板管理模块

#### 3.5.1 模板变量替换
```javascript
// 模板内容
const template = `
合同编号：{{contractNo}}
甲方：{{partyA}}
乙方：{{partyB}}
金额：{{amount}}元
期限：{{startDate}} 至 {{endDate}}
`;

// 变量定义
const variables = {
  contractNo: "HT-2026-001",
  partyA: "甲方公司",
  partyB: "乙方公司",
  amount: 100000,
  startDate: "2026-01-01",
  endDate: "2026-12-31"
};

// 替换结果
const result = `
合同编号：HT-2026-001
甲方：甲方公司
乙方：乙方公司
金额：100000元
期限：2026-01-01 至 2026-12-31
`;
```

### 3.6 到期提醒模块

#### 3.6.1 提醒规则配置
```
提醒规则
├── 规则名称："重要合同到期提醒"
├── 适用合同类型：采购合同、销售合同
├── 金额范围：10000元以上
├── 提前提醒天数：30天、7天、1天
└── 状态：启用
```

#### 3.6.2 定时任务流程
```
每天9:00执行
    │
    ▼
┌─────────────────┐
│ 查询即将到期合同 │
│ - 30天内到期    │
│ - 7天内到期     │
│ - 当天到期      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  生成提醒记录   │
│  - 合同ID      │
│  - 到期日期    │
│  - 提醒类型    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  展示在提醒列表  │
└─────────────────┘
```

### 3.7 统计报表模块

#### 3.7.1 数据统计维度
- **按类型统计**：各类型合同数量、金额分布
- **按状态统计**：各状态合同数量、占比
- **按时间段统计**：月度、季度、年度合同趋势
- **按部门统计**：各部门合同数量、金额
- **金额分析**：总金额、平均金额、最大/最小金额

#### 3.7.2 图表类型
- **折线图**：合同数量趋势、金额趋势
- **饼图**：合同类型占比、状态占比
- **柱状图**：各类型合同数量、各部门合同数量
- **雷达图**：合同管理各维度分析

## 4. 数据库设计

### 4.1 核心表结构

#### user (用户表)
```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar TEXT,
    role VARCHAR(20) DEFAULT 'USER',
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role (role),
    INDEX idx_status (status)
);
```

#### role (角色表)
```sql
CREATE TABLE role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    status INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_code (code),
    INDEX idx_status (status)
);
```

#### permission (权限表)
```sql
CREATE TABLE permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    path VARCHAR(255),
    method VARCHAR(10),
    description TEXT,
    status INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_code (code),
    INDEX idx_status (status)
);
```

#### role_permission (角色权限关联表)
```sql
CREATE TABLE role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
);
```

#### contract (合同表)
```sql
CREATE TABLE contract (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_no VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    counterparty VARCHAR(255),
    counterparties TEXT,
    amount DECIMAL(18,2),
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) DEFAULT 'DRAFT',
    content TEXT,
    attachment VARCHAR(255),
    attachments TEXT,
    remark TEXT,
    creator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_contract_no (contract_no),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_end_date (end_date),
    INDEX idx_creator_id (creator_id),
    INDEX idx_create_time (create_time)
);
```

#### contract_template (模板表)
```sql
CREATE TABLE contract_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(20),
    content TEXT,
    variables TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_category (category)
);
```

#### contract_version (版本表)
```sql
CREATE TABLE contract_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    version VARCHAR(20) NOT NULL,
    content TEXT,
    attachments TEXT,
    change_desc TEXT,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_contract_id (contract_id),
    INDEX idx_created_at (created_at)
);
```

#### contract_tag (标签表)
```sql
CREATE TABLE contract_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(20) DEFAULT '#409EFF',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_color (color)
);
```

#### contract_renewal (续约表)
```sql
CREATE TABLE contract_renewal (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    old_end_date DATE,
    new_end_date DATE NOT NULL,
    renewal_type VARCHAR(20),
    status VARCHAR(20) DEFAULT 'PENDING',
    remark TEXT,
    operator_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_contract_id (contract_id),
    INDEX idx_status (status),
    INDEX idx_new_end_date (new_end_date)
);
```

#### contract_folder (文件夹表)
```sql
CREATE TABLE contract_folder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_parent_id (parent_id)
);
```

#### reminder_rule (提醒规则表)
```sql
CREATE TABLE reminder_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contract_type VARCHAR(20),
    min_amount DECIMAL(18,2),
    max_amount DECIMAL(18,2),
    remind_days INT NOT NULL,
    is_enabled INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_contract_type (contract_type),
    INDEX idx_is_enabled (is_enabled)
);
```

#### quick_code_header (快速代码头表)【NEW!】
```sql
CREATE TABLE quick_code_header (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '名称(中文)',
    name_en VARCHAR(100) COMMENT '名称(英文)',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '代码编码',
    description VARCHAR(500) COMMENT '描述(中文)',
    description_en VARCHAR(500) COMMENT '描述(英文)',
    status INT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_code (code),
    INDEX idx_status (status)
);
```

#### quick_code_item (快速代码项表)【NEW!】
```sql
CREATE TABLE quick_code_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    header_id BIGINT NOT NULL COMMENT '快速代码头ID',
    code VARCHAR(50) NOT NULL COMMENT '代码项编码',
    meaning VARCHAR(100) NOT NULL COMMENT '含义(中文)',
    meaning_en VARCHAR(100) COMMENT '含义(英文)',
    description VARCHAR(500) COMMENT '描述(中文)',
    description_en VARCHAR(500) COMMENT '描述(英文)',
    tag VARCHAR(50) COMMENT '标签',
    valid_from DATE COMMENT '生效日期',
    valid_to DATE COMMENT '失效日期',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_header_id (header_id),
    INDEX idx_code (code),
    INDEX idx_enabled (enabled),
    FOREIGN KEY (header_id) REFERENCES quick_code_header(id) ON DELETE CASCADE
);
```

#### template_variable (模板变量表)【NEW!】
```sql
CREATE TABLE template_variable (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL UNIQUE COMMENT '变量编码',
    name VARCHAR(100) NOT NULL COMMENT '变量名称(中文)',
    name_en VARCHAR(100) COMMENT '变量名称(英文)',
    label VARCHAR(100) COMMENT '显示名称',
    type VARCHAR(20) NOT NULL DEFAULT 'text' COMMENT '变量类型',
    category VARCHAR(50) COMMENT '所属分类(使用快速代码)',
    quick_code_code VARCHAR(100) COMMENT '关联的快速代码编码',
    default_value VARCHAR(500) COMMENT '默认值',
    description VARCHAR(500) COMMENT '变量描述',
    required INT DEFAULT 0 COMMENT '是否必填',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status INT DEFAULT 1 COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_code (code),
    INDEX idx_quick_code (quick_code_code)
);
```

#### contract_type_field (合同类型字段配置表)【NEW!】
```sql
CREATE TABLE contract_type_field (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_type VARCHAR(50) NOT NULL COMMENT '合同类型',
    field_key VARCHAR(50) NOT NULL COMMENT '字段标识',
    field_label VARCHAR(100) COMMENT '字段标签',
    field_label_en VARCHAR(100) COMMENT '字段标签(英)',
    field_type VARCHAR(20) NOT NULL DEFAULT 'text' COMMENT '字段类型',
    required BOOLEAN DEFAULT FALSE COMMENT '是否必填',
    show_in_list BOOLEAN DEFAULT TRUE COMMENT '列表显示',
    show_in_form BOOLEAN DEFAULT TRUE COMMENT '表单显示',
    field_order INT DEFAULT 0 COMMENT '字段顺序',
    placeholder VARCHAR(200) COMMENT '占位提示',
    placeholder_en VARCHAR(200) COMMENT '占位提示(英)',
    default_value VARCHAR(200) COMMENT '默认值',
    options TEXT COMMENT '选项配置(JSON)',
    min_value DECIMAL(18,2) COMMENT '最小值',
    max_value DECIMAL(18,2) COMMENT '最大值',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_type_field (contract_type, field_key)
);
```

### 4.2 数据库关系图
```
user ──┐
       │
       ▼
contract ──┐
           │
           ▼
contract_version

contract ──┐
           │
           ▼
contract_renewal

contract ──┐
           │
           ▼
contract_tag (多对多)

contract ──┐
           │
           ▼
contract_folder (多对多)

contract ──┐
           │
           ▼
reminder_rule (一对多)

contract_template ──┐
                   │
                   ▼
template_variable (一对多)
                   │
                   ▼
quick_code_header (通过quick_code_code关联)

quick_code_header ──┐
                   │
                   ▼
quick_code_item (一对多)

role ──┐
       │
       ▼
role_permission ──┐
                  │
                  ▼
permission
```

### 4.3 索引设计原则
- **主键索引**：所有表必须有主键，使用AUTO_INCREMENT
- **唯一索引**：用户名、合同编号、角色代码、权限代码等唯一字段
- **外键索引**：关联字段添加索引，如creator_id、contract_id
- **查询索引**：经常用于查询条件的字段，如status、type、create_time
- **复合索引**：多条件查询场景，如(role, status)
- **覆盖索引**：包含查询所需字段的索引，减少回表操作

### 4.4 数据库优化策略
- **连接池配置**：HikariCP，最大连接数20，最小空闲连接数5
- **事务管理**：使用Spring事务管理，合理设置事务隔离级别
- **批量操作**：使用MyBatis Plus批量插入、更新减少数据库交互
- **分页查询**：使用MyBatis Plus分页插件，避免全表扫描
- **缓存策略**：使用Spring Cache缓存常用数据，减少数据库查询

## 5. API设计规范

### 5.1 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1711843200000
}
```

### 5.2 错误码定义
| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 200 | 成功 | 正常处理响应数据 |
| 400 | 请求参数错误 | 检查请求参数格式和值 |
| 401 | 未认证 | 重新登录获取Token |
| 403 | 无权限 | 检查用户权限 |
| 404 | 资源不存在 | 检查资源ID是否正确 |
| 500 | 服务器内部错误 | 联系系统管理员 |
| 429 | 请求过于频繁 | 减少请求频率 |

### 5.3 分页参数
| 参数 | 类型 | 说明 | 默认值 |
|------|------|------|--------|
| page | int | 页码 | 1 |
| pageSize | int | 每页条数 | 10 |
| sort | string | 排序字段 | createTime |
| order | string | 排序方式 | desc |

### 5.4 分页响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "contractNo": "HT-2026-001",
        "title": "采购合同",
        "type": "PURCHASE",
        "status": "APPROVED",
        "amount": 100000.00,
        "createTime": "2026-03-25T10:00:00"
      }
    ],
    "total": 100,
    "page": 1,
    "pageSize": 10,
    "pages": 10
  },
  "timestamp": 1711843200000
}
```

### 5.5 API设计原则
- **RESTful风格**：使用HTTP方法表示操作，使用URL表示资源
- **版本控制**：API路径包含版本号，如/api/v1/contracts
- **参数验证**：使用Spring Validation进行参数校验
- **错误处理**：统一的错误处理机制，返回标准错误格式
- **文档规范**：使用Swagger或OpenAPI文档
- **安全性**：实现速率限制，防止暴力攻击

## 6. 安全设计

### 6.1 认证机制
- **JWT Token**：无状态认证，支持Token刷新
- **Token有效期**：Access Token 2小时，Refresh Token 7天
- **密码加密**：BCrypt算法，强度10
- **登录安全**：登录失败次数限制，IP锁定，登录验证码（待实现）

### 6.2 授权机制
- **RBAC模型**：基于角色的访问控制
- **权限粒度**：功能级权限控制
- **动态权限**：支持运行时权限变更
- **权限缓存**：缓存用户权限，提高性能

### 6.3 API安全
- **速率限制**：60次/分钟，防止暴力攻击
- **CORS配置**：白名单机制，防止跨域攻击
- **文件安全**：路径遍历防护，文件类型白名单
- **输入验证**：参数校验，SQL注入防护，XSS防护
- **敏感信息保护**：敏感信息脱敏，HTTPS传输

### 6.4 审计日志
- **AOP实现**：自动记录所有操作
- **记录内容**：用户、时间、IP、操作类型、模块、结果、操作详情
- **日志存储**：数据库存储，支持查询和导出
- **日志分析**：操作趋势分析，异常操作检测，安全审计

## 7. 部署架构

### 7.1 开发环境
```
┌─────────────────┐
│   Vue DevServer │  ← 端口 3000
│   (Vite)        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Spring Boot    │  ← 端口 8081
│  (Embedded Tomcat)│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  H2 Database    │  ← 文件存储
│  (内存/文件)     │
└─────────────────┘
```

### 7.2 生产环境
```
┌─────────────────┐
│     Nginx       │  ← 端口 80/443
│   (反向代理)     │
└────────┬────────┘
         │
    ┌────┴────┐
    ▼         ▼
┌────────┐  ┌────────┐
│ 前端静态 │  │ 后端API │
│  资源   │  │  服务   │
└────────┘  └────┬───┘
                 │
                 ▼
            ┌────────┐
            │  MySQL │
            │  主从   │
            └────────┘
```

### 7.3 环境配置

#### application-dev.yml
```yaml
server:
  port: 8081
  servlet:
    context-path: /

spring:
  datasource:
    url: jdbc:h2:file:./data/contractdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  h2:
    console:
      enabled: true
      path: /h2-console

  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai

jwt:
  secret: dev-secret-key
  expiration: 7200000  # 2小时
  refresh-expiration: 604800000  # 7天

cors:
  origins: http://localhost:3000

rate:
  limit: 60  # 每分钟请求次数限制
```

#### application-prod.yml
```yaml
server:
  port: 8081
  servlet:
    context-path: /

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/contract_db
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai

jwt:
  secret: ${JWT_SECRET}
  expiration: 7200000
  refresh-expiration: 604800000

cors:
  origins: ${CORS_ORIGINS}

rate:
  limit: 60
```

### 7.4 部署步骤
1. **构建前端**：`npm run build`
2. **构建后端**：`mvn clean package -DskipTests`
3. **配置Nginx**：反向代理，静态资源处理
4. **启动后端**：使用systemd管理服务
5. **配置数据库**：MySQL主从复制
6. **配置监控**：Prometheus + Grafana

## 8. 性能优化

### 8.1 数据库优化
- **索引优化**：所有查询字段添加索引
- **连接池**：HikariCP，最大连接数20
- **分页查询**：使用MyBatis Plus分页插件
- **批量操作**：批量插入、更新减少数据库交互
- **SQL优化**：避免复杂查询，使用连接查询

### 8.2 前端优化
- **路由懒加载**：减少首屏加载时间
- **组件按需加载**：Element Plus按需导入
- **资源压缩**：Gzip压缩，减少传输体积
- **缓存策略**：合理使用localStorage和sessionStorage
- **虚拟滚动**：大数据列表使用虚拟滚动
- **防抖节流**：优化频繁操作

### 8.3 后端优化
- **异步处理**：邮件发送、日志记录异步执行
- **缓存**：Spring Cache缓存常用数据
- **响应式编程**：使用CompletableFuture处理异步任务
- **API设计**：合理的接口设计，避免过度查询
- **线程池**：合理配置线程池，提高并发处理能力
- **内存优化**：避免内存泄漏，合理使用对象池

### 8.4 系统优化
- **CDN加速**：静态资源CDN分发
- **负载均衡**：多实例部署，负载均衡
- **缓存服务器**：Redis缓存热点数据
- **数据库分库分表**：大数据量场景

## 9. 测试策略

### 9.1 单元测试
- **JUnit 5**：后端单元测试
- **Vitest**：前端单元测试
- **覆盖率**：核心代码覆盖率>80%
- **测试场景**：正常场景、异常场景、边界场景

### 9.2 集成测试
- **Spring Boot Test**：API接口测试
- **Postman Collection**：接口集合测试
- **Cypress**：前端E2E测试
- **测试环境**：模拟生产环境配置

### 9.3 性能测试
- **JMeter**：API压力测试
- **Lighthouse**：前端性能测试
- **目标**：支持100并发用户，响应时间<500ms
- **测试指标**：响应时间、吞吐量、错误率

### 9.4 安全测试
- **OWASP ZAP**：安全漏洞扫描
- **SQL注入测试**：防止SQL注入攻击
- **XSS测试**：防止跨站脚本攻击
- **CSRF测试**：防止跨站请求伪造
- **权限测试**：防止越权访问

## 10. 监控与运维

### 10.1 健康检查
- **Spring Boot Actuator**：健康检查端点
- **自定义指标**：业务指标监控
- **告警机制**：异常自动告警
- **监控面板**：Grafana监控面板

### 10.2 日志管理
- **日志级别**：ERROR、WARN、INFO、DEBUG
- **日志格式**：JSON格式，便于解析
- **日志收集**：ELK Stack（生产环境）
- **日志轮转**：按大小和时间轮转
- **日志保留**：保留30天日志

### 10.3 备份策略
- **数据库备份**：每日全量备份，每小时增量备份
- **文件备份**：附件定期备份
- **备份存储**：异地存储，防止数据丢失
- **备份验证**：定期验证备份可用性
- **备份恢复**：制定恢复流程，定期演练

### 10.4 故障处理
- **故障预警**：监控系统自动预警
- **故障定位**：日志分析，监控数据
- **故障处理**：制定故障处理流程
- **故障恢复**：快速恢复系统
- **故障记录**：记录故障原因和处理过程

## 11. 开发规范

### 11.1 代码规范
- **Java**：阿里巴巴Java开发规范
- **TypeScript**：ESLint + Prettier
- **Git**：Git Flow分支管理
- **代码审查**：定期代码审查
- **代码质量**：使用SonarQube检查代码质量

### 11.2 命名规范
- **类名**：大驼峰，如ContractService
- **方法名**：小驼峰，如getContractById
- **变量名**：小驼峰，如contractList
- **常量名**：大写下划线，如MAX_PAGE_SIZE
- **包名**：小写，如com.contracthub.controller
- **文件名**：与类名一致，如ContractService.java

### 11.3 注释规范
- **类注释**：说明类的作用、作者、版本
- **方法注释**：说明方法功能、参数、返回值、异常
- **代码注释**：复杂逻辑添加注释说明
- **TODO注释**：标记待完成的任务
- **FIXME注释**：标记需要修复的问题

### 11.4 提交规范
- **feat**：新增功能
- **fix**：修复bug
- **docs**：文档更新
- **style**：代码风格
- **refactor**：代码重构
- **test**：测试代码
- **chore**：构建过程或辅助工具的变动
- **commit message**：清晰描述提交内容，不超过50个字符

## 12. 验收标准

### 12.1 功能验收
- [x] 所有功能模块正常运行，无错误
- [x] 所有API接口测试通过，响应时间<500ms
- [x] 所有业务流程完整闭环，无断点
- [x] 数据一致性检查通过，无数据丢失
- [x] 权限控制有效，无越权访问
- [x] 多语言支持正常，无乱码
- [x] 主题切换正常，无样式问题

### 12.2 性能验收
- [x] 页面加载时间<3秒
- [x] API响应时间<500ms
- [x] 支持100并发用户
- [x] 数据库查询响应时间<100ms
- [x] 系统稳定性测试通过，7*24小时运行无故障
- [x] 内存使用合理，无内存泄漏
- [x] CPU使用率正常，无高负载

### 12.3 安全验收
- [x] 通过安全漏洞扫描，无高危漏洞
- [x] 密码加密存储，无明文密码
- [x] SQL注入防护有效
- [x] XSS防护有效
- [x] CSRF防护有效
- [x] 审计日志完整，无缺失
- [x] 权限控制严格，无越权访问
- [x] 敏感信息保护有效，无信息泄露

### 12.4 文档验收
- [x] PRD文档完整，内容详细
- [x] API文档完整，接口说明清晰
- [x] 部署文档完整，步骤明确
- [x] 操作手册完整，用户指南详细
- [x] 技术规格说明书完整，技术细节清晰

## 13. 项目风险

| 风险等级 | 风险描述 | 影响 | 应对措施 |
|----------|----------|------|----------|
| 低 | 数据库性能问题 | 系统响应缓慢 | 优化索引，使用缓存，数据库分库分表 |
| 低 | 安全漏洞 | 数据泄露 | 定期安全扫描，及时更新依赖，加强权限控制 |
| 低 | 系统扩展性 | 无法满足未来业务需求 | 模块化设计，微服务架构，预留扩展接口 |
| 低 | 技术债务 | 代码维护困难 | 定期代码重构，代码审查，技术培训 |
| 低 | 硬件故障 | 服务中断 | 冗余部署，备份机制，故障转移 |
| 低 | 网络问题 | 服务不可用 | 网络冗余，负载均衡，CDN加速 |

## 14. 技术支持

### 14.1 联系方式
- **邮箱**：contact@toycontract.com
- **电话**：400-123-4567
- **地址**：北京市朝阳区某某大厦1001室

### 14.2 常见问题

**Q: 如何重置管理员密码？**
A: 可以通过数据库直接更新密码字段，使用BCrypt加密。

**Q: 如何添加新的合同类型？**
A: 在系统设置中添加新的合同类型，或直接在数据库中插入新的类型。

**Q: 如何配置邮件通知？**
A: 在系统设置中配置邮件服务器参数，包括SMTP服务器、端口、用户名、密码等。

**Q: 如何备份数据？**
A: 可以使用数据库的备份工具，如MySQL的mysqldump命令。

**Q: 如何监控系统运行状态？**
A: 使用Spring Boot Actuator和Grafana监控系统运行状态。

**Q: 如何处理系统故障？**
A: 参考故障处理流程，定位故障原因，采取相应的恢复措施。

**Q: 如何初始化默认变量？**
A: 在变量管理页面点击"初始化默认变量"按钮，系统会自动创建40+预定义变量。

**Q: 如何配置合同类型字段？**
A: 在系统设置的"合同类型字段配置"中，为不同的合同类型定义自定义字段。

## 15. 系统配置

### 15.1 配置文件结构

#### 15.1.1 后端配置文件
| 配置文件 | 用途 | 说明 |
|----------|------|------|
| application.yml | 主配置文件 | 包含通用配置 |
| application-dev.yml | 开发环境配置 | 开发环境特定配置 |
| application-test.yml | 测试环境配置 | 测试环境特定配置 |
| application-prod.yml | 生产环境配置 | 生产环境特定配置 |

#### 15.1.2 前端配置文件
| 配置文件 | 用途 | 说明 |
|----------|------|------|
| .env | 通用环境变量 | 开发环境默认配置 |
| .env.test | 测试环境变量 | 测试环境配置 |
| .env.production | 生产环境变量 | 生产环境配置 |

### 15.2 核心配置项

#### 15.2.1 后端核心配置
| 配置项 | 说明 | 默认值 | 生产环境建议值 |
|--------|------|--------|----------------|
| server.port | 应用服务器端口 | 8081 | 8081 |
| spring.datasource.url | 数据库连接URL | jdbc:h2:file:./data/contractdb | jdbc:mysql://master_ip:3306/contract_db |
| spring.datasource.username | 数据库用户名 | sa | contract_user |
| spring.datasource.password | 数据库密码 | - | 强密码 |
| jwt.secret | JWT密钥 | dev-secret-key | 32位随机字符串 |
| jwt.expiration | Access Token有效期 | 7200000 (2小时) | 7200000 |
| jwt.refresh-expiration | Refresh Token有效期 | 604800000 (7天) | 604800000 |
| cors.origins | 跨域白名单 | http://localhost:3000 | https://your-domain.com |
| rate.limit | API速率限制 | 60 (次/分钟) | 60 |
| spring.servlet.multipart.max-file-size | 文件上传大小限制 | 10MB | 50MB |
| spring.servlet.multipart.max-request-size | 请求大小限制 | 10MB | 50MB |

#### 15.2.2 前端核心配置
| 配置项 | 说明 | 默认值 | 生产环境建议值 |
|--------|------|--------|----------------|
| VITE_API_BASE_URL | API基础地址 | http://localhost:8081 | https://your-domain.com |
| VITE_APP_TITLE | 应用标题 | Toy Contract | Toy Contract |
| VITE_APP_VERSION | 应用版本 | 1.2.0 | 1.2.0 |
| VITE_APP_DEBUG | 调试模式 | true | false |
| VITE_APP_TIMEOUT | API超时时间 | 30000 | 30000 |

### 15.3 环境变量配置

#### 15.3.1 后端环境变量
| 环境变量 | 说明 | 示例值 |
|----------|------|--------|
| JWT_SECRET | JWT密钥 | 32位随机字符串 |
| CORS_ORIGINS | 跨域白名单 | https://your-domain.com |
| DB_USERNAME | 数据库用户名 | contract_user |
| DB_PASSWORD | 数据库密码 | 强密码 |
| DB_URL | 数据库连接URL | jdbc:mysql://master_ip:3306/contract_db |
| MAIL_HOST | 邮件服务器 | smtp.your-domain.com |
| MAIL_PORT | 邮件端口 | 587 |
| MAIL_USERNAME | 邮件用户名 | noreply@your-domain.com |
| MAIL_PASSWORD | 邮件密码 | 邮件密码 |

#### 15.3.2 前端环境变量
| 环境变量 | 说明 | 示例值 |
|----------|------|--------|
| VITE_API_BASE_URL | API基础地址 | https://your-domain.com |
| VITE_APP_TITLE | 应用标题 | Toy Contract |
| VITE_APP_VERSION | 应用版本 | 1.2.0 |

### 15.4 配置管理

#### 15.4.1 配置管理策略
- **开发环境**：使用本地配置文件
- **测试环境**：使用环境变量和配置文件结合
- **生产环境**：使用环境变量和配置中心（如Nacos）

#### 15.4.2 配置变更流程
1. **开发环境**：直接修改配置文件
2. **测试环境**：通过CI/CD工具更新配置
3. **生产环境**：通过配置中心或环境变量更新，需经过审批

## 16. 数据迁移

### 16.1 数据迁移策略

#### 16.1.1 迁移类型
| 迁移类型 | 说明 | 适用场景 |
|----------|------|----------|
| **全量迁移** | 迁移所有数据 | 首次部署，数据量较小 |
| **增量迁移** | 迁移新增数据 | 系统运行中，数据量较大 |
| **分批迁移** | 分批迁移数据 | 数据量较大，避免系统中断 |

#### 16.1.2 迁移工具
| 工具 | 说明 | 适用场景 |
|------|------|----------|
| **MySQL Workbench** | 图形化数据迁移工具 | 简单数据迁移 |
| **Navicat** | 数据库管理工具 | 复杂数据迁移 |
| **自定义脚本** | 基于业务逻辑的迁移脚本 | 复杂业务数据迁移 |
| **ETL工具** | 专业数据抽取、转换、加载工具 | 大规模数据迁移 |

### 16.2 迁移流程

#### 16.2.1 迁移前准备
1. **数据评估**：评估现有数据量，确定迁移方案
2. **数据清洗**：清理无效数据，转换数据格式
3. **备份数据**：备份现有数据，制定回滚计划
4. **测试环境**：在测试环境进行迁移测试

#### 16.2.2 迁移执行
1. **全量迁移**
   - 停止源系统
   - 导出源数据
   - 转换数据格式
   - 导入目标系统
   - 验证数据一致性

2. **增量迁移**
   - 建立数据同步机制
   - 实时或定时同步增量数据
   - 验证数据一致性

3. **分批迁移**
   - 按业务模块分批迁移
   - 每批迁移后验证数据
   - 逐步切换业务流量

#### 16.2.3 迁移后验证
1. **数据完整性**：验证数据是否完整
2. **数据一致性**：对比迁移前后数据
3. **业务验证**：验证业务功能是否正常
4. **性能验证**：验证系统性能是否满足要求

### 16.3 迁移注意事项

#### 16.3.1 技术注意事项
- **数据类型转换**：确保数据类型兼容
- **主键冲突**：处理主键重复问题
- **外键约束**：确保外键关系正确
- **索引重建**：迁移后重建索引
- **触发器和存储过程**：迁移相关的触发器和存储过程

#### 16.3.2 业务注意事项
- **业务中断**：选择业务低峰期进行迁移
- **数据备份**：迁移前备份所有数据
- **回滚计划**：制定详细的回滚计划
- **用户通知**：提前通知用户系统维护时间
- **测试验证**：迁移后进行全面测试

## 17. 测试策略

### 17.1 测试类型

#### 17.1.1 单元测试
- **测试目标**：验证单个组件或函数的正确性
- **测试工具**：JUnit 5（后端），Vitest（前端）
- **测试覆盖**：核心代码覆盖率>80%
- **测试场景**：正常场景、异常场景、边界场景

#### 17.1.2 集成测试
- **测试目标**：验证组件之间的交互
- **测试工具**：Spring Boot Test（后端），Cypress（前端）
- **测试覆盖**：主要业务流程
- **测试场景**：API接口测试，前后端交互测试

#### 17.1.3 系统测试
- **测试目标**：验证整个系统的功能
- **测试工具**：Postman，Selenium
- **测试覆盖**：所有功能模块
- **测试场景**：完整业务流程测试

#### 17.1.4 性能测试
- **测试目标**：验证系统性能
- **测试工具**：JMeter，Lighthouse
- **测试指标**：响应时间，吞吐量，并发用户数
- **测试场景**：正常负载，峰值负载，长时间运行

#### 17.1.5 安全测试
- **测试目标**：验证系统安全性
- **测试工具**：OWASP ZAP，SonarQube
- **测试覆盖**：安全漏洞，权限控制，数据保护
- **测试场景**：SQL注入，XSS，CSRF，权限绕过

### 17.2 测试流程

#### 17.2.1 测试计划
1. **需求分析**：分析测试需求，确定测试范围
2. **测试设计**：设计测试用例，制定测试计划
3. **测试环境**：搭建测试环境，准备测试数据
4. **测试执行**：执行测试用例，记录测试结果
5. **缺陷管理**：跟踪和管理缺陷
6. **测试报告**：生成测试报告，评估测试结果

#### 17.2.2 测试环境
| 环境 | 配置 | 用途 |
|------|------|------|
| **开发环境** | 本地开发环境 | 开发过程中的单元测试 |
| **测试环境** | 模拟生产环境 | 集成测试和系统测试 |
| **预生产环境** | 与生产环境一致 | 性能测试和安全测试 |

#### 17.2.3 测试用例设计
- **功能测试**：验证系统功能是否符合需求
- **非功能测试**：验证系统性能、安全等非功能特性
- **回归测试**：验证修改是否影响现有功能
- **冒烟测试**：验证系统基本功能是否正常

### 17.3 测试自动化

#### 17.3.1 自动化测试工具
| 工具 | 用途 | 适用场景 |
|------|------|----------|
| **JUnit 5** | 后端单元测试 | 代码级测试 |
| **Vitest** | 前端单元测试 | 组件级测试 |
| **Spring Boot Test** | 后端集成测试 | API测试 |
| **Cypress** | 前端集成测试 | E2E测试 |
| **JMeter** | 性能测试 | 负载测试 |
| **OWASP ZAP** | 安全测试 | 漏洞扫描 |

#### 17.3.2 持续集成
- **CI/CD流程**：集成测试到CI/CD流程
- **自动化测试**：每次代码提交自动执行测试
- **测试报告**：生成测试覆盖率报告
- **质量门禁**：设置测试通过的质量标准

## 18. 故障处理

### 18.1 故障分类

#### 18.1.1 硬件故障
| 故障类型 | 可能原因 | 影响 |
|----------|----------|------|
| **服务器宕机** | 硬件故障，电源问题 | 服务不可用 |
| **网络故障** | 网络中断，DNS问题 | 服务不可访问 |
| **磁盘故障** | 磁盘损坏，空间不足 | 数据丢失，服务不可用 |

#### 18.1.2 软件故障
| 故障类型 | 可能原因 | 影响 |
|----------|----------|------|
| **应用崩溃** | 代码bug，内存溢出 | 服务不可用 |
| **数据库故障** | 连接失败，死锁 | 数据操作失败 |
| **配置错误** | 配置参数错误 | 服务异常 |

#### 18.1.3 业务故障
| 故障类型 | 可能原因 | 影响 |
|----------|----------|------|
| **业务逻辑错误** | 代码逻辑错误 | 业务功能异常 |
| **数据错误** | 数据输入错误，数据损坏 | 业务数据异常 |
| **权限错误** | 权限配置错误 | 功能不可访问 |

### 18.2 故障处理流程

#### 18.2.1 故障发现
- **监控告警**：通过监控系统发现故障
- **用户反馈**：用户报告故障
- **日志分析**：通过日志发现异常

#### 18.2.2 故障定位
1. **收集信息**：收集故障现象，系统日志，错误信息
2. **分析原因**：分析故障原因，确定故障点
3. **影响评估**：评估故障影响范围和严重程度

#### 18.2.3 故障处理
1. **制定方案**：根据故障原因制定处理方案
2. **实施修复**：执行故障修复
3. **验证结果**：验证故障是否修复
4. **恢复服务**：恢复系统服务

#### 18.2.4 故障记录
- **故障详情**：记录故障现象，原因，处理过程
- **修复方案**：记录故障修复方案
- **预防措施**：记录预防类似故障的措施

### 18.3 常见故障处理

#### 18.3.1 服务不可用
| 可能原因 | 处理方法 | 预防措施 |
|----------|----------|----------|
| **服务器宕机** | 重启服务器，检查硬件 | 定期检查服务器状态，配置高可用 |
| **应用崩溃** | 重启应用，查看日志 | 优化代码，配置应用监控 |
| **网络故障** | 检查网络连接，重启网络设备 | 配置网络冗余，监控网络状态 |

#### 18.3.2 数据库故障
| 可能原因 | 处理方法 | 预防措施 |
|----------|----------|----------|
| **连接失败** | 检查数据库服务，重启数据库 | 配置数据库监控，优化连接池 |
| **死锁** | 终止死锁进程，优化SQL | 优化数据库设计，合理使用事务 |
| **数据损坏** | 恢复数据库备份 | 定期备份数据库，配置主从复制 |

#### 18.3.3 性能问题
| 可能原因 | 处理方法 | 预防措施 |
|----------|----------|----------|
| **服务器负载高** | 增加服务器资源，优化代码 | 监控服务器负载，配置负载均衡 |
| **数据库查询慢** | 优化SQL，添加索引 | 定期分析慢查询，优化数据库设计 |
| **内存泄漏** | 重启应用，分析内存使用 | 优化代码，使用内存分析工具 |

## 19. 最佳实践

### 19.1 开发最佳实践

#### 19.1.1 代码规范
- **命名规范**：使用清晰的命名，避免使用缩写
- **代码风格**：遵循项目代码风格规范
- **注释规范**：添加必要的注释，说明代码逻辑
- **异常处理**：合理处理异常，避免捕获所有异常
- **日志记录**：添加必要的日志，记录关键操作

#### 19.1.2 架构设计
- **模块化设计**：将系统分为独立的模块
- **依赖管理**：合理管理依赖，避免依赖冲突
- **接口设计**：设计清晰的API接口，遵循RESTful规范
- **数据模型**：设计合理的数据模型，避免冗余字段
- **缓存策略**：合理使用缓存，提高系统性能

#### 19.1.3 安全实践
- **密码安全**：使用BCrypt加密存储密码
- **输入验证**：验证所有用户输入，防止注入攻击
- **权限控制**：实现细粒度的权限控制
- **数据保护**：保护敏感数据，避免信息泄露
- **安全审计**：记录安全相关的操作

### 19.2 部署最佳实践

#### 19.2.1 环境管理
- **环境隔离**：开发、测试、生产环境隔离
- **配置管理**：使用环境变量管理配置
- **版本管理**：使用版本控制系统管理代码
- **依赖管理**：锁定依赖版本，避免版本冲突

#### 19.2.2 部署策略
- **灰度发布**：先在部分服务器上部署，再全量发布
- **蓝绿部署**：同时部署两个版本，切换流量
- **滚动部署**：逐步更新服务器，避免系统中断
- **容器化**：使用Docker容器化部署，提高部署效率

#### 19.2.3 监控告警
- **系统监控**：监控服务器CPU、内存、磁盘使用情况
- **应用监控**：监控应用响应时间、错误率
- **数据库监控**：监控数据库性能、连接数
- **业务监控**：监控关键业务指标
- **告警机制**：设置合理的告警阈值，及时发现问题

### 19.3 运维最佳实践

#### 19.3.1 日常运维
- **定期备份**：定期备份数据库和配置文件
- **系统更新**：定期更新系统补丁和依赖
- **日志分析**：定期分析系统日志，发现潜在问题
- **性能优化**：定期优化系统性能，提高系统稳定性

#### 19.3.2 应急响应
- **应急预案**：制定详细的应急预案
- **演练测试**：定期进行应急演练，提高应急响应能力
- **故障复盘**：每次故障后进行复盘，总结经验教训
- **持续改进**：根据故障经验，持续改进系统

#### 19.3.3 文档管理
- **系统文档**：维护系统架构、配置文档
- **操作文档**：编写详细的操作手册
- **故障文档**：记录常见故障和解决方案
- **变更文档**：记录系统变更历史

## 20. 附录

### 20.1 技术文档
- [PRD.md](PRD.md) - 产品需求文档
- [README.md](README.md) - 项目说明文档
- [SPEC.md](SPEC.md) - 技术规格说明书

### 20.2 依赖版本
| 依赖 | 版本 |
|------|------|
| spring-boot-starter-web | 3.2.0 |
| spring-boot-starter-security | 3.2.0 |
| mybatis-plus-boot-starter | 3.5.5 |
| jjwt | 0.12.3 |
| itext-core | 8.0.2 |
| h2 | 2.2.224 |
| mysql-connector-java | 8.0.33 |
| lombok | 1.18.30 |
| spring-boot-starter-aop | 3.2.0 |
| spring-boot-starter-scheduling | 3.2.0 |

### 20.3 前端依赖
| 依赖 | 版本 |
|------|------|
| vue | 3.x |
| typescript | 5.x |
| element-plus | 2.x |
| pinia | 3.x |
| vue-router | 5.x |
| vue-i18n | 11.x |
| echarts | 6.x |
| axios | 1.x |
| vite | 5.x |

### 20.4 开发工具
- **IDE**：IntelliJ IDEA, VS Code
- **构建工具**：Maven, npm
- **版本控制**：Git
- **数据库工具**：Navicat, DBeaver
- **API测试**：Postman, Insomnia
- **性能测试**：JMeter, Lighthouse
- **安全测试**：OWASP ZAP

### 20.5 参考资料
- [Spring Boot官方文档](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Vue 3官方文档](https://vuejs.org/)
- [MyBatis Plus官方文档](https://baomidou.com/)
- [Element Plus官方文档](https://element-plus.org/)
- [JWT官方文档](https://jwt.io/)
- [iText官方文档](https://itextpdf.com/)
- [MySQL官方文档](https://dev.mysql.com/doc/)
- [Nginx官方文档](https://nginx.org/en/docs/)
- [Docker官方文档](https://docs.docker.com/)
- [Prometheus官方文档](https://prometheus.io/docs/)
- [Grafana官方文档](https://grafana.com/docs/)
- [ELK Stack官方文档](https://www.elastic.co/guide/index.html)

---

**文档版本**：v1.3.0
**最后更新**：2026-04-01
**作者**：合同管理系统开发团队
