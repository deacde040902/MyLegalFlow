import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

api.interceptors.response.use(
  response => response,
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export const qaApi = {
  askQuestion(data) {
    return api.post('/qa', data)
  },
  askQuestionWithFiles(formData) {
    return api.post('/qa/with-files', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  createConversation() {
    return api.post('/qa/conversation/create')
  }
}

export const knowledgeApi = {
  getStats() {
    return api.get('/knowledge/stats')
  },
  search(query, category, topK = 10) {
    return api.get('/knowledge/search', {
      params: { query, category, topK }
    })
  },
  getAllDocuments() {
    return api.get('/knowledge/documents')
  },
  getDocument(id) {
    return api.get(`/knowledge/documents/${id}`)
  },
  getCategories() {
    return api.get('/knowledge/categories')
  }
}

export const taskApi = {
  createTask(data) {
    return api.post('/tasks', data)
  },
  getAllTasks() {
    return api.get('/tasks')
  },
  getTask(taskId) {
    return api.get(`/tasks/${taskId}`)
  },
  deleteTask(taskId) {
    return api.delete(`/tasks/${taskId}`)
  }
}

export const healthApi = {
  check() {
    return api.get('/actuator/health')
  }
}

export default api
