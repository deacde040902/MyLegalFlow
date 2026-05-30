# 📮 LegalFlow Postman 测试文档

## 🌐 基础配置

### 环境变量
在 Postman 中创建环境变量：

| 变量名 | 值 | 说明 |
|--------|-----|------|
| `base_url` | `http://localhost:8080` | 应用基础地址 |
| `task_id` | (空，运行时填充) | 任务ID |

### 导入环境
```json
{
  "name": "LegalFlow",
  "values": [
    {
      "key": "base_url",
      "value": "http://localhost:8080",
      "enabled": true
    },
    {
      "key": "task_id",
      "value": "",
      "enabled": true
    }
  ]
}
```

---

## 📡 API 接口测试

### 1. 健康检查

**请求：**
- **方法**: GET
- **URL**: `{{base_url}}/actuator/health`

**预期响应（200 OK）：**
```json
{
  "status": "UP"
}
```

---

### 2. 创建法律问答任务

**请求：**
- **方法**: POST
- **URL**: `{{base_url}}/api/tasks`
- **Headers**: `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "message": "劳动合同中的竞业限制条款有哪些风险？",
  "taskType": "LEGAL_QA"
}
```

**预期响应（200 OK）：**
```json
{
  "taskId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "CREATED",
  "message": "任务计划已接收，等待执行",
  "data": null
}
```

**测试用例：**
| 测试场景 | 请求消息 | taskType |
|----------|----------|----------|
| 竞业限制风险 | 劳动合同中的竞业限制条款有哪些风险？ | LEGAL_QA |
| 违约金规定 | 违约金条款的法律规定是什么？ | LEGAL_QA |
| 试用期规定 | 试用期最长期限是多久？ | LAW_RETRIEVAL |
| 合同审查 | 分析这份劳动合同的违约金条款风险 | CONTRACT_REVIEW |

---

### 3. 查询任务结果

**请求：**
- **方法**: GET
- **URL**: `{{base_url}}/api/tasks/{{task_id}}`

**预期响应（200 OK）：**
```json
{
  "taskId": "{{task_id}}",
  "status": "COMPLETED",
  "message": "任务执行完成",
  "data": {...}
}
```

---

### 4. 文件上传（合同审查）

**请求：**
- **方法**: POST
- **URL**: `{{base_url}}/api/tasks/upload`
- **Headers**: `Content-Type: multipart/form-data`
- **Body** (form-data):
  - `file`: 选择本地合同文件（PDF/Word）
  - `taskType`: `CONTRACT_REVIEW`

**预期响应（200 OK）：**
```json
{
  "status": "UPLOADED",
  "message": "文件已上传，等待处理"
}
```

---

### 5. 知识库统计

**请求：**
- **方法**: GET
- **URL**: `{{base_url}}/api/knowledge/stats`

**预期响应（200 OK）：**
```json
{
  "totalDocuments": 8,
  "totalRecords": 485,
  "categories": {
    "风险条款": 17,
    "法律问答": 456,
    "行业案例": 12
  }
}
```

---

### 6. 知识库检索

**请求：**
- **方法**: GET
- **URL**: `{{base_url}}/api/knowledge/search`
- **Params**:
  - `query`: 检索关键词
  - `topK`: 返回数量（默认10）
  - `category`: 分类过滤（可选）

**示例请求：**
```
{{base_url}}/api/knowledge/search?query=竞业限制&topK=5
```

**预期响应（200 OK）：**
```json
[
  {
    "documentId": "1",
    "title": "合同常见风险条款清单",
    "content": "1. 竞业限制条款：...",
    "category": "风险条款",
    "tags": ["竞业限制", "劳动合同"],
    "score": 0.95,
    "source": "合同常见风险条款清单.txt"
  }
]
```

**检索测试关键词：**
| 关键词 | 预期结果 |
|--------|----------|
| 竞业限制 | 返回竞业限制相关知识 |
| 违约金 | 返回违约金相关条款 |
| 试用期 | 返回试用期规定 |
| 解除合同 | 返回合同解除相关 |

---

### 7. 获取所有文档

**请求：**
- **方法**: GET
- **URL**: `{{base_url}}/api/knowledge/documents`

**预期响应（200 OK）：**
```json
[
  {
    "id": "1",
    "title": "合同常见风险条款清单",
    "category": "风险条款",
    "source": "合同常见风险条款清单.txt",
    "createdAt": "2024-01-01T10:00:00"
  }
]
```

---

### 8. 获取单个文档

**请求：**
- **方法**: GET
- **URL**: `{{base_url}}/api/knowledge/documents/{documentId}`

**预期响应（200 OK）：**
```json
{
  "id": "1",
  "title": "合同常见风险条款清单",
  "content": "完整文档内容...",
  "category": "风险条款",
  "tags": ["风险", "合同"],
  "source": "合同常见风险条款清单.txt"
}
```

---

### 9. 获取分类列表

**请求：**
- **方法**: GET
- **URL**: `{{base_url}}/api/knowledge/categories`

