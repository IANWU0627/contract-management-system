# 合同管理系统 - API（系统与运维接口）

> 面向管理员、运维与平台集成：通知、系统配置、外部合同接入、分类/条款/相对方、变更日志、到期预警。

---

## 使用说明

- 本文聚焦“非核心业务页面”但高频运维/集成接口。
- 完整请求与示例仍以 `API_DOC.md` 为主。
- 若本文与实现不一致，以控制器注解映射为准。
- 系统/运维接口变更时，优先更新本文，再按迭代回填 `API_DOC.md`。

---

## 模块导航（系统/运维）

1. 通知：`/api/notifications/*`
2. 系统管理：`/api/system/*`
3. 外部合同接入：`/api/external/contracts/*`
4. 合同分类：`/api/contract-categories/*`、`/api/categories/*`
5. 条款库：`/api/clauses/*`
6. 相对方：`/api/contract-counterparties/*`
7. 变更日志：`/api/contracts/{contractId}/change-logs/*`、`/api/change-logs/*`
8. 到期预警：`/api/contracts/expiring`、`/api/contracts/statistics/expiration`
9. 管理初始化：`/api/admin/init-permissions`

---

## 适用场景

- 外部系统写入合同、批量导入
- 后台系统配置和会话治理
- 运营侧通知管理
- 数据治理（分类、条款、相对方、变更记录）

---

## 详细内容位置

- 对应完整定义见 `API_DOC.md` 第 12 章与第 14 章。
- 常规业务联调请看 `API_BUSINESS.md`。

---

## 最小维护流程

1. 新增/调整系统接口后，先更新本文导航与说明。
2. 运行 `python3 scripts/scan-controller-routes.py`，核对实际路径。
3. 迭代收口时同步更新 `API_DOC.md` 的系统相关章节。
