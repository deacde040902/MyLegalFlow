# LegalFlow TODO 功能实现清单

## 📋 实现状态

### ✅ 已完成
- [x] DTO类（TaskPlan, TaskResult, Clause, Risk等）
- [x] Entity实体类（Task, AgentLog, Feedback）
- [x] Repository接口
- [x] Agent基类（BaseAgent, Agent接口）
- [x] KnowledgeBaseService（知识库加载和检索）
- [x] TaskService（任务管理）
- [x] OrchestratorAgent（协调者Agent）
- [x] LawRetrieverAgent（法规检索Agent）

### 🔄 进行中
- [ ] ClauseExtractorAgent（条款抽取Agent）
- [ ] RiskAnalyzerAgent（风险识别Agent）
- [ ] SuggestionGeneratorAgent（修改建议Agent）
- [ ] ReportBuilderAgent（报告生成Agent）

### ⏳ 待实现
- [ ] 配置类（Redis, RabbitMQ）
- [ ] Controller层（TaskController, KnowledgeController）
- [ ] 文档解析服务
- [ ] Spring AI集成

---

## 📝 待实现功能详细说明

### 1. ClauseExtractorAgent（条款抽取Agent）

**当前状态：** 框架已有，需要实现具体逻辑

**待实现功能：**
- 使用Apache Tika解析PDF/Word文档
- 使用LLM识别条款结构
- 提取条款名称、条款内容、条款类型
- 返回结构化条款列表

**实现建议：**
```java
@Override
protected List<Clause> doExecute(DocumentInput input) {
    // 1. 使用Tika解析文档
    String text = parseDocument(input);
    
    // 2. 使用LLM识别条款结构
    List<Clause> clauses = extractClausesWithLLM(text);
    
    return clauses;
}
```

---

### 2. RiskAnalyzerAgent（风险识别Agent）

**当前状态：** 框架已有，需要实现具体逻辑

**待实现功能：**
- 接收条款列表和法规引用
- 分析每个条款的法律风险
- 评估风险等级（高、中、低）
- 生成风险报告

**风险识别规则：**
| 风险类型 | 风险等级 | 识别特征 |
|---------|---------|---------|
| 竞业限制过宽 | 高 | 范围过广、期限过长、无补偿 |
| 违约金过高 | 高 | 超过法定标准 |
| 霸王条款 | 高 | 排除对方权利、加重对方责任 |
| 模糊条款 | 中 | 表述不明确、易产生争议 |
| 常规风险 | 低 | 正常商业风险 |

---

### 3. SuggestionGeneratorAgent（修改建议Agent）

**当前状态：** 框架已有，需要实现具体逻辑

**待实现功能：**
- 基于风险分析结果
- 生成条款修改建议
- 提供多个备选方案
- 说明修改理由和法律依据

**建议类型：**
- 删除条款
- 修改条款表述
- 添加补充条款
- 提供替代方案

---

### 4. ReportBuilderAgent（报告生成Agent）

**当前状态：** 框架已有，需要实现具体逻辑

**待实现功能：**
- 汇总所有Agent的结果
- 生成结构化报告
- 支持Markdown/PDF格式
- 包含执行摘要和详细分析

**报告结构：**
```markdown
# 合同审查报告

## 执行摘要
## 条款分析
## 风险识别
## 修改建议
## 法规参考
## 结论
```

---

### 5. 配置类

#### RedisConfig
- 配置Redis连接
- 实现缓存功能
- 会话管理

#### RabbitMQConfig
- 配置消息队列
- 实现异步任务
- Agent间通信

---

### 6. Controller层

#### TaskController
- `POST /api/tasks` - 创建任务
- `GET /api/tasks/{id}` - 查询任务
- `POST /api/tasks/{id}/execute` - 执行任务
- `GET /api/tasks` - 查询所有任务

#### KnowledgeController
- `GET /api/knowledge/stats` - 知识库统计
- `GET /api/knowledge/search` - 知识检索
- `GET /api/knowledge/documents` - 文档列表

#### FeedbackController
- `POST /api/feedback/{taskId}` - 提交反馈
- `GET /api/feedback/{taskId}` - 查询反馈

---

### 7. 文档解析服务

#### DocumentParserService
- 解析PDF文档
- 解析Word文档
- 提取文本内容
- 格式转换

---

## 🎯 快速测试建议

### 测试步骤：
1. **启动应用**
   ```bash
   mvn spring-boot:run
   ```

2. **测试知识库检索**
   ```bash
   curl "http://localhost:8080/api/knowledge/search?query=竞业限制&topK=5"
   ```

3. **创建任务**
   ```bash
   curl -X POST http://localhost:8080/api/tasks \
     -H "Content-Type: application/json" \
     -d '{"message":"劳动合同中的竞业限制条款有哪些风险？","taskType":"LEGAL_QA"}'
   ```

4. **查询任务**
   ```bash
   curl http://localhost:8080/api/tasks/{taskId}
   ```

---

## 📚 学习资源

### Spring AI
- Spring AI官方文档
- 阿里云百炼API文档

### 法律知识
- 中华人民共和国劳动合同法
- 合同法基础知识

### 技术栈
- Spring Boot 3.2
- Spring AI 1.1
- Apache Tika
- iText PDF

---

## ⚠️ 注意事项

1. **API Key配置**：确保在application.yml中配置了阿里云API Key
2. **JDK版本**：需要JDK 17+
3. **端口占用**：确保8080端口未被占用
4. **知识库加载**：首次启动需要加载知识库，可能需要几秒钟

---

## 🔧 故障排除

### 问题1：知识库未加载
**现象**：检索结果为空
**解决**：检查knowledgebase目录下是否有文件

### 问题2：API调用失败
**现象**：401 Unauthorized
**解决**：检查API Key配置是否正确

### 问题3：应用启动失败
**现象**：端口被占用
**解决**：修改application.yml中的端口或停止占用端口的应用

---

**创建时间：** 2024年1月
**最后更新：** 完成基础Agent和服务层实现
**下一步：** 实现剩余的Agent和Controller层
