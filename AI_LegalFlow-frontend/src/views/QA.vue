<template>
  <div class="max-w-4xl mx-auto">
    <div class="bg-black rounded-xl p-6 text-white mb-6">
      <div class="flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <img src="/src/resources/picture/L_legal.png" alt="LegalFlow" class="w-8 h-8" />
          <div>
            <h2 class="text-xl font-bold">法律智能问答</h2>
            <p class="text-gray-300 text-sm">输入您的法律问题，获得专业解答</p>
          </div>
        </div>
        <button 
          class="text-sm text-gray-300 hover:text-white"
          @click="clearHistory"
        >
          🗑️ 清空历史
        </button>
      </div>
    </div>

    <div class="bg-white rounded-xl border border-gray-300 p-6">
      <div v-if="messages.length > 0" class="space-y-4 mb-6 max-h-96 overflow-auto">
        <div 
          v-for="(msg, index) in messages" 
          :key="index"
          :class="msg.role === 'user' ? 'bg-gray-50' : 'bg-gray-100'"
          class="rounded-lg p-4 border"
        >
          <div class="flex items-start space-x-3">
            <div :class="msg.role === 'user' ? 'bg-gray-200' : 'bg-black'" class="w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0">
              <span :class="msg.role === 'user' ? '' : 'text-white'">{{ msg.role === 'user' ? '💬' : '🤖' }}</span>
            </div>
            <div class="flex-1">
              <p :class="msg.role === 'user' ? 'text-gray-500' : 'text-gray-600'" class="text-sm mb-1">{{ msg.role === 'user' ? '您的问题' : '智能助手' }}</p>
              <div class="prose prose-sm max-w-none">
                <pre class="whitespace-pre-wrap text-gray-900">{{ msg.content }}</pre>
              </div>
              <p class="text-xs text-gray-400 mt-2">响应时间：{{ msg.responseTime || '-' }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="space-y-4">
        <el-input
          v-model="question"
          type="textarea"
          :rows="5"
          placeholder="请输入您的法律问题，例如：竞业限制条款有哪些风险？"
          class="resize-none"
        />

        <div class="flex items-center justify-between">
          <div class="flex flex-wrap gap-2">
            <span 
              v-for="tag in hotTags" 
              :key="tag"
              class="px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm cursor-pointer hover:bg-gray-200 transition-colors"
              @click="question = tag"
            >
              {{ tag }}
            </span>
          </div>
          <div class="flex items-center gap-4">
            <div 
              class="text-sm text-gray-600 hover:text-black cursor-pointer flex items-center gap-1"
              @click="triggerFileUpload"
            >
              <input
                ref="fileInput"
                type="file"
                multiple
                accept=".pdf,.doc,.docx,.txt,.jpg,.jpeg,.png,.gif"
                class="hidden"
                @change="handleFileChange"
              />
              <img src="/src/resources/picture/L_wenjianjia.png" alt="上传文件" class="w-4 h-4" /> 上传文件
            </div>
            <el-button 
              type="primary" 
              :loading="loading"
              @click="submitQuestion"
              :disabled="!question.trim() && files.length === 0"
              class="bg-black hover:bg-gray-800 border-none text-white"
            >
              提问
            </el-button>
          </div>
        </div>

        <div v-if="files.length > 0" class="flex flex-wrap gap-2 mt-2">
          <span 
            v-for="(file, index) in files" 
            :key="index"
            class="px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm flex items-center gap-1"
          >
            📎 {{ file.name }}
            <span class="cursor-pointer hover:text-black" @click="removeFile(index)">×</span>
          </span>
        </div>
      </div>

      <div class="mt-4 pt-4 border-t border-gray-300">
        <div class="bg-gray-200 rounded-lg p-3">
          <p class="text-xs text-gray-700 leading-relaxed">
            ⚠️ <strong>免责声明：</strong>本平台提供的法律咨询服务仅供参考，不构成正式法律意见或法律建议。法律问题复杂且具有时效性，建议您在需要时咨询执业律师以获取专业的法律帮助。本平台不对因使用本服务而产生的任何后果承担法律责任。
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { qaApi } from '../api'

const route = useRoute()
const question = ref('')
const loading = ref(false)
const files = ref([])
const fileInput = ref(null)
const messages = ref([])
const conversationId = ref(null)

const hotTags = [
  '竞业限制', '违约金', '试用期', '解除合同', '赔偿', '加班', '工资', '保密协议'
]

const initConversation = async () => {
  try {
    const res = await qaApi.createConversation()
    conversationId.value = res.data.conversationId
  } catch (error) {
    console.error('Failed to create conversation:', error)
  }
}

const triggerFileUpload = () => {
  fileInput.value?.click()
}

const handleFileChange = (event) => {
  const selectedFiles = Array.from(event.target.files || [])
  files.value = [...files.value, ...selectedFiles]
  event.target.value = ''
}

const removeFile = (index) => {
  files.value.splice(index, 1)
}

const submitQuestion = async () => {
  if (!question.value.trim() && files.value.length === 0) return
  
  loading.value = true
  
  const userMessage = {
    role: 'user',
    content: question.value,
    responseTime: ''
  }
  messages.value.push(userMessage)
  
  try {
    let res
    if (files.value.length > 0) {
      const formData = new FormData()
      formData.append('question', question.value)
      formData.append('taskType', 'LEGAL_QA')
      if (conversationId.value) {
        formData.append('conversationId', conversationId.value)
      }
      
      files.value.forEach((file) => {
        formData.append('files', file)
      })
      
      res = await qaApi.askQuestionWithFiles(formData)
    } else {
      const requestData = {
        question: question.value,
        taskType: 'LEGAL_QA'
      }
      if (conversationId.value) {
        requestData.conversationId = conversationId.value
      }
      res = await qaApi.askQuestion(requestData)
    }
    
    const assistantMessage = {
      role: 'assistant',
      content: res.data.answer,
      responseTime: res.data.responseTime
    }
    messages.value.push(assistantMessage)
    
  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: '抱歉，处理您的问题时出现错误，请稍后重试。',
      responseTime: '0秒'
    })
  } finally {
    loading.value = false
    question.value = ''
    files.value = []
  }
}

const clearHistory = () => {
  messages.value = []
  conversationId.value = null
  initConversation()
}

onMounted(() => {
  initConversation()
  if (route.query.question) {
    question.value = decodeURIComponent(route.query.question)
    submitQuestion()
  }
})
</script>
