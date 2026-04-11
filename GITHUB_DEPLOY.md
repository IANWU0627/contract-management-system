# GitHub部署指南

## 📋 本地Git仓库已准备完毕！

恭喜！你的项目已经成功初始化Git仓库并提交了代码！

## 🚀 下一步：推送到GitHub

### 步骤1：在GitHub上创建新仓库

1. 访问 https://github.com/new
2. 填写仓库信息：
   - **Repository name**: `contract-management-system` (或你喜欢的名称)
   - **Description**: 合同管理系统 - 一个功能完整的企业级合同管理系统
   - **Public/Private**: 选择公开或私有
   - ❌ **不要**勾选 "Initialize this repository with a README"
   - ❌ **不要**勾选 "Add .gitignore"
   - ❌ **不要**勾选 "Choose a license"
3. 点击 **Create repository**

### 步骤2：关联远程仓库并推送

创建仓库后，GitHub会显示一些命令。请执行以下命令：

```bash
# 关联远程仓库（替换 YOUR_USERNAME 为你的GitHub用户名）
git remote add origin https://github.com/YOUR_USERNAME/contract-management-system.git

# 重命名分支为 main（可选，GitHub现在默认使用main）
git branch -M main

# 推送到GitHub
git push -u origin main
```

### 步骤3：如果使用SSH（推荐）

如果你已经配置了SSH密钥，可以使用SSH地址：

```bash
# 使用SSH关联远程仓库
git remote add origin git@github.com:YOUR_USERNAME/contract-management-system.git

# 推送到GitHub
git push -u origin main
```

## 🔑 配置Git用户信息（如果还没配置）

如果你是第一次使用Git，需要配置用户信息：

```bash
# 配置全局用户信息
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# 或者只配置当前仓库
git config user.name "Your Name"
git config user.email "your.email@example.com"
```

## 📝 后续提交代码

以后修改代码后，提交和推送的流程：

```bash
# 查看修改状态
git status

# 添加修改的文件
git add .

# 提交代码
git commit -m "描述你的修改"

# 推送到GitHub
git push
```

## 🎯 常用Git命令

| 命令 | 说明 |
|------|------|
| `git status` | 查看当前状态 |
| `git add .` | 添加所有修改 |
| `git add <file>` | 添加指定文件 |
| `git commit -m "message"` | 提交代码 |
| `git push` | 推送到远程 |
| `git pull` | 从远程拉取 |
| `git log` | 查看提交历史 |
| `git diff` | 查看修改内容 |

## ⚠️ 注意事项

1. **敏感信息**：确保不要提交密码、API密钥等敏感信息
2. **.gitignore**：已经配置好，会自动忽略不必要的文件
3. **大文件**：不要提交大文件到Git仓库
4. **提交信息**：写清楚提交信息，方便回溯

## 📚 更多帮助

- GitHub官方文档：https://docs.github.com
- Git官方文档：https://git-scm.com/doc
- Git入门教程：https://guides.github.com/introduction/git-handbook/

---

准备好了吗？去GitHub创建仓库吧！🎉
