# 合同版本记录功能修复说明文档

## 一、问题原因分析

### 1.1 版本号生成规则错误
- **问题**：原系统使用硬编码的模拟数据（v1.0, v1.1, v1.2），没有实现自动递增的版本号生成逻辑
- **影响**：无法保证版本号的唯一性和有序性

### 1.2 版本历史记录不完整
- **问题**：后端仅返回静态模拟数据，未实际存储版本信息到数据库
- **影响**：版本历史数据丢失，无法追溯合同变更历史

### 1.3 版本详情API缺失
- **问题**：`getVersionDetail` API 未实现，无法获取指定版本的详细内容
- **影响**：用户无法查看历史版本的具体内容

### 1.4 版本回滚机制失效
- **问题**：`restoreVersion` API 未实现，没有版本恢复的业务逻辑
- **影响**：无法恢复到历史版本

### 1.5 版本对比功能缺失
- **问题**：前端没有版本对比功能，无法显示版本间差异
- **影响**：用户无法直观了解版本变更内容

### 1.6 权限控制逻辑漏洞
- **问题**：缺少版本操作的权限控制，任何用户都可以执行恢复操作
- **影响**：存在安全风险

## 二、解决方案

### 2.1 后端修复

#### 2.1.1 创建版本管理控制器
- 文件：`ContractVersionController.java`
- 功能：
  - 获取版本历史列表
  - 获取版本详情
  - 恢复指定版本
  - 对比两个版本

#### 2.1.2 实现版本服务层
- 文件：`ContractVersionService.java` / `ContractVersionServiceImpl.java`
- 核心功能：
  - **版本号生成规则**：采用 `v主版本.次版本` 格式，自动递增
    - 首次版本：v1.0
    - 递增规则：v1.0 → v1.1 → ... → v1.9 → v2.0
  - **创建版本**：保存合同内容、修改说明、操作人信息
  - **版本对比**：使用行级差异算法，标记新增、删除、未变更行
  - **版本恢复**：创建新版本，复制目标版本内容，记录恢复操作

#### 2.1.3 实体类优化
- 文件：`ContractVersion.java`
- 修改：version字段类型从Integer改为String，支持版本号格式（如v1.0）

#### 2.1.4 数据访问层
- 文件：`ContractVersionMapper.java`
- 功能：基于MyBatis-Plus实现版本数据的CRUD操作

#### 2.1.5 权限控制
- 文件：`RequirePermission.java` / `PermissionAspect.java` / `SecurityUtils.java`
- 功能：
  - 注解式权限控制
  - 管理员拥有所有权限
  - 普通用户只能操作自己创建的合同版本
  - 敏感操作（删除、恢复）需要管理员权限

### 2.2 前端修复

#### 2.2.1 版本历史组件重构
- 文件：`VersionHistory.vue`
- 新增功能：
  - 版本选择对比（支持选择两个版本）
  - 与当前版本对比快捷按钮
  - 版本详情展示（包含版本号、操作人、修改时间、修改说明、合同内容）
  - 版本对比结果展示（差异行高亮显示）
  - 版本恢复功能（带确认对话框）

#### 2.2.2 API接口扩展
- 文件：`extra.ts`
- 新增：`compareVersions` 方法，支持版本对比API调用

## 三、测试结果

### 3.1 单元测试
- 测试文件：`ContractVersionServiceTest.java`
- 测试覆盖：
  - ✅ 版本号生成规则测试
  - ✅ 创建版本测试
  - ✅ 版本对比测试
  - ✅ 版本恢复测试
  - ✅ 版本历史查询测试
  - ✅ 最新版本获取测试

### 3.2 功能验证

#### 3.2.1 版本号生成
```
首次创建：v1.0
连续修改：v1.0 → v1.1 → v1.2 → ... → v1.9 → v2.0
```

#### 3.2.2 版本历史记录
- 每次合同修改自动创建新版本
- 记录内容：版本号、合同内容、修改说明、操作人、操作时间
- 按时间倒序排列，最新版本在前

#### 3.2.3 版本对比
- 支持任意两个版本对比
- 差异展示：
  - 新增行：绿色背景，+ 标记
  - 删除行：红色背景，- 标记
  - 未变更行：白色背景
- 统计显示：新增行数、删除行数

#### 3.2.4 版本回滚
- 恢复到指定历史版本
- 自动创建新版本记录回滚操作
- 保留回滚前的版本历史

#### 3.2.5 权限控制
- 管理员：拥有所有版本操作权限
- 普通用户：
  - 可以查看版本历史
  - 可以对比版本
  - 只能恢复自己创建的合同版本

## 四、数据库变更

### 4.1 版本记录表结构
```sql
CREATE TABLE contract_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_id BIGINT NOT NULL COMMENT '合同ID',
    version VARCHAR(10) NOT NULL COMMENT '版本号（如v1.0）',
    content TEXT COMMENT '合同内容',
    change_desc VARCHAR(500) COMMENT '修改说明',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

CREATE INDEX idx_contract_id ON contract_version(contract_id);
CREATE INDEX idx_created_at ON contract_version(created_at);
```

## 五、接口文档

### 5.1 获取版本历史
```
GET /api/contracts/{contractId}/versions
Response: { total: number, list: Version[], latestVersion: string }
```

### 5.2 获取版本详情
```
GET /api/contracts/{contractId}/versions/{versionId}
Response: Version
```

### 5.3 恢复版本
```
POST /api/contracts/{contractId}/versions/{versionId}/restore
Response: { restoredVersion: string, newVersion: string, content: string }
```

### 5.4 对比版本
```
POST /api/contracts/{contractId}/versions/compare?versionId1={id1}&versionId2={id2}
Response: { version1: string, version2: string, differences: Diff[], addedLines: number, removedLines: number }
```

