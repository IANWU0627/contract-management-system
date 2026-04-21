# 合同管理系统

一个覆盖合同全生命周期的管理系统，支持合同管理、模板管理、审批流、到期提醒、统计分析、多语言和权限控制。

当前项目以可维护性和可扩展性为目标，支持后续对接企业业务数据、电子签章和 OA 审批流。

## ✨ 功能特性

- 📋 **合同管理** - 合同的创建、编辑、查看、删除
- 📝 **模板管理** - 支持自定义模板和变量
- 🔄 **审批流程** - 完整的合同审批工作流
- ⏰ **到期提醒** - 智能提醒和规则配置
- ⭐ **收藏管理** - 快速访问重要合同
- 🏷️ **标签管理** - 灵活的合同分类
- 🔢 **快速代码** - 系统配置快速代码管理
- 📊 **模板变量** - 动态模板变量配置
- 👥 **用户角色** - 完善的权限管理
- 🌍 **多语言** - 支持中文和英文
- 📈 **统计报表** - 数据可视化分析
- 🤖 **AI分析** - 智能合同内容分析（列表侧不依赖正文字段；详情/载荷按需加载）
- 📦 **版本管理** - 合同变更历史；支持版本对比与审批快照对比（条款粒度 + 风险说明）

### 合同 AI 分析接口说明

- **推荐**：`POST /api/contracts/{id}/analyze`（与前端列表、用户配置、无 API 时的离线演示一致）。
- **兼容**：`POST /api/ai/analyze/{id}` 与上者共用同一套助手逻辑，新集成请优先使用合同路径。

## 🚀 快速开始

### 环境要求

- **Node.js**: 18.x 或更高
- **Java**: 21 或更高
- **Maven**: 3.6+（或使用项目自带的mvnw）
- **MySQL**: 8.0+（当前默认开发配置）

### 默认方式：使用 MySQL（与当前代码配置一致）

1. **创建数据库**
   ```sql
   CREATE DATABASE contract_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. **按需修改配置**
   编辑 `backend/src/main/resources/application.yml`，确认数据库连接参数
3. **启动后端**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
4. **启动前端**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

5. **访问应用**
   - 前端地址: <http://localhost:3000>
   - 后端地址: <http://localhost:8081>
   - 默认账号: admin / admin123

> 说明：历史版本曾提供 H2 本地开发方案；当前仓库默认配置为 MySQL。若需启用 H2，请以实际 profile 配置和代码为准，不建议直接按旧文档操作。

详细说明请参考 [QUICKSTART.md](./QUICKSTART.md)

## 🛠️ 技术栈

### 前端

- Vue 3 + TypeScript
- Vite
- Element Plus
- Vue Router
- Pinia
- Vue I18n
- AntV G2Plot（工作台与统计页图表）

### 后端

- Java 21
- Spring Boot
- Spring Security + JWT
- MyBatis Plus
- MySQL / H2
- Maven

## 📁 项目结构

```
contract-management-system/
├── frontend/              # 前端项目
│   ├── src/
│   │   ├── views/       # 页面组件
│   │   ├── components/  # 通用组件
│   │   ├── api/        # API调用
│   │   └── locales/    # 国际化文件
│   └── package.json
├── backend/              # 后端项目
│   ├── src/
│   │   └── main/
│   │       ├── java/    # Java源代码
│   │       └── resources/ # 配置文件
│   └── pom.xml
├── docs/                 # 项目文档（见 docs/README.md 索引）
├── README.md             # 本文件
└── QUICKSTART.md         # 快速启动指南
```

## 🔐 默认账号

| 用户名   | 密码       | 角色    |
| ----- | -------- | ----- |
| admin | admin123 | 超级管理员 |

## 📝 开发文档

- [文档索引](./docs/README.md)
- [快速启动指南](./QUICKSTART.md)
- [业务 API 入口](./docs/API_BUSINESS.md)
- [系统/运维 API 入口](./docs/API_SYSTEM.md)
- [控制器分层说明](./docs/CONTROLLER_LAYERING.md)
- [API 文档](./docs/API_DOC.md)
- [数据库说明](./docs/DATABASE.md)
- [技术规格](./docs/SPEC.md)

## 🧹 维护说明

- **大字段**：合同正文、模板变量、附件等存于 `contract_payload`；列表接口返回精简字段，详情页通过 `GET /api/contracts/{id}/payload` 加载。
- **快照**：审批冻结内容存 `contract_snapshot`；对比接口见 [API 文档](./docs/API_DOC.md)（快照对比、版本对比）。
- **数据库**：升级请使用 `backend/src/main/resources/db/migration/` 下 Flyway 脚本；若历史库缺列，优先对照 `DATABASE.md` 与 `V1.14.0__Ensure_diff_analysis_schema_compat.sql` 等幂等脚本。

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目仅供学习和研究使用。

## 💡 其他

如有问题，请查看 [QUICKSTART.md](./QUICKSTART.md) 中的常见问题部分。
