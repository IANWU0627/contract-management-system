# Frontend

合同管理系统前端，基于 Vue 3 + TypeScript + Vite + Element Plus。

项目级文档见仓库根目录 [README.md](../README.md) 与 [docs/README.md](../docs/README.md)。

## 开发命令

```bash
npm install
npm run dev
```

## 可用脚本

- `npm run dev`：本地开发
- `npm run build`：类型检查并构建生产包
- `npm run preview`：本地预览生产构建
- `npm run type-check`：仅执行 TS 类型检查
- `npm run clean`：清理构建产物与依赖目录

## 目录建议

- `src/views`：页面级组件
- `src/components`：通用组件
- `src/api`：接口请求封装
- `src/locales`：多语言资源
- `src/utils`：工具函数

## 维护约定

- 新增页面优先使用 `script setup + TypeScript`
- 多语言文案统一放在 `src/locales/zh.ts`
- 样式优化优先采用“低干扰、可读性优先”的视觉策略