## 六、部署说明

1. 执行数据库变更脚本
2. 部署后端代码
3. 部署前端代码
4. 验证版本管理功能

## 七、后续优化建议

1. 添加版本标签功能，支持标记重要版本
2. 实现版本分支管理，支持并行编辑
3. 增加版本变更审批流程
4. 添加版本变更通知机制
5. 实现版本变更统计分析

---

# 更新日志 (v1.2.0)

## 新增功能

### 1. 收藏/关注合同
- **后端**: `FavoriteController.java` + `ContractFavoriteMapper.java`
- **前端**: `Favorites.vue` 收藏列表页面
- **数据库**: `contract_favorite` 表
- **API**: GET/POST/DELETE `/api/favorites`

### 2. 自定义合同标签
- **后端**: `TagController.java` + `ContractTagMapper.java`
- **前端**: `TagManagement.vue` 标签管理页面
- **数据库**: `contract_tag` + `contract_tag_relation` 表
- **API**: CRUD `/api/tags` + 合同标签关联 API

### 3. 批量操作增强
- 批量更新状态 (`batch-status`)
- 批量审批 (`batch-approve`)
- 批量提交审批 (`batch-submit`)

### 4. 合同续约管理
- **后端**: `RenewalController.java` + `ContractRenewalMapper.java`
- **前端**: `Renewal.vue` 续约管理页面
- **数据库**: `contract_renewal` 表
- **API**: 续约记录 CRUD + 审批/拒绝

### 5. 到期预警规则配置
- **后端**: `ReminderRuleController.java` + `ReminderRuleMapper.java`
- **前端**: `ReminderRules.vue` 规则配置页面
- **数据库**: `reminder_rule` 表
- 支持按合同类型、金额范围配置不同提醒天数

### 6. 增强审计日志
- 新增 `detail` JSON 字段记录操作详情
- `SecurityUtils.getClientIp()` 获取客户端 IP
- 更详细的操作类型识别

### 7. 数据导出报表
- Excel 导出 (POI)
- PDF 下载

### 8. 合同版本持久化
- 新增 `contract_version` 表持久化版本历史
- 版本详情 `detail` 字段

## 数据库新增表

```sql
-- 合同收藏表
CREATE TABLE contract_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_contract_user (contract_id, user_id)
);

-- 合同标签表
CREATE TABLE contract_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    color VARCHAR(20) DEFAULT '#667eea',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 合同标签关联表
CREATE TABLE contract_tag_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_contract_tag (contract_id, tag_id)
);

-- 提醒规则表
CREATE TABLE reminder_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contract_type VARCHAR(20),
    min_amount DECIMAL(18,2),
    max_amount DECIMAL(18,2),
    remind_days INTEGER NOT NULL DEFAULT 30,
    is_enabled INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 合同续约记录表
CREATE TABLE contract_renewal (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    old_end_date DATE,
    new_end_date DATE,
    renewal_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    remark TEXT,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- operation_log 新增 detail 字段
ALTER TABLE operation_log ADD COLUMN detail TEXT;
```

## 新增路由

| 路径 | 页面 | 权限 |
|------|------|------|
| `/favorites` | 我的收藏 | 全部用户 |
| `/tags` | 标签管理 | 管理员 |
| `/reminder-rules` | 提醒规则 | 管理员 |
| `/renewals` | 续约管理 | 管理员/法务 |

## 菜单结构调整

**一级菜单（常用功能）：**
- 工作台、合同管理、模板库

**合同工具（子菜单）：**
- 审批管理、到期提醒、我的收藏、续约管理

**系统管理（子菜单）：**
- 统计报表、用户管理、标签管理、提醒规则

---

# v1.3.0 更新日志

## 登录页优化

### UI/UX 优化
- 键盘快捷键 - 回车提交登录
- 记住登录状态持久化到 localStorage
- 输入框浮动标签效果
- 语言切换按钮（中/EN）
- 社交登录选项（Google/企业微信/钉钉/飞书）
- 移动端自动聚焦
- ARIA标签和无障碍优化
- 动画性能优化（will-change）

### 登录页修复
- 修复输入框文字阴影问题
- 修复输入框宽度不一致问题
- 修复密码可见切换图标位置
- 移除密码强度提示（影响美观）
- 移除登录后彩纸动画（加载太慢）
- 移除红色错误提示框（不和谐）

## 通知中心增强

### 新增功能
- WebSocket 实时推送（模拟）
- 点击通知跳转对应页面
- 右键菜单删除/标记已读/标记重要
- 通知分类筛选（全部/重要/审批/系统）
- 声音提醒开关（Web Audio API）
- 偏好设置（下拉菜单）
- 未读数量角标动态显示
- 相对时间格式化
- 左滑删除支持

### 通知分类
- **重要** - 红色标记，星标通知
- **审批** - 紫色标记，审批流程
- **系统** - 蓝色标记，系统消息
- **普通** - 默认，常规提醒

## 暗色模式优化 (v1.3.0)

### 清新蓝黑主题
```
主题色: #818cf8 (柔和蓝紫)
背景色: #0f172a (深蓝灰)
卡片色: #1e293b (柔和蓝灰)
边框色: #334155 (微妙分隔)
成功色: #34d399 (柔和绿)
危险色: #f87171 (柔和红)
```

### 优化组件
- 登录页保持原有渐变配色
- 工作台图标尺寸优化（56px → 44px）
- 合同列表暗色适配
- 合同详情页面布局修复
- Element Plus 全局组件覆盖
- 卡片、对话框、表格、表单样式统一

## Bug 修复

1. 修复 `common.selected` 翻译缺失
2. 修复 CSS 语法错误（Dashboard.vue）
3. 修复输入框样式冲突
4. 修复合同详情栏位过长导致布局变形
