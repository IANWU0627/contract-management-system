# 合同管理系统 - 快速启动指南

## 🚀 环境要求

- **Node.js**: 18.x 或更高
- **Java**: 21 或更高
- **Maven**: 3.6+（或使用项目自带的mvnw）
- **MySQL**: 8.0+（当前默认开发配置）

## 📦 快速启动

### 默认方式：使用 MySQL（与当前代码配置一致）

1. **创建数据库**
   ```sql
   CREATE DATABASE contract_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **修改配置**
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
   - 前端地址: http://localhost:3000
   - 后端地址: http://localhost:8081
   - 默认账号: admin / admin123

> 说明：历史版本曾提供 H2 本地开发方案；当前仓库默认配置为 MySQL。若需启用 H2，请基于实际 profile 自行切换。

## 🔧 常用命令

### 前端命令
```bash
cd frontend

# 启动开发服务器
npm run dev

# 类型检查
npm run type-check

# 构建生产版本
npm run build

# 预览生产构建
npm run preview

# 清理构建和依赖
npm run clean
```

### 后端命令
```bash
cd backend

# 使用Maven启动
./mvnw spring-boot:run

# 清理并重新编译
./mvnw clean install

# 运行测试
./mvnw test

# 跳过测试打包
./mvnw clean package -DskipTests
```

## 📋 项目结构

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
├── docs/                 # 项目文档（索引见 docs/README.md）
└── QUICKSTART.md         # 本文件
```

## 📚 相关文档

- [文档索引](./docs/README.md)
- [API 文档](./docs/API_DOC.md)
- [数据库说明](./docs/DATABASE.md)

## 🎯 主要功能

- ✅ 合同管理（创建、编辑、查看、删除）
- ✅ 模板管理（支持变量）
- ✅ 合同审批流程
- ✅ 到期提醒
- ✅ 收藏管理
- ✅ 标签管理
- ✅ 快速代码管理
- ✅ 模板变量管理
- ✅ 用户和角色管理
- ✅ 多语言支持（中文/英文）
- ✅ 合同版本管理
- ✅ 变更记录
- ✅ 统计报表
- ✅ AI智能分析

## 🔐 默认登录账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin  | admin123 | 超级管理员 |

## 💡 开发建议

1. **使用Git进行版本控制**
2. **先在H2数据库上开发**
3. **提交前运行类型检查**
4. **遵循代码规范**

## 🐛 常见问题

### 前端启动失败
- 检查端口3000是否被占用
- 删除 `node_modules` 后重新 `npm install`

### 后端启动失败
- 检查Java版本是否为21+
- 检查数据库连接配置
- 查看 `logs/application.log` 日志文件

### 端口被占用
修改相应的配置文件中的端口号

---

如有问题，请查看项目文档或联系技术支持！
