# 控制器分层与职责说明（ContractController）

本文档用于说明 `ContractController` 在拆分后的职责边界，减少后续“控制器回胖”风险。

## 1. 分层原则

- 控制器只做：路由绑定、鉴权注解、参数接收、统一响应包装。
- 服务层承接：业务规则、状态流转、跨表更新、复杂参数解析。
- Mapper/Repository 仅做数据访问，不承载业务编排。

## 2. 当前职责归属

### 2.1 保留在 `ContractController`

- 基础路由与响应包装：`/api/contracts/*`
- 合同查询与详情编排入口（委托 `ContractService`）
- 绩效里程碑相关入口（委托 `ContractPerformanceMilestoneService`）
- 单次导出 PDF（`POST /api/contracts/generate-pdf`）的轻量编排

### 2.2 拆分到独立服务

- `ContractCommandService`
  - `POST /api/contracts`
  - `PUT /api/contracts/{id}`
  - `DELETE /api/contracts/{id}`
  - `POST /api/contracts/{id}/copy`
- `ContractWorkflowService`
  - 单合同状态流转（提交、审批、拒绝、撤回、签署、归档、终止、续签）
  - 批量操作（批量删除、批量改状态、批量审批、批量提交、批量编辑）
- `ContractFileService`
  - 附件上传、下载、删除
- `ContractExportService`
  - 合同导出（列表导出、Excel、PDF、Word）
- `ContractCollaborationService`
  - 评论与审批历史
- `ContractAiGatewayService`
  - 合同 AI 分析入口与配置联动

## 3. 新增接口的归属规则

- 若接口会改变合同主状态或审批流：放到 `ContractWorkflowService`。
- 若接口是合同增删改复制命令：放到 `ContractCommandService`。
- 若接口涉及文件系统或对象存储：放到 `ContractFileService`。
- 若接口涉及导出渲染与下载流：放到 `ContractExportService`。
- 若接口涉及评论、协作、通知联动：放到 `ContractCollaborationService`。
- 若接口涉及模型调用、提示词、AI 配置：放到 `ContractAiGatewayService`。

## 4. 开发维护清单

每次变更建议按以下顺序执行：

1. 修改对应 Service，控制器仅补充路由转发。
2. 更新 API 文档入口：
   - 业务域优先更新 `API_BUSINESS.md`
   - 系统域优先更新 `API_SYSTEM.md`
3. 若控制器路由有变化，执行：
   - `python3 scripts/scan-controller-routes.py > scripts/controller-routes.baseline.tsv`
   - 提交基线文件
4. 本地编译验证：
   - 前端：`npm run build`
   - 后端：`mvn -q -DskipTests compile`

## 5. 目标与约束

- 目标：保持控制器薄、服务职责清晰、文档与代码同步。
- 约束：避免在控制器中引入业务分支、数据库操作或复杂对象转换。
