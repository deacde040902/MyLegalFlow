<template>
  <div class="max-w-5xl mx-auto">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h2 class="text-2xl font-bold text-gray-900">任务管理</h2>
        <p class="text-gray-500 text-sm mt-1">查看和管理法律分析任务</p>
      </div>
      <el-button type="primary" @click="showCreateModal = true" class="bg-black hover:bg-gray-800 border-none text-white">创建任务</el-button>
    </div>

    <div class="bg-white rounded-xl border border-gray-300 overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-100">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">任务ID</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">问题</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">类型</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">状态</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">创建时间</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="task in tasks" :key="task.taskId" class="hover:bg-gray-50">
            <td class="px-6 py-4 text-sm text-gray-900 font-mono">{{ task.taskId.slice(0, 8) }}...</td>
            <td class="px-6 py-4 text-sm text-gray-600 max-w-xs truncate" :title="task.message">{{ task.message }}</td>
            <td class="px-6 py-4">
              <span 
                class="px-2 py-1 rounded-full text-xs font-medium"
                :class="getTypeClass(task.taskType)"
              >
                {{ getTypeLabel(task.taskType) }}
              </span>
            </td>
            <td class="px-6 py-4">
              <span 
                class="px-2 py-1 rounded-full text-xs font-medium"
                :class="getStatusClass(task.status)"
              >
                {{ getStatusLabel(task.status) }}
              </span>
            </td>
            <td class="px-6 py-4 text-sm text-gray-500">{{ formatTime(task.completedAt) }}</td>
            <td class="px-6 py-4">
              <div class="flex items-center space-x-2">
                <button 
                  class="text-black hover:text-gray-600 text-sm"
                  @click="viewTask(task)"
                >
                  查看
                </button>
                <button 
                  class="text-gray-600 hover:text-black text-sm"
                  @click="deleteTask(task.taskId)"
                >
                  删除
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="tasks.length === 0" class="text-center py-12">
        <div class="text-6xl mb-4">📋</div>
        <p class="text-gray-500">暂无任务</p>
        <el-button type="primary" text @click="showCreateModal = true" class="mt-3 text-black hover:text-gray-600">
          创建第一个任务
        </el-button>
      </div>
    </div>

    <el-dialog title="创建任务" :visible="showCreateModal" @close="showCreateModal = false">
      <div class="space-y-4">
        <el-input
          v-model="newTask.message"
          type="textarea"
          :rows="3"
          placeholder="请输入任务内容..."
        />
        <el-select v-model="newTask.taskType" placeholder="选择任务类型">
          <el-option label="法律问答" value="LEGAL_QA" />
          <el-option label="合同审查" value="CONTRACT_REVIEW" />
          <el-option label="法规检索" value="LAW_RETRIEVAL" />
        </el-select>
      </div>
      <template #footer>
        <el-button @click="showCreateModal = false">取消</el-button>
        <el-button type="primary" @click="createTask" class="bg-black hover:bg-gray-800 border-none text-white">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog title="任务详情" :visible="showDetailModal" @close="showDetailModal = false">
      <div v-if="selectedTask" class="space-y-4">
        <div class="flex justify-between items-center">
          <span class="text-sm text-gray-500">任务ID</span>
          <span class="font-mono text-sm">{{ selectedTask.taskId }}</span>
        </div>
        <div class="flex justify-between items-center">
          <span class="text-sm text-gray-500">任务类型</span>
          <span>{{ getTypeLabel(selectedTask.taskType) }}</span>
        </div>
        <div class="flex justify-between items-center">
          <span class="text-sm text-gray-500">状态</span>
          <span :class="getStatusClass(selectedTask.status)">{{ getStatusLabel(selectedTask.status) }}</span>
        </div>
        <div>
          <span class="text-sm text-gray-500">问题</span>
          <p class="mt-1 text-gray-900">{{ selectedTask.message }}</p>
        </div>
        <div v-if="selectedTask.data" class="max-h-60 overflow-auto">
          <span class="text-sm text-gray-500">结果</span>
          <pre class="mt-1 text-sm text-gray-700 bg-gray-50 p-3 rounded border border-gray-200">{{ JSON.stringify(selectedTask.data, null, 2) }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { taskApi } from '../api'

const tasks = ref([])
const showCreateModal = ref(false)
const showDetailModal = ref(false)
const selectedTask = ref(null)

const newTask = reactive({
  message: '',
  taskType: 'LEGAL_QA'
})

const loadTasks = async () => {
  try {
    const res = await taskApi.getAllTasks()
    tasks.value = res.data
  } catch (error) {
    console.error('Failed to load tasks:', error)
  }
}

const createTask = async () => {
  if (!newTask.message.trim()) return
  
  try {
    await taskApi.createTask(newTask)
    showCreateModal.value = false
    newTask.message = ''
    newTask.taskType = 'LEGAL_QA'
    loadTasks()
  } catch (error) {
    console.error('Failed to create task:', error)
  }
}

const viewTask = (task) => {
  selectedTask.value = task
  showDetailModal.value = true
}

const deleteTask = async (taskId) => {
  try {
    await taskApi.deleteTask(taskId)
    loadTasks()
  } catch (error) {
    console.error('Failed to delete task:', error)
  }
}

const getTypeLabel = (type) => {
  const labels = {
    'LEGAL_QA': '法律问答',
    'CONTRACT_REVIEW': '合同审查',
    'LAW_RETRIEVAL': '法规检索'
  }
  return labels[type] || type
}

const getTypeClass = (type) => {
  const classes = {
    'LEGAL_QA': 'bg-gray-100 text-gray-700',
    'CONTRACT_REVIEW': 'bg-gray-200 text-gray-800',
    'LAW_RETRIEVAL': 'bg-gray-100 text-gray-700'
  }
  return classes[type] || 'bg-gray-100 text-gray-700'
}

const getStatusLabel = (status) => {
  const labels = {
    'CREATED': '已创建',
    'EXECUTING': '执行中',
    'COMPLETED': '已完成',
    'FAILED': '失败'
  }
  return labels[status] || status
}

const getStatusClass = (status) => {
  const classes = {
    'CREATED': 'bg-gray-100 text-gray-700',
    'EXECUTING': 'bg-gray-200 text-gray-800',
    'COMPLETED': 'bg-gray-100 text-gray-700',
    'FAILED': 'bg-gray-200 text-gray-800'
  }
  return classes[status] || 'bg-gray-100 text-gray-700'
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadTasks()
})
</script>
