# 基于情感分析的校园论坛帖子智能管理系统 —— 环境配置说明

> 学号：202432110119　姓名：刘彦博　班级：软外241
> GitHub 仓库地址：**【请在此处填写你的 GitHub 链接】**

本系统采用前后端分离架构：后端 Spring Boot 3.2 提供 RESTful API，前端 Vue3 + Vite，
数据库使用 MySQL 8，情感分析能力由百度 NLP 情感分析接口提供。

---

## 一、运行环境要求

| 类别 | 要求 |
| --- | --- |
| JDK | Java 17 |
| 构建工具 | Maven 3.8+ |
| 数据库 | MySQL 8.0（字符集 utf8mb4） |
| Node.js | 18+（含 npm） |
| 第三方服务 | 百度智能云 NLP 情感分析接口（需自备 API Key / Secret Key） |

---

## 二、目录结构

```
├── backend/     后端源码与 SQL 脚本（Spring Boot）
│   ├── sql/     数据库初始化脚本（init.sql、campus_seed_data.sql）
│   ├── src/     Java 源码
│   └── pom.xml  Maven 依赖配置
├── frontend/    前端源码与构建配置（Vue3 + Vite）
├── deploy/      部署配置示例（application-prod.yml.example、nginx.conf.example）
├── scripts/     部署脚本（deploy-backend.ps1、deploy-frontend.ps1）
├── README.md    环境配置说明（本文件）
└── 作品文件清单.txt
```

## 三、数据库配置

1. 启动 MySQL 8，使用 root（或自建账号）登录。
2. 导入初始化脚本（脚本会自动创建数据库 `forum_db` 并建表）：
   ```bash
   mysql -u root -p < backend/sql/init.sql
   ```
3. （可选）导入示例数据：
   ```bash
   mysql -u root -p forum_db < backend/sql/campus_seed_data.sql
   ```

> 数据库名默认为 `forum_db`，字符集 `utf8mb4`。如需修改，请同步修改下方后端配置中的 `datasource.url`。

## 四、后端配置与运行

后端配置文件：`backend/src/main/resources/application.yml`，运行前请按本机环境修改以下关键项：

| 配置项 | 说明 |
| --- | --- |
| `spring.datasource.url` | 数据库连接地址，默认 `jdbc:mysql://localhost:3306/forum_db...` |
| `spring.datasource.username` / `password` | 数据库账号 / 密码 |
| `jwt.secret` | JWT 签名密钥（请自行设置足够长的随机字符串） |
| `jwt.expiration` | Token 有效期（毫秒），默认 86400000（24 小时） |
| `baidu.nlp.api-key` / `baidu.nlp.secret` | 百度 NLP 的 API Key / Secret Key |
| `system.warning.threshold` | 预警阈值，默认 0.7（负向概率超过则触发预警） |
| `system.warning.ban-count` | 自动封禁阈值，默认 3（累计违规达到则封禁） |

启动后端（默认端口 `8080`）：
```bash
cd backend
mvn clean package        # 打包
mvn spring-boot:run      # 或：java -jar target/forum-1.0.0.jar
```

## 五、前端配置与运行

```bash
cd frontend
npm install              # 安装依赖
npm run dev              # 本地开发，默认 http://localhost:5173
npm run build            # 生产构建，产物在 dist/
```

> 若后端地址 / 端口有变动，请检查 `frontend/vite.config.ts` 中的代理配置或 `src` 下的接口基础地址。

## 六、部署说明

- `deploy/backend/application-prod.yml.example`：生产环境后端配置模板，复制为 `application-prod.yml` 并填入正式参数。
- `deploy/nginx.conf.example`：Nginx 反向代理与前端静态资源托管示例。
- `scripts/deploy-backend.ps1`、`scripts/deploy-frontend.ps1`：Windows PowerShell 部署脚本。

## 七、启动顺序

1. 启动 MySQL 并完成第三节的脚本导入；
2. 配置并启动后端（第四节）；
3. 安装依赖并启动前端（第五节）；
4. 浏览器访问前端地址即可使用。

---

## ⚠️ 提交前安全提醒

当前 `application.yml` 中含有**真实的数据库密码、JWT 密钥、百度 NLP API Key/Secret**。
对外提交 / 上传 GitHub 前，建议将这些值替换为占位符（如 `your-db-password`、
`your-baidu-api-key`），避免密钥泄露。
