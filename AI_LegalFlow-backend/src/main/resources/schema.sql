-- ======================================
-- LegalFlow 数据库初始化脚本
-- ======================================
-- 说明：
--  - 使用H2内存数据库时，Hibernate会自动创建表
--  - 使用PostgreSQL时，需要先创建数据库
-- ======================================

-- 如果使用PostgreSQL，请先执行以下建库语句：
-- CREATE DATABASE legalflow WITH ENCODING 'UTF8' TEMPLATE template0;

-- ======================================
-- 示例：手动创建表结构（如果不使用Hibernate自动创建）
-- ======================================

-- 任务表
-- CREATE TABLE task (
--     id BIGINT PRIMARY KEY AUTO_INCREMENT,
--     task_id VARCHAR(255) UNIQUE,
--     user_message TEXT,
--     task_type VARCHAR(100),
--     status VARCHAR(50),
--     result_message TEXT,
--     result_data TEXT,
--     created_at TIMESTAMP,
--     updated_at TIMESTAMP
-- );

-- 智能体日志表
-- CREATE TABLE agent_log (
--     id BIGINT PRIMARY KEY AUTO_INCREMENT,
--     task_id VARCHAR(255),
--     agent_name VARCHAR(100),
--     action_type VARCHAR(100),
--     input_data TEXT,
--     output_data TEXT,
--     execution_time_ms BIGINT,
--     created_at TIMESTAMP
-- );

-- 反馈表
-- CREATE TABLE feedback (
--     id BIGINT PRIMARY KEY AUTO_INCREMENT,
--     task_id VARCHAR(255),
--     rating INT,
--     comment TEXT,
--     created_at TIMESTAMP
-- );

-- ======================================
-- 开发模式使用说明
-- ======================================
-- 1. 应用启动时，Hibernate会根据 @Entity 注解自动创建表
-- 2. 访问 H2 控制台：http://localhost:8080/h2-console
-- 3. JDBC URL: jdbc:h2:mem:legalflow
-- 4. 用户名: sa
-- 5. 密码: (留空)
-- ======================================
