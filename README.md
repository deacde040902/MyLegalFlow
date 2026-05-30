# LegalFlow - 法律智能助手

## 项目简介

LegalFlow 是一个基于多智能体协同架构的法律工作流系统，采用前后端分离设计，提供合同风险分析、法律问答、知识库管理等功能。

## 🎯 核心功能

### 1. 合同风险分析
- 自动识别合同中的高风险条款
- 分为高/中/低三个风险等级
- 提供具体的修改建议和法律依据
- 支持追问获取详细建议

### 2. 法律智能问答
- 支持对话记忆的智能问答系统
- 提供专业的法律建议和维权途径
- 支持工资、社保、竞业限制等常见劳动法问题
- 专业法律助手人设与开场白

### 3. 文件解析
- 支持 PDF、Word、TXT、图片等多种格式
- 自动解析合同内容进行风险分析
- 文件上传和内容识别

### 4. 知识库管理
- 法律法规、合同条款、案例的存储和检索
- 知识库统计和分类管理

### 5. 任务管理
- 合同审查和法律分析任务的管理
- 任务状态跟踪和历史记录

## 🛠️ 技术栈

### 后端
- **框架**: Spring Boot 3.x
- **AI框架**: Spring AI 1.1
- **数据库**: H2 Database (开发环境) / PostgreSQL (生产环境)
- **文档解析**: Apache Tika + PDFBox + Apache POI
- **架构**: 多智能体协同

### 前端
- **框架**: Vue 3
- **构建工具**: Vite
- **UI组件**: Element Plus
- **样式**: Tailwind CSS
- **路由**: Vue Router

### AI架构
- **OrchestratorAgent**: 协调者Agent，负责任务分发和结果整合
- **ClauseExtractorAgent**: 条款抽取Agent
- **LawRetrieverAgent**: 法规检索Agent
- **RiskAnalyzerAgent**: 风险识别Agent
- **SuggestionGeneratorAgent**: 修改建议Agent
- **ReportBuilderAgent**: 报告生成Agent

## 💡 特色功能

### 合同风险智能识别
系统会自动识别以下风险类型：

**高风险项**：
- 违约金过高
- 验收标准不明确（如"甲方满意为最终标准"）
- 最终解释权条款
- 永久保密义务
- 单方解除权条款
- 免除法定责任

**中风险项**：
- 需求变更不增加费用
- 个人收款账户
- 管辖法院约定

**低风险项**：
- 争议解决方式
- 期限约定

### 对话记忆与追问
- 支持对话上下文记忆
- 可追问"改造中高风险条款"获取修改建议
- 可追问"总结主要风险"查看风险总结

### 专业法律助手
- 专业的人设和开场白
- 针对不同问题提供专业回答
- 包含维权途径和证据准备建议

## 📦 安装部署

### 后端部署

```bash
cd AI_LegalFlow-backend

# 使用 Maven 构建
mvn clean install

# 运行应用
java -jar target/legalflow-core-1.0.0-SNAPSHOT.jar
```

### 前端部署

```bash
cd AI_LegalFlow-frontend

# 安装依赖
npm install

# 开发模式运行
npm run dev

# 生产构建
npm run build
```

## 🔧 配置说明

### 后端配置
编辑 `AI_LegalFlow-backend/src/main/resources/application.yml`：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:legalflow
    driver-class-name: org.h2.Driver
```

### 前端配置
前端默认连接后端 `http://localhost:8080`，可在 `src/api/index.js` 中修改。

## 📸 项目截图

### 首页



### 法律问答
<img width="1230" height="831" alt="image" src="https://github.com/user-attachments/assets/50d96ad5-449b-45e5-84ba-0a5907c5a911" />

### 合同风险分析
<img width="1252" height="855" alt="image" src="https://github.com/user-attachments/assets/a1d6d464-ec2e-466c-a8eb-564fd85e1cea" />

### 知识库
![Uploading image.png…]()

## ⚠️ 免责声明

本系统仅提供参考意见，不构成正式法律建议。具体法律问题建议咨询专业律师。

## 📄 许可证

MIT License

## 👨‍💻 作者
deacde040902
