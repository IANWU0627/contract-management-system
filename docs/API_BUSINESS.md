# 合同管理系统 - API（业务接口）

> 面向产品业务联调：认证、合同、模板、变量、用户、角色、提醒、收藏标签、文件夹、统计。

---

## 使用说明

- 本文是业务接口的快速入口，便于日常联调与排查。
- 完整字段、示例与补充说明请参考 `API_DOC.md` 对应章节。
- 若本文与代码有冲突，以 `backend/src/main/java/com/contracthub/controller` 中注解映射为准。
- 业务接口发生变更时，优先更新本文，再在版本迭代时回填 `API_DOC.md`。

---

## 章节导航（业务）

1. 认证：`/api/auth/*`
2. 合同管理：`/api/contracts/*`、`/api/contracts/{contractId}/versions/*`
3. 模板管理：`/api/templates/*`
4. 变量管理：`/api/template-variables/*`
5. 用户管理：`/api/users/*`
6. 角色权限：`/api/roles/*`、`/api/permissions/*`
7. 提醒管理：`/api/reminders/*`、`/api/reminder-rules/*`
8. 收藏与标签：`/api/favorites/*`、`/api/tags/*`
9. 文件夹：`/api/contract-folders/*`
10. 统计报表：`/api/statistics/*`

---

## 推荐阅读顺序

1. 先看认证与权限（登录、角色、权限）
2. 再看合同主链路（合同 + 审批 + 版本 + 导出）
3. 最后看辅助模块（提醒、标签、统计、文件夹）

---

## 详细内容位置

- 认证、合同、模板、变量、用户、角色、提醒、收藏标签、文件夹、统计：
  - 见 `API_DOC.md` 第 2 到第 11 章与第 13 章相关内容
- 若需要系统/运维类接口：
  - 见 `API_SYSTEM.md`

---

## 最小维护流程

1. 修改接口后，先更新本文对应模块条目。
2. 运行 `python3 scripts/scan-controller-routes.py`，确认路径无遗漏。
3. 在提测或发布前，将变更同步到 `API_DOC.md`（完整参考）。
