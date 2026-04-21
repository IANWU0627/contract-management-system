# 项目文档索引

本目录存放合同管理系统的说明与规格文档，与根目录 `README.md`（项目总览）配合使用。

| 文档 | 说明 |
|------|------|
| [API_BUSINESS.md](./API_BUSINESS.md) | 业务联调 API 入口（认证、合同、模板、用户、统计等） |
| [API_SYSTEM.md](./API_SYSTEM.md) | 系统/运维 API 入口（通知、系统配置、外部接入等） |
| [API_DOC.md](./API_DOC.md) | REST API 路径、参数与响应约定 |
| [CONTROLLER_LAYERING.md](./CONTROLLER_LAYERING.md) | `ContractController` 控制器分层、接口职责归属与维护清单 |
| [DATABASE.md](./DATABASE.md) | 数据库表、Flyway 迁移与演进说明 |
| [QUICKSTART.md](../QUICKSTART.md) | 环境要求与本地启动步骤（根目录） |
| [SPEC.md](./SPEC.md) | 技术架构与规格说明 |
| [PRD.md](./PRD.md) | 产品需求说明 |
| [USER_GUIDE.md](./USER_GUIDE.md) | 使用说明（面向最终用户时可扩充） |

## 辅助脚本（与文档对账）

| 脚本 | 说明 |
|------|------|
| [../scripts/scan-controller-routes.py](../scripts/scan-controller-routes.py) | 扫描 `controller` 包下 `@*Mapping`，输出 `METHOD<TAB>path<TAB>文件`，供与 [API_DOC.md](./API_DOC.md) 核对 |

## API 文档维护约定

- 业务接口改动：先改 `API_BUSINESS.md`
- 系统/运维接口改动：先改 `API_SYSTEM.md`
- 版本收口时：统一回填 `API_DOC.md` 作为完整参考
- CI 会运行 `scripts/check-controller-routes-baseline.py`；若控制器路由变化，请更新 `scripts/controller-routes.baseline.tsv`

## 近期数据模型要点（便于联调）

- **合同主表 `contract`**：存元数据（编号、状态、金额等）；大字段已拆出。
- **`contract_payload`**：正文、模板变量 JSON、动态字段、附件等大字段。
- **`contract_snapshot`**：审批/审计用不可变快照（提交审批等场景写入）。
- **`contract_ai_summary`**：审批摘要等 AI 辅助结果（可按合同/快照维度缓存）。
- **`contract_version_diff_analysis` / `contract_snapshot_diff_analysis`**：版本对比、快照对比的规则/缓存结果（含 `diff_json`、`risk_json`）。

迁移脚本目录：`backend/src/main/resources/db/migration/`。
