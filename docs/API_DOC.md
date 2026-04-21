# 合同管理系统 - API 接口文档

> 合同管理系统 REST API 完整参考

> 推荐先看拆分入口：
> - 业务接口：`API_BUSINESS.md`
> - 系统/运维接口：`API_SYSTEM.md`

---

## 📑 目录

1. [概述](#1-概述)
2. [认证](#2-认证)
3. [合同管理](#3-合同管理)
4. [模板管理](#4-模板管理)
5. [变量管理](#5-变量管理)
6. [用户管理](#6-用户管理)
7. [角色权限](#7-角色权限)
8. [提醒管理](#8-提醒管理)
9. [收藏与标签](#9-收藏与标签)
10. [文件夹](#10-文件夹)
11. [统计报表](#11-统计报表)
12. [其他接口](#12-其他接口)
13. [快速代码管理](#13-快速代码管理)
14. [附录](#14-附录)

---

## 1. 概述

### 1.1 基础信息

| 项目 | 说明 |
|------|------|
| 基础URL | `http://localhost:8081/api` |
| 数据格式 | JSON |
| 编码格式 | UTF-8 |
| 认证方式 | Bearer Token (JWT) |

### 1.2 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { },
  "timestamp": 1775030400000
}
```

### 1.3 响应状态码

| 状态码 | 说明 | 处理建议 |
|--------|------|----------|
| 200 | 成功 | 正常处理 |
| 400 | 请求参数错误 | 检查请求参数 |
| 401 | 未认证 | 重新登录 |
| 403 | 无权限 | 检查用户权限 |
| 404 | 资源不存在 | 检查资源ID |
| 500 | 服务器错误 | 联系管理员 |

### 1.4 分页参数

| 参数 | 类型 | 说明 | 默认值 |
|------|------|------|--------|
| page | int | 页码 | 1 |
| pageSize | int | 每页条数 | 10 |

### 1.5 分页响应

```json
{
  "code": 200,
  "data": {
    "list": [],
    "total": 100,
    "page": 1,
    "pageSize": 10
  }
}
```

### 1.6 文档对齐说明（2026-04）

以下路径已按当前后端 `com.contracthub.controller` 包内映射核对；若本文与实现有冲突，以代码中的 `@RequestMapping` / `@GetMapping` 等为准。

### 1.7 维护策略（单点维护）

- 日常新增/改动接口时，优先更新对应入口文档：
  - 业务接口改动：`API_BUSINESS.md`
  - 系统/运维改动：`API_SYSTEM.md`
- `API_DOC.md` 保持“完整参考”定位，按迭代合并更新，不要求每次小改即时逐行同步。
- 每次合并前执行 `python3 scripts/scan-controller-routes.py` 做路径对账。

---

## 2. 认证

### 2.1 用户登录

**POST** `/api/auth/login`

#### 请求参数

```json
{
  "username": "admin",
  "password": "admin123"
}
```

#### 响应示例

```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "id": 1,
    "username": "admin",
    "nickname": "管理员",
    "role": "ADMIN",
    "roles": ["ADMIN", "LEGAL", "USER"],
    "permissions": [
      "CONTRACT_MANAGE",
      "TEMPLATE_MANAGE",
      "USER_MANAGE"
    ]
  }
}
```

---

### 2.2 用户注册

**POST** `/api/auth/register`

#### 请求参数

```json
{
  "username": "newuser",
  "password": "password123",
  "nickname": "新用户",
  "email": "newuser@example.com"
}
```

---

### 2.3 刷新Token

**POST** `/api/auth/refresh`

#### 请求头

```
Authorization: Bearer <token>
```

---

## 3. 合同管理

### 3.1 合同列表

**GET** `/api/contracts`

#### 请求参数

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 |
| pageSize | int | 每页条数 |
| keyword | string | 关键词搜索 |
| type | string | 合同类型 |
| status | string | 合同状态 |
| folderId | long | 文件夹ID |
| startDate | string | 开始日期 |
| endDate | string | 结束日期 |

#### 合同类型

| 值 | 说明 |
|------|------|
| PURCHASE | 采购合同 |
| SALES | 销售合同 |
| LEASE | 租赁合同 |
| EMPLOYMENT | 劳动合同 |
| SERVICE | 服务合同 |
| OTHER | 其他合同 |

#### 合同状态

| 值 | 说明 |
|------|------|
| DRAFT | 草稿 |
| PENDING | 待审批 |
| APPROVED | 已批准 |
| REJECTED | 已拒绝 |
| SIGNED | 已签署 |
| ARCHIVED | 已归档 |
| TERMINATED | 已终止 |

---

### 3.2 创建合同

**POST** `/api/contracts`

#### 请求参数

```json
{
  "title": "采购服务器设备合同",
  "type": "PURCHASE",
  "amount": 500000,
  "startDate": "2026-04-01",
  "endDate": "2027-03-31",
  "counterparties": [
    {
      "type": "partyA",
      "name": "甲方公司",
      "contact": "张三",
      "phone": "13800138000",
      "email": "甲方@example.com",
      "creditCode": "91110000XXXXXXXX",
      "address": "北京市朝阳区",
      "bank": "中国银行",
      "account": "123456789"
    },
    {
      "type": "partyB",
      "name": "乙方公司",
      "contact": "李四",
      "phone": "13900139000"
    }
  ],
  "content": "<p>合同正文内容...</p>",
  "folderId": 1,
  "tags": ["重要", "紧急"],
  "dynamicFields": {
    "productName": "服务器设备",
    "quantity": 10,
    "unitPrice": 50000
  }
}
```

---

### 3.3 合同详情

**GET** `/api/contracts/{id}`

---

### 3.4 更新合同

**PUT** `/api/contracts/{id}`

#### 请求参数

同创建合同，可部分更新。

---

### 3.5 删除合同

**DELETE** `/api/contracts/{id}`

---

### 3.6 提交审批

**POST** `/api/contracts/{id}/submit`

---

### 3.7 审批通过

**POST** `/api/contracts/{id}/approve`

#### 请求参数

```json
{
  "comment": "审批通过"
}
```

---

### 3.8 审批拒绝

**POST** `/api/contracts/{id}/reject`

#### 请求参数

```json
{
  "comment": "需要修改金额"
}
```

---

### 3.9 签署合同

**POST** `/api/contracts/{id}/sign`

---

### 3.10 归档合同

**POST** `/api/contracts/{id}/archive`

---

### 3.11 终止合同

**POST** `/api/contracts/{id}/terminate`

#### 请求参数

```json
{
  "reason": "合同到期终止"
}
```

---

### 3.12 复制合同

**POST** `/api/contracts/{id}/copy`

---

### 3.12.1 续签状态流转（合同主接口）

以下接口位于 `ContractController`，用于合同主状态机中的续签流程：

- **POST** `/api/contracts/{id}/renewal/start`
- **POST** `/api/contracts/{id}/renewal/complete`
- **POST** `/api/contracts/{id}/renewal/decline`

---

### 3.13 AI分析

**POST** `/api/contracts/{id}/analyze`

#### 请求参数

```json
{
  "analysisType": "risk"
}
```

| 分析类型 | 说明 |
|----------|------|
| risk | 风险评估 |
| clause | 关键条款提取 |
| compliance | 合规性检查 |

#### 响应示例

```json
{
  "code": 200,
  "data": {
    "riskLevel": "medium",
    "risks": [
      {
        "type": "payment",
        "description": "付款条件较为苛刻",
        "severity": "medium"
      }
    ],
    "clauses": [
      {
        "title": "违约金条款",
        "content": "违约方需支付合同金额20%的违约金"
      }
    ],
    "suggestions": [
      "建议明确付款验收标准"
    ]
  }
}
```

---

### 3.14 合同评论

**POST** `/api/contracts/{id}/comments`

#### 请求参数

```json
{
  "content": "这是我的评论"
}
```

---

### 3.15 获取评论列表

**GET** `/api/contracts/{id}/comments`

---

### 3.16 版本历史与对比（`/api/contracts/{contractId}/versions`）

路径参数在代码中为 `contractId`，与 `{id}` 含义相同（合同主键）。

**GET** `/api/contracts/{contractId}/versions` — 版本列表  

**GET** `/api/contracts/{contractId}/versions/{versionId}` — 版本详情  

**POST** `/api/contracts/{contractId}/versions` — 手动创建版本记录（body 含 `content`、`changeDesc` 等，以服务端校验为准）  

**POST** `/api/contracts/{contractId}/versions/{versionId}/restore` — 恢复到指定版本（需 `CONTRACT_APPROVE`）  

**POST** `/api/contracts/{contractId}/versions/compare` — 两版本对比（Query：`versionId1`、`versionId2`）

---

### 3.17 附件与文件

#### 3.17.1 通用上传（磁盘文件，返回 `fileName` 供正文引用）

**POST** `/api/contracts/upload`  

`multipart/form-data`：`file`（必填）；`contractId`（可选）。

**GET** `/api/contracts/download/{fileName}` — 下载  

**DELETE** `/api/contracts/attachments/{fileName}` — 删除磁盘上的附件文件  

#### 3.17.2 结构化附件（`contract_attachment` 表）

**GET** `/api/contract-attachments/contract/{contractId}` — 某合同的附件记录列表  

**POST** `/api/contract-attachments` — 新增附件元数据  

**PUT** `/api/contract-attachments/{id}` / **DELETE** `/api/contract-attachments/{id}`  

**POST** `/api/contract-attachments/batch` — 按合同批量覆盖保存（body 含 `contractId` 与 `attachments` 列表）

---

### 3.18 导出与单份下载

| 说明 | 方法 | 路径 |
|------|------|------|
| 合同列表导出 Excel（当前可见范围，与列表权限一致） | GET | `/api/contracts/export` |
| 合同列表导出 Excel（支持筛选 query，与列表查询参数类似） | GET | `/api/contracts/export/excel` |
| 单份 PDF | GET | `/api/contracts/{id}/pdf` |
| 单份 Word | GET | `/api/contracts/{id}/word` |
| 根据 HTML 正文生成 PDF（body：`content`、`contractNo`、`watermark` 等） | POST | `/api/contracts/generate-pdf` |

> 不再有统一的 `GET /api/contracts/{id}/export?format=`；请按上表选用接口。

---

### 3.19 合同正文载荷（大字段）

列表接口为瘦身响应，正文等从本接口按需加载。

**GET** `/api/contracts/{id}/payload`

需要权限：`CONTRACT_MANAGE`（与合同详情一致策略以服务端为准）。

响应 `data` 中通常包含：`content`、`templateVariables`、`dynamicFieldValues`、`attachments` 等（与 `contract_payload` 表一致）。

---

### 3.20 快照列表

**GET** `/api/contracts/{id}/snapshots`

用于审批审计、版本历史页选择快照对比等场景。

---

### 3.21 快照详情

**GET** `/api/contracts/{id}/snapshots/{snapshotId}`

---

### 3.22 快照对比

**POST** `/api/contracts/{id}/snapshots/compare`

#### 请求参数（Query）

| 参数 | 类型 | 说明 |
|------|------|------|
| baseSnapshotId | long | 基准快照 ID |
| targetSnapshotId | long | 对比快照 ID |

返回包含行级 `differences`、条款粒度 `clauseChanges`、风险解释 `riskItems`、`aiCommentary` / `aiCommentaryKey` 等（具体字段以后端实现为准）。

---

### 3.23 审批摘要

**GET** `/api/contracts/{id}/approval-summary`  
**POST** `/api/contracts/{id}/approval-summary/generate`

#### 生成接口参数（Query）

| 参数 | 类型 | 说明 |
|------|------|------|
| force | boolean | 是否强制重新生成，默认 `false` |

---

### 3.24 版本对比（合同版本实体）

版本对比见上文 **3.16** 中的 **POST** `/api/contracts/{contractId}/versions/compare`（Query：`versionId1`、`versionId2`）。

---

## 4. 模板管理

### 4.1 模板列表

**GET** `/api/templates`

#### 请求参数

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 |
| pageSize | int | 每页条数 |
| category | string | 模板分类 |
| keyword | string | 关键词搜索 |

---

### 4.2 创建模板

**POST** `/api/templates`

#### 请求参数

```json
{
  "name": "标准采购合同模板",
  "category": "PURCHASE",
  "content": "<p>合同正文，包含变量：{{contractNo}}, {{partyA}}, {{partyB}}</p>",
  "variables": {
    "contractNo": "合同编号",
    "partyA": "甲方名称",
    "partyB": "乙方名称",
    "productName": "商品名称",
    "quantity": "数量",
    "unitPrice": "单价",
    "totalPrice": "总价"
  },
  "description": "适用于一般采购业务"
}
```

---

### 4.3 模板详情

**GET** `/api/templates/{id}`

---

### 4.4 更新模板

**PUT** `/api/templates/{id}`

---

### 4.5 删除模板

**DELETE** `/api/templates/{id}`

---

### 4.6 变量替换预览

**POST** `/api/templates/{id}/substitute`

#### 请求参数

```json
{
  "contractNo": "TC20260401-0001",
  "partyA": "某某科技有限公司",
  "partyB": "某某贸易有限公司",
  "productName": "服务器设备",
  "quantity": 10,
  "unitPrice": 50000,
  "totalPrice": 500000
}
```

#### 响应示例

```json
{
  "code": 200,
  "data": {
    "content": "<p>合同编号：TC20260401-0001</p><p>甲方：某某科技有限公司</p>...",
    "usageCount": 15
  }
}
```

---

### 4.7 克隆模板

**POST** `/api/templates/{id}/clone`

---

### 4.8 获取模板变量

**GET** `/api/templates/variables/list`

Query：`contractType`（可选），按合同类型筛选可用变量。

---

### 4.9 模板变量、预览与导出（补充）

以下均挂在 **`/api/templates`** 下；列表/详情/增删改需 **`TEMPLATE_MANAGE`**（已在方法上标注的除外）。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/variables/{id}` | 指定模板 ID 的变量 Map（`id` 为模板主键） |
| GET | `/variables/extract?content=` | 从正文中提取占位符变量名 |
| POST | `/{id}/preview-simple` | 简单预览（`[[变量名]]` 风格替换，body 为键值对） |
| POST | `/{id}/preview` | 完整预览；body 可含 `values` 覆盖默认变量 |
| POST | `/replace` | body：`content`、`values`，仅做变量替换，不读库模板 |
| POST | `/{id}/watermark` | 水印：body 含 `text` / `imageUrl`、`position`、`opacity` |
| GET | `/{id}/export/pdf` | 下载模板 PDF |
| GET | `/{id}/export` | 返回模板 JSON（含 `content`、`variables` 等） |

> 默认种子模板正文使用 `[[placeholder]]` 占位；与仅作示例的 `{{ }}` 写法并存时，以实际模板内容与 `TemplateVariableService` 替换逻辑为准。

---

## 5. 变量管理

### 5.1 变量列表

**GET** `/api/template-variables`

#### 请求参数

| 参数 | 类型 | 说明 |
|------|------|------|
| category | string | 变量分类 |
| type | string | 变量类型 |
| status | int | 状态（默认1） |

#### 变量分类

| 值 | 说明 |
|------|------|
| system | 系统变量 |
| party | 相对方信息 |
| purchase | 采购合同 |
| service | 服务合同 |
| lease | 租赁合同 |
| employment | 劳动合同 |
| custom | 自定义 |

#### 变量类型

| 值 | 说明 |
|------|------|
| text | 文本 |
| number | 数字 |
| date | 日期 |
| textarea | 多行文本 |

---

### 5.2 创建变量

**POST** `/api/template-variables`

#### 请求参数

```json
{
  "code": "customField1",
  "name": "自定义字段",
  "label": "自定义字段",
  "type": "text",
  "category": "custom",
  "defaultValue": "",
  "description": "用于自定义业务字段",
  "required": 0
}
```

---

### 5.3 更新变量

**PUT** `/api/template-variables/{id}`

---

### 5.4 删除变量

**DELETE** `/api/template-variables/{id}`

---

### 5.5 初始化默认变量

**POST** `/api/template-variables/init-defaults`

批量创建40+预定义变量。

---

## 6. 用户管理

### 6.1 当前用户信息

**GET** `/api/users/me`

---

### 6.2 用户列表

**GET** `/api/users`

#### 请求参数

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 |
| pageSize | int | 每页条数 |
| role | string | 角色筛选 |
| keyword | string | 关键词搜索 |

---

### 6.3 创建用户

**POST** `/api/users`

#### 请求参数

```json
{
  "username": "newuser",
  "nickname": "新用户",
  "email": "newuser@example.com",
  "phone": "13800138001",
  "role": "USER"
}
```

---

### 6.4 更新用户

**PUT** `/api/users/{id}`

---

### 6.5 删除用户

**DELETE** `/api/users/{id}`

---

### 6.6 修改密码

**POST** `/api/users/change-password`

#### 请求参数

```json
{
  "oldPassword": "123456",
  "newPassword": "newpassword123"
}
```

---

### 6.7 重置密码

**PUT** `/api/users/{id}/reset-password`

---

### 6.8 更新状态

**PUT** `/api/users/{id}/status`

#### 请求参数

```json
{
  "status": "active"
}
```

---

## 7. 角色权限

### 7.1 角色列表

**GET** `/api/roles`

---

### 7.2 活跃角色

**GET** `/api/roles/active`

---

### 7.3 权限列表（含分组）

**GET** `/api/roles/permissions`

响应 `data` 中同时包含：

- `list`：全部权限平铺列表  
- `grouped`：按业务模块分组后的 `Map<String, List<Permission>>`  
- `total`：权限总数  

> 不存在单独的 `/api/roles/permissions/grouped` 路径；分组数据请使用本接口的 `grouped` 字段。

---

### 7.4 角色详情

**GET** `/api/roles/{id}`

---

### 7.5 创建角色

**POST** `/api/roles`

#### 请求参数

```json
{
  "name": "法务专员",
  "code": "LEGAL_STAFF",
  "description": "法务专员角色",
  "status": 1,
  "permissionIds": [1, 2, 3, 6, 7, 9, 10]
}
```

---

### 7.6 更新角色

**PUT** `/api/roles/{id}`

---

### 7.7 删除角色

**DELETE** `/api/roles/{id}`

---

### 7.8 角色权限分组

分组后的权限见 **7.3** 响应中的 `data.grouped`（键名为后端按权限 `code` 归类的中文分组，与下表示例可能略有差异，以实际返回为准）。

#### 响应示例（`data.grouped` 结构示意）

```json
{
  "合同管理": [
    {"id": 1, "code": "CONTRACT_MANAGE", "name": "合同管理"}
  ],
  "合同协同": [
    {"id": 3, "code": "CONTRACT_APPROVE", "name": "合同审批"}
  ],
  "系统管理": [
    {"id": 4, "code": "USER_MANAGE", "name": "用户管理"}
  ]
}
```

### 7.9 启用/停用角色

**PUT** `/api/roles/{id}/toggle`

---

## 8. 提醒管理

### 8.1 提醒列表

**GET** `/api/reminders/my` — 当前登录用户的提醒（需 `CONTRACT_MANAGE`）；支持 `keyword`、`status` 等 query  

**GET** `/api/reminders` — 全部提醒（管理，需 `REMINDER_MANAGE`）

#### 请求参数（`/my` 示例）

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 |
| pageSize | int | 每页条数 |
| keyword | string | 合同编号/标题模糊 |
| status | string | 状态筛选 |

其它：`PUT /api/reminders/{id}/read`、`PUT /api/reminders/read-batch`、`POST /api/reminders/check` 等见 `ReminderController`。

---

### 8.2 提醒规则列表

**GET** `/api/reminder-rules`

**GET** `/api/reminder-rules/my` — 当前用户可见规则（本人创建或公开规则）

---

### 8.3 创建提醒规则

**POST** `/api/reminder-rules`

#### 请求参数

```json
{
  "name": "重要合同提醒",
  "contractTypes": ["PURCHASE", "SALES"],
  "minAmount": 100000,
  "maxAmount": 10000000,
  "remindDays": 30,
  "isEnabled": 1
}
```

---

### 8.4 更新提醒规则

**PUT** `/api/reminder-rules/{id}`

---

### 8.5 删除提醒规则

**DELETE** `/api/reminder-rules/{id}`

### 8.6 启用/停用规则

**PUT** `/api/reminder-rules/{id}/toggle`

---

## 9. 收藏与标签

### 9.1 收藏列表

**GET** `/api/favorites`

#### 请求参数

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 |
| pageSize | int | 每页条数 |
| userId | long | 用户 ID（当前实现为 query，默认 `1`，与登录用户绑定策略以服务端为准） |

---

### 9.2 添加收藏

**POST** `/api/favorites/{contractId}`

#### 请求参数

路径参数：`contractId`（Long）

Query：`userId`（可选，默认 `1`，与当前登录用户一致性以服务端为准）

---

### 9.3 取消收藏

**DELETE** `/api/favorites/{contractId}`

Query：`userId`（可选，默认 `1`）

---

### 9.4 标签列表

**GET** `/api/tags` — 全部标签（含使用量等）

**GET** `/api/tags/my` — 当前用户可见（本人创建或公开标签）

---

### 9.5 创建标签

**POST** `/api/tags`

#### 请求参数

```json
{
  "name": "重要",
  "color": "#F56C6C",
  "description": "重要合同标记",
  "isPublic": true
}
```

---

### 9.6 更新标签

**PUT** `/api/tags/{id}`

---

### 9.7 删除标签

**DELETE** `/api/tags/{id}`

---

### 9.8 标签与合同关联

均需 **`TAG_MANAGE`**。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/tags/contract/{contractId}` | 某合同已打上的标签 |
| POST | `/api/tags/contract/{contractId}` | body：`{ "tagId": 1 }`，为合同增加一个标签 |
| DELETE | `/api/tags/contract/{contractId}/{tagId}` | 移除合同上的某标签 |
| PUT | `/api/tags/contract/{contractId}` | 覆盖式更新：body `tags: [{ "id": 1 }, ...]` |
| GET | `/api/tags/{tagId}/contracts` | 某标签下的合同列表 |

---

## 10. 文件夹

### 10.1 文件夹树

**GET** `/api/contract-folders/tree`

#### 响应示例

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "采购合同",
      "color": "#409EFF",
      "children": [
        {"id": 2, "name": "2026年", "parentId": 1}
      ]
    }
  ]
}
```

---

### 10.2 文件夹列表

**GET** `/api/contract-folders`

---

### 10.3 创建文件夹

**POST** `/api/contract-folders`

#### 请求参数

```json
{
  "name": "采购合同",
  "parentId": null,
  "color": "#409EFF",
  "description": "采购相关合同"
}
```

---

### 10.4 更新文件夹

**PUT** `/api/contract-folders/{id}`

---

### 10.5 删除文件夹

**DELETE** `/api/contract-folders/{id}`

---

## 11. 统计报表

### 11.1 概览统计

**GET** `/api/statistics/overview`

#### 响应示例

以下字段与 `StatisticsController#overview` 当前实现一致（金额增长等为数值百分比，非图表专用 `byStatus` / `byType` 结构）：

```json
{
  "code": 200,
  "data": {
    "totalContracts": 156,
    "totalAmount": 12560000.0,
    "pendingApproval": 8,
    "expiringSoon": 12,
    "monthlyNew": 10,
    "signedThisMonth": 4,
    "amountGrowth": 5.2,
    "contractGrowth": -1.0
  }
}
```

状态/类型分布请使用 **11.2**、**11.3** 对应接口。

---

### 11.2 状态分布

**GET** `/api/statistics/status-distribution`

---

### 11.3 类型分布

**GET** `/api/statistics/type-distribution`

---

### 11.4 月度趋势

**GET** `/api/statistics/monthly-trend`

---

### 11.5 交易对手 Top

**GET** `/api/statistics/top-counterparties`

---

### 11.6 用户活跃度

**GET** `/api/statistics/user-activity`

---

## 12. 其他接口

### 12.1 续约（主路径：挂在合同下）

**GET** `/api/contracts/{contractId}/renewals` — 某合同的续约记录列表  

**POST** `/api/contracts/{contractId}/renewals` — 发起续约申请  

#### 请求参数（POST body 示例）

```json
{
  "newEndDate": "2028-03-31",
  "renewalType": "EXTEND",
  "remark": "合同到期续约"
}
```

**PUT** `/api/contracts/{contractId}/renewals/{id}/approve` — 审批通过（角色 `ADMIN` / `LEGAL`）  

**PUT** `/api/contracts/{contractId}/renewals/{id}/reject` — 审批拒绝（body 可含 `remark`）

---

### 12.2 续约列表（全局分页）

**GET** `/api/renewals`

Query：`status`、`page`、`pageSize`（实现见 `RenewalListController`）。

---

### 12.3 操作日志

**GET** `/api/logs`

#### 请求参数

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 |
| pageSize | int | 每页条数 |
| module | string | 模块 |
| targetId | long | 目标实体 ID |
| keyword | string | 描述或操作人模糊匹配 |

**GET** `/api/logs/{id}` — 详情  

**GET** `/api/logs/modules` — 模块名去重列表  

> 系统设置里另有 **GET** `/api/system/operation-logs`（`SystemController`），与本接口不同，请勿混淆。

---

### 12.4 合同类型字段（完整子路径见代码）

基础路径：**GET/POST** `/api/contract-type-fields`

常用示例：

- **GET** `/api/contract-type-fields/form/{contractType}` — 表单用字段列表  
- **GET** `/api/contract-type-fields/config/{contractType}` — 分页配置  
- **GET/PUT/DELETE** `/api/contract-type-fields/draft/{contractType}` — 草稿  
- **POST** `/api/contract-type-fields/publish/{contractType}` — 发布草稿到正式表  
- **GET** `/api/contract-type-fields/export` / **POST** `/api/contract-type-fields/import` — 导入导出  

单条 **POST** `/api/contract-type-fields` 表示新增一条字段配置（body 为 `ContractTypeField` 结构），与「整表 JSON 一次性保存」型接口不同，请以实际请求体为准。

---

### 12.5 权限资源 CRUD（独立模块）

**GET** `/api/permissions` — 全部权限  

**GET** `/api/permissions/active` — 启用权限  

**GET** `/api/permissions/{id}` — 详情  

**POST** `/api/permissions` / **PUT** `/api/permissions/{id}` / **DELETE** `/api/permissions/{id}` — 维护（需 `ROLE_MANAGE`）

---

### 12.6 通知接口（`/api/notifications`）

- **GET** `/api/notifications/my` — 我的通知列表
- **PUT** `/api/notifications/{id}/read` — 单条标已读
- **PUT** `/api/notifications/read-all` — 全部标已读
- **PUT** `/api/notifications/{id}/important` — 重要标记切换
- **DELETE** `/api/notifications/{id}` — 删除单条
- **DELETE** `/api/notifications/my` — 清空我的通知

---

### 12.7 系统管理接口（`/api/system`）

主要用于系统配置与运维观测（权限通常要求管理员）：

- **GET/POST** `/api/system/configs` — 读取/保存系统配置
- **GET** `/api/system/monitor` — 运行状态/监控信息
- **GET** `/api/system/operation-logs` — 系统侧操作日志
- **GET** `/api/system/login-history` — 登录历史
- **GET** `/api/system/sessions` — 在线会话
- **DELETE** `/api/system/sessions/{id}`、`/api/system/sessions` — 会话剔除
- **POST** `/api/system/email/test`、`/api/system/sms/test` — 通道连通性测试

---

### 12.8 外部对接合同接口（`/api/external/contracts`）

用于第三方系统写入/查询合同，控制器包含授权校验：

- **POST** `/api/external/contracts` — 创建单合同
- **POST** `/api/external/contracts/batch` — 批量创建
- **GET** `/api/external/contracts` — 条件分页查询
- **GET** `/api/external/contracts/{id}` — 合同详情
- **PUT** `/api/external/contracts/{id}` — 更新
- **DELETE** `/api/external/contracts/{id}` — 删除
- **GET** `/api/external/contracts/next-number` — 下一个合同编号
- **GET** `/api/external/contracts/health` — 健康检查

---

### 12.9 合同分类接口（`/api/contract-categories` 与 `/api/categories`）

`ContractCategoryController` 支持双基路径别名，能力一致：

- **GET** `/api/contract-categories`（或 `/api/categories`）— 启用分类列表
- **GET** `/api/contract-categories/all` — 管理页分页列表
- **GET** `/api/contract-categories/{id}` — 分类详情
- **POST** `/api/contract-categories` — 创建
- **PUT** `/api/contract-categories/{id}` — 更新
- **DELETE** `/api/contract-categories/{id}` — 删除/禁用（系统默认分类会走禁用策略）

---

### 12.10 条款库接口（`/api/clauses`）

- **GET** `/api/clauses` — 列表（支持 `keyword`、`category`、`status`）
- **GET** `/api/clauses/references` — 条款引用统计（模板侧）
- **GET** `/api/clauses/{id}` — 详情
- **POST** `/api/clauses` — 创建
- **PUT** `/api/clauses/{id}` — 更新
- **DELETE** `/api/clauses/{id}` — 删除

---

### 12.11 合同相对方接口（`/api/contract-counterparties`）

- **GET** `/api/contract-counterparties/contract/{contractId}` — 按合同查询
- **POST** `/api/contract-counterparties` — 创建单条
- **PUT** `/api/contract-counterparties/{id}` — 更新单条
- **DELETE** `/api/contract-counterparties/{id}` — 删除单条
- **DELETE** `/api/contract-counterparties/contract/{contractId}` — 清空合同下全部相对方
- **POST** `/api/contract-counterparties/batch` — 批量覆盖保存（body 含 `contractId` 与 `counterparties`）

---

### 12.12 变更日志接口（`/api/contracts/{contractId}/change-logs`、`/api/change-logs`）

- **GET** `/api/contracts/{contractId}/change-logs` — 某合同变更记录
- **POST** `/api/contracts/{contractId}/change-logs` — 新增变更记录
- **GET** `/api/change-logs/recent` — 最近变更（`limit`）
- **GET** `/api/change-logs/my` — 我的变更（`limit`）

---

### 12.13 到期预警接口

- **GET** `/api/contracts/expiring` — 即将到期合同列表
- **GET** `/api/contracts/statistics/expiration` — 到期统计

---

## 13. 快速代码管理

### 13.1 快速代码头列表

**GET** `/api/quick-codes` — 下拉用精简列表（受 `Accept-Language` 影响展示名，无分页参数）

**GET** `/api/quick-codes/all` — 管理页分页列表  

#### 请求参数（`/all`）

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 |
| pageSize | int | 每页条数 |

---

### 13.2 创建快速代码头

**POST** `/api/quick-codes`

#### 请求参数

```json
{
  "name": "模板变量分类",
  "nameEn": "Template Variable Category",
  "code": "TEMPLATE_VARIABLE_CATEGORY",
  "description": "模板变量的分类选项",
  "descriptionEn": "Category options for template variables"
}
```

---

### 13.3 快速代码头详情

**GET** `/api/quick-codes/{id}`

---

### 13.4 更新快速代码头

**PUT** `/api/quick-codes/{id}`

#### 请求参数

同创建快速代码头。

---

### 13.5 删除快速代码头

**DELETE** `/api/quick-codes/{id}`

**PUT** `/api/quick-codes/{id}/toggle` — 启用/停用  

**GET** `/api/quick-codes/{id}/impact` — 引用影响面（管理用）

---

### 13.6 创建快速代码项

**POST** `/api/quick-codes/{id}/items`

#### 请求参数

```json
{
  "code": "system",
  "meaning": "系统变量",
  "meaningEn": "System Variables",
  "description": "系统预设变量",
  "descriptionEn": "System predefined variables",
  "tag": "system",
  "validFrom": "2026-01-01",
  "validTo": "2026-12-31",
  "enabled": true,
  "sortOrder": 1
}
```

---

### 13.7 更新快速代码项

**PUT** `/api/quick-codes/items/{itemId}`

#### 请求参数

同创建快速代码项。

---

### 13.8 删除快速代码项

**DELETE** `/api/quick-codes/items/{itemId}`

---

### 13.9 根据代码获取快速代码项

**GET** `/api/quick-codes/code/{code}`

获取指定代码的所有启用的代码项。

#### 响应示例

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "code": "system",
      "meaning": "系统变量",
      "meaningEn": "System Variables",
      "enabled": true,
      "sortOrder": 1
    }
  ]
}
```

---

## 14. 附录

### 其它已注册路由（未全文展开）

| 模块 | 基础路径 | 说明 |
|------|----------|------|
| AI | `/api/ai` | 分析、模板分析、Ollama 模型列表等（`AiController`） |
| 通知 | `/api/notifications` | 我的通知、已读、删除等（`NotificationController`） |
| 系统 | `/api/system` | 配置、监控、操作日志（另一套）、会话、登录历史等（`SystemController`） |
| 外部合同 | `/api/external/contracts` | 对外集成（`ExternalContractController`） |
| 合同分类 | `/api/contract-categories`、`/api/categories` | 别名双路径（`ContractCategoryController`） |
| 条款库 | `/api/clauses` | `ContractClauseController` |
| 相对方 | `/api/contract-counterparties` | `ContractCounterpartyController` |
| 变更记录 | `/api/contracts/{contractId}/change-logs`、`/api/change-logs` | `ContractChangeLogController` |
| 到期预警 | `/api/contracts/expiring`、`/api/contracts/statistics/expiration` | `ExpirationWarningController` |
| 管理初始化 | `/api/admin/init-permissions` | `AdminController` |

### 文档维护清单（防再次偏差）

1. 新增或修改 REST 接口时，同步更新本文对应小节，或至少在「附录」表中增加一行指向 `*Controller`。  
2. 路径以编译后的 `@RequestMapping` + 方法映射为准，不要用前端 axios 的字符串反向猜测。  
3. 响应结构以 `ApiResponse` + 各方法返回值为准；示例 JSON 仅作演示。  
4. 同一业务若存在「合同下子资源」与「全局列表」两套路径（如续约），两类都需在文档中写明。  
5. 可运行仓库内脚本 **`python3 scripts/scan-controller-routes.py`** 生成「方法 + 路径 + 源文件」清单，用于与本文对账（启发式解析，极少数注解写法需人工复核）。  
6. CI 会校验 `scripts/controller-routes.baseline.tsv` 与当前扫描结果一致；接口路径变化后请同步更新该基线文件。  

### 错误码对照表

| 错误码 | 错误信息 | 解决方案 |
|--------|----------|----------|
| 1001 | 用户名或密码错误 | 检查登录信息 |
| 1002 | Token已过期 | 重新登录 |
| 1003 | 无权限访问 | 联系管理员 |
| 2001 | 合同不存在 | 检查合同ID |
| 2002 | 合同状态不允许操作 | 检查合同当前状态 |
| 3001 | 模板不存在 | 检查模板ID |
| 4001 | 变量编码已存在 | 使用其他编码 |
| 5001 | 用户不存在 | 检查用户ID |
| 5002 | 用户名已存在 | 使用其他用户名 |

---

*API文档最后更新：2026-04-21（第四轮：通知/系统/外部合同/分类/条款/相对方/变更日志/到期预警 与续签状态流转补齐）*
