# 合同管理系统

一个功能完整的企业级合同管理系统，支持合同全生命周期管理、模板管理、审批流程、到期提醒等功能。

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
- 🤖 **AI分析** - 智能合同内容分析
- 📦 **版本管理** - 合同变更历史记录

## 🚀 快速开始

### 环境要求

- **Node.js**: 18.x 或更高
- **Java**: 17 或更高
- **Maven**: 3.6+（或使用项目自带的mvnw）
- **MySQL**: 8.0+（可选，内置H2数据库可用于开发）

### 方式一：使用内置H2数据库（推荐开发用）

1. **启动后端**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
   或Windows:
   ```bash
   cd backend
   mvnw.cmd spring-boot:run
   ```

2. **启动前端**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

3. **访问应用**
   - 前端地址: http://localhost:3000
   - 后端地址: http://localhost:8081
   - 默认账号: admin / admin123

### 方式二：使用MySQL数据库

1. **创建数据库**
   ```sql
   CREATE DATABASE contract_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **修改配置**
   编辑 `backend/src/main/resources/application.yml`，配置数据库连接

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

详细说明请参考 [QUICKSTART.md](./QUICKSTART.md)

## 🛠️ 技术栈

### 前端
- Vue 3 + TypeScript
- Vite
- Element Plus
- Vue Router
- Pinia
- Vue I18n
- AntV G2Plot

### 后端
- Java 17
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
├── docs/                 # 项目文档
├── README.md             # 本文件
└── QUICKSTART.md         # 快速启动指南
```

## 🔐 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin  | admin123 | 超级管理员 |

## 📝 开发文档

- [快速启动指南](./QUICKSTART.md)
- [API文档](./docs/API.md) (待添加)
- [数据库设计](./docs/DATABASE.md) (待添加)

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目仅供学习和研究使用。

## 💡 其他

如有问题，请查看 [QUICKSTART.md](./QUICKSTART.md) 中的常见问题部分。
