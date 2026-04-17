# 合同管理系统 - API 接口文档

> 合同管理系统 REST API 完整参考

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

### 3.16 获取版本历史

**GET** `/api/contracts/{id}/versions`

---

### 3.17 合同附件

**POST** `/api/contracts/{id}/attachments`

使用 `multipart/form-data` 上传文件。

---

### 3.18 导出合同

**GET** `/api/contracts/{id}/export`

#### 请求参数

| 参数 | 说明 |
|------|------|
| format | pdf/word/excel |

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

在 [3.16 获取版本历史](#316-获取版本历史) 所列版本记录基础上，使用：

**POST** `/api/contracts/{contractId}/versions/compare`

#### 请求参数（Query）

| 参数 | 类型 | 说明 |
|------|------|------|
| versionId1 | long | 版本记录 ID |
| versionId2 | long | 版本记录 ID |

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

### 7.3 权限列表

**GET** `/api/roles/permissions`

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

获取带分组的权限列表：

**GET** `/api/roles/permissions/grouped`

#### 响应示例

```json
{
  "code": 200,
  "data": {
    "合同模块": [
      {"id": 1, "code": "CONTRACT_MANAGE", "name": "合同管理"},
      {"id": 2, "code": "FOLDER_MANAGE", "name": "文件夹管理"}
    ],
    "审批流程": [
      {"id": 3, "code": "CONTRACT_APPROVE", "name": "合同审批"}
    ],
    "系统管理": [
      {"id": 4, "code": "USER_MANAGE", "name": "用户管理"}
    ]
  }
}
```

---

## 8. 提醒管理

### 8.1 提醒列表

**GET** `/api/reminders`

#### 请求参数

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 |
| pageSize | int | 每页条数 |
| status | int | 状态（0未读/1已读） |

---

### 8.2 提醒规则列表

**GET** `/api/reminder-rules`

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

---

## 9. 收藏与标签

### 9.1 收藏列表

**GET** `/api/favorites`

---

### 9.2 添加收藏

**POST** `/api/favorites`

#### 请求参数

```json
{
  "contractId": 1
}
```

---

### 9.3 取消收藏

**DELETE** `/api/favorites/{contractId}`

---

### 9.4 标签列表

**GET** `/api/tags`

---

### 9.5 创建标签

**POST** `/api/tags`

#### 请求参数

```json
{
  "name": "重要",
  "color": "#F56C6C",
  "description": "重要合同标记"
}
```

---

### 9.6 更新标签

**PUT** `/api/tags/{id}`

---

### 9.7 删除标签

**DELETE** `/api/tags/{id}`

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

```json
{
  "code": 200,
  "data": {
    "totalContracts": 156,
    "totalAmount": 12560000,
    "pendingApproval": 8,
    "expiringSoon": 12,
    "byStatus": [
      {"status": "DRAFT", "count": 15},
      {"status": "PENDING", "count": 8}
    ],
    "byType": [
      {"type": "PURCHASE", "count": 45},
      {"type": "SALES", "count": 38}
    ]
  }
}
```

---

### 11.2 金额统计

**GET** `/api/statistics/amount`

#### 请求参数

| 参数 | 类型 | 说明 |
|------|------|------|
| type | string | 合同类型 |
| startDate | string | 开始日期 |
| endDate | string | 结束日期 |

---

### 11.3 导出统计数据

**GET** `/api/statistics/export`

#### 请求参数

| 参数 | 类型 | 说明 |
|------|------|------|
| format | string | 导出格式（excel） |
| type | string | 合同类型 |
| startDate | string | 开始日期 |
| endDate | string | 结束日期 |

---

## 12. 其他接口

### 12.1 续约列表

**GET** `/api/renewals`

---

### 12.2 创建续约

**POST** `/api/renewals`

#### 请求参数

```json
{
  "contractId": 1,
  "newEndDate": "2028-03-31",
  "renewalType": "RENEW",
  "remark": "合同到期续约"
}
```

---

### 12.3 操作日志

**GET** `/api/operation-logs`

#### 请求参数

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 |
| pageSize | int | 每页条数 |
| module | string | 模块筛选 |
| startDate | string | 开始日期 |
| endDate | string | 结束日期 |

---

### 12.4 获取合同类型字段配置

**GET** `/api/type-field-config`

#### 请求参数

| 参数 | 类型 | 说明 |
|------|------|------|
| contractType | string | 合同类型 |

---

### 12.5 保存合同类型字段配置

**POST** `/api/type-field-config`

#### 请求参数

```json
{
  "contractType": "PURCHASE",
  "fields": [
    {
      "fieldKey": "productName",
      "fieldLabel": "产品名称",
      "fieldType": "text",
      "required": true,
      "showInList": true,
      "showInForm": true
    }
  ]
}
```

---

## 13. 快速代码管理

### 13.1 快速代码头列表

**GET** `/api/quick-codes`

#### 请求参数

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 |
| pageSize | int | 每页条数 |
| keyword | string | 关键词搜索 |
| status | int | 状态（默认1） |

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

---

### 13.6 快速代码项列表

**GET** `/api/quick-codes/{headerId}/items`

#### 请求参数

| 参数 | 类型 | 说明 |
|------|------|------|
| enabled | boolean | 只返回启用的代码项 |

---

### 13.7 创建快速代码项

**POST** `/api/quick-codes/{headerId}/items`

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

### 13.8 更新快速代码项

**PUT** `/api/quick-codes/{headerId}/items/{itemId}`

#### 请求参数

同创建快速代码项。

---

### 13.9 删除快速代码项

**DELETE** `/api/quick-codes/{headerId}/items/{itemId}`

---

### 13.10 根据代码获取快速代码项

**GET** `/api/quick-codes/by-code/{code}`

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

## 📝 附录

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

*API文档最后更新：2026-04-11*
