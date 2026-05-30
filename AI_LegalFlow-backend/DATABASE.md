# LegalFlow 数据库配置说明

## 📊 数据库方案

### 开发模式（推荐）：H2 内存数据库
- ✅ **无需安装任何数据库**
- ✅ **开箱即用**
- ✅ **自动创建表**
- ✅ **内置 Web 控制台**

### 生产模式：PostgreSQL
- 完整功能
- 数据持久化
- 高可用

---

## 🚀 快速开始（H2）

### 1. 启动应用

应用启动时会：
1. 自动创建 H2 内存数据库
2. 自动创建所需的表
3. 启动 H2 Web 控制台

### 2. 访问 H2 控制台

打开浏览器访问：
```
http://localhost:8080/h2-console
```

### 3. 登录 H2 控制台

| 配置项 | 值 |
|--------|-----|
| JDBC URL | `jdbc:h2:mem:legalflow` |
| 用户名 | `sa` |
| 密码 | (留空) |
| 驱动 | org.h2.Driver |

点击 **Connect** 即可连接！

---

## 📋 数据库表结构

### 1. task - 任务表
存储用户提交的法律任务和执行结果

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| task_id | VARCHAR(255) | 任务ID（UUID） |
| user_message | TEXT | 用户原始请求 |
| task_type | VARCHAR(100) | 任务类型 |
| status | VARCHAR(50) | 状态（CREATED/ANALYZING/COMPLETED） |
| result_message | TEXT | 结果消息 |
| result_data | TEXT | JSON格式结果数据 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 2. agent_log - 智能体日志表
记录每个智能体的执行过程

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| task_id | VARCHAR(255) | 关联的任务ID |
| agent_name | VARCHAR(100) | 智能体名称 |
| action_type | VARCHAR(100) | 操作类型 |
| input_data | TEXT | 输入数据（JSON） |
| output_data | TEXT | 输出数据（JSON） |
| execution_time_ms | BIGINT | 执行耗时（毫秒） |
| created_at | TIMESTAMP | 创建时间 |

### 3. feedback - 反馈表
收集用户对结果的反馈

| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| task_id | VARCHAR(255) | 关联的任务ID |
| rating | INT | 评分（1-5） |
| comment | TEXT | 评论内容 |
| created_at | TIMESTAMP | 创建时间 |

---

## 🔄 切换到 PostgreSQL

### 1. 创建 PostgreSQL 数据库

```sql
CREATE DATABASE legalflow WITH ENCODING 'UTF8' TEMPLATE template0;
```

### 2. 修改 application.yml

将 **H2 配置**注释掉，启用 **PostgreSQL 配置**：

```yaml
spring:
  datasource:
    # 注释 H2 配置
    # url: jdbc:h2:mem:legalflow;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL
    # username: sa
    # password:
    # driver-class-name: org.h2.Driver

    # 启用 PostgreSQL 配置
    url: jdbc:postgresql://localhost:5432/legalflow
    username: postgres
    password: your-postgres-password
    driver-class-name: org.postgresql.Driver

  # 注释 H2 控制台
  # h2:
  #   console:
  #     enabled: true
  #     path: /h2-console
```

---

## 💡 常用 H2 控制台命令

### 查看所有表
```sql
SHOW TABLES;
```

### 查询任务
```sql
SELECT * FROM task ORDER BY created_at DESC;
```

### 查看智能体日志
```sql
SELECT * FROM agent_log WHERE task_id = 'your-task-id';
```

---

## 🎯 当前配置状态

- ✅ **已配置 H2 内存数据库（默认）**
- ✅ **已添加 H2 依赖**
- ✅ **已启用 H2 控制台**
- ✅ **Hibernate DDL auto: update** - 自动更新表结构