**预期响应（200 OK）：**
```json
[
  {
    "name": "风险条款",
    "count": 17
  },
  {
    "name": "法律问答",
    "count": 456
  },
  {
    "name": "行业案例",
    "count": 12
  }
]
```

---

### 10. 高级搜索

**请求：**
- **方法**: POST
- **URL**: `{{base_url}}/api/knowledge/search`
- **Headers**: `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "query": "违约金",
  "category": "风险条款",
  "topK": 10,
  "threshold": 0.5
}
```

**预期响应（200 OK）：**
```json
[
  {
    "documentId": "1",
    "title": "...",
    "content": "...",
    "score": 0.9
  }
]
```

---

### 11. 创建反馈

**请求：**
- **方法**: POST
- **URL**: `{{base_url}}/api/feedback/{{task_id}}`
- **Headers**: `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "rating": 5,
  "comment": "回答非常专业，帮助很大！"
}
```

**预期响应（200 OK）：**
```json
{
  "id": 1,
  "taskId": "{{task_id}}",
  "rating": 5,
  "comment": "回答非常专业，帮助很大！",
  "createdAt": "2024-01-01T10:00:00"
}
```

---

### 12. 获取反馈

**请求：**
- **方法**: GET
- **URL**: `{{base_url}}/api/feedback/{{task_id}}`

**预期响应（200 OK）：**
```json
[
  {
    "id": 1,
    "taskId": "{{task_id}}",
    "rating": 5,
    "comment": "回答非常专业，帮助很大！",
    "createdAt": "2024-01-01T10:00:00"
  }
]
```

---

## 🧪 Postman 测试集合

### 导入集合

```json
{
  "info": {
    "_postman_id": "legalflow-api-collection",
    "name": "LegalFlow API",
    "description": "法律智能助手API测试集合",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "健康检查",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/actuator/health"
      }
    },
    {
      "name": "创建任务-法律问答",
      "request": {
        "method": "POST",
        "url": "{{base_url}}/api/tasks",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\"message\":\"竞业限制条款有哪些风险？\",\"taskType\":\"LEGAL_QA\"}"
        }
      }
    },
    {
      "name": "查询任务结果",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/tasks/{{task_id}}"
      }
    },
    {
      "name": "知识库统计",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/knowledge/stats"
      }
    },
    {
      "name": "知识库检索",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/knowledge/search?query=竞业限制&topK=5"
      }
    },
    {
      "name": "获取文档列表",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/knowledge/documents"
      }
    },
    {
      "name": "获取分类列表",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/knowledge/categories"
      }
    },
    {
      "name": "创建反馈",
      "request": {
        "method": "POST",
        "url": "{{base_url}}/api/feedback/{{task_id}}",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\"rating\":5,\"comment\":\"测试反馈\"}"
        }
      }
    }
  ]
}
```

---

## 📋 测试流程

### 标准测试流程

1. **启动应用**
   ```bash
   cd AI_LegalFlow-backend
   mvn spring-boot:run
   ```

2. **配置Postman环境**
   - 创建环境变量 `base_url = http://localhost:8080`

3. **运行测试用例**
   - 先测试「健康检查」确认服务启动
   - 测试「知识库统计」确认知识库加载
   - 创建任务并获取结果
   - 测试知识库检索功能

### 预期状态码

| 接口 | 成功 | 失败 |
|------|------|------|
| GET /api/knowledge/* | 200 | 500 |
| POST /api/tasks | 200 | 400/500 |
| GET /api/tasks/{id} | 200 | 404 |
| POST /api/feedback | 200 | 400 |

---

## 🐛 常见问题

### 问题1：连接失败
**现象**: `Could not get any response`
**原因**: 应用未启动或端口被占用
**解决**: 
- 检查应用是否启动
- 检查端口8080是否被占用

### 问题2：知识库未加载
**现象**: 统计接口返回空数据
**原因**: 知识库文件路径错误
**解决**: 检查 `resources/knowledgebase/` 目录是否有文件

### 问题3：API Key无效
**现象**: `401 Unauthorized`
**原因**: 阿里云API Key配置错误或过期
**解决**: 检查 `application.yml` 中的API Key

### 问题4：H2控制台访问
**地址**: `http://localhost:8080/h2-console`
**JDBC URL**: `jdbc:h2:mem:legalflow`
**用户名**: `sa`
**密码**: (空)

---

## 📝 测试笔记

在 Postman 中记录测试结果：

| 测试时间 | 接口 | 状态 | 备注 |
|----------|------|------|------|
| 2024-01-01 | POST /api/tasks | ✅ | 创建成功 |
| 2024-01-01 | GET /api/knowledge/stats | ✅ | 知识库加载正常 |
| 2024-01-01 | GET /api/knowledge/search | ✅ | 检索功能正常 |

---

**提示**: 在创建任务后，可以使用「Tests」功能自动提取task_id到环境变量：
```javascript
var jsonData = pm.response.json();
if (jsonData.taskId) {
    pm.environment.set("task_id", jsonData.taskId);
}
```
