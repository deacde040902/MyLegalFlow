<template>
  <div class="h-screen flex flex-col">
    <header class="bg-white shadow-sm border-b border-gray-300 px-6 py-4">
      <div class="flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <div class="w-10 h-10 bg-black rounded-lg flex items-center justify-center overflow-hidden">
            <img src="/src/resources/picture/L_legal.png" alt="LegalFlow" class="w-full h-full object-contain" />
          </div>
          <div>
            <h1 class="text-xl font-bold text-gray-900">LegalFlow</h1>
            <p class="text-sm text-gray-500">法律智能助手</p>
          </div>
        </div>
        <div class="flex items-center space-x-4">
          <span class="text-sm text-gray-500">欢迎使用</span>
        </div>
      </div>
    </header>

    <div class="flex flex-1 overflow-hidden">
      <aside class="w-64 bg-white border-r border-gray-300 py-6">
        <nav class="space-y-2 px-4">
          <router-link
            v-for="item in menuItems"
            :key="item.name"
            :to="item.path"
            class="flex items-center space-x-3 px-4 py-3 rounded-lg transition-all duration-200"
            :class="currentPath === item.path ? 'bg-gray-100 text-black' : 'text-gray-600 hover:bg-gray-50 hover:text-black'"
          >
            <img 
              :src="getIconPath(item.icon)" 
              :alt="item.label" 
              class="w-5 h-5"
            />
            <span>{{ item.label }}</span>
          </router-link>
        </nav>

        <div class="mt-8 px-4">
          <div class="bg-black rounded-xl p-4 text-white">
            <p class="text-sm opacity-80">法律知识库</p>
            <p class="text-2xl font-bold mt-1">{{ stats.totalDocuments || 0 }}</p>
            <p class="text-xs opacity-80 mt-1">条记录</p>
          </div>
        </div>
      </aside>

      <main class="flex-1 overflow-auto p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { knowledgeApi } from './api'

const route = useRoute()
const stats = ref({ totalDocuments: 0 })

const currentPath = computed(() => route.path)

const menuItems = [
  { name: 'Home', path: '/', label: '首页', icon: 'home' },
  { name: 'QA', path: '/qa', label: '法律问答', icon: 'message' },
  { name: 'Knowledge', path: '/knowledge', label: '知识库', icon: 'knowledge' },
  { name: 'Tasks', path: '/tasks', label: '任务管理', icon: 'mission' }
]

const getIconPath = (iconName) => {
  const iconMap = {
    'home': '/src/resources/picture/L-legalFlow.png',
    'message': '/src/resources/picture/L_message.png',
    'knowledge': '/src/resources/picture/L_knowleage.png',
    'mission': '/src/resources/picture/L_mission.png'
  }
  return iconMap[iconName] || '/src/resources/picture/L-legalFlow.png'
}

onMounted(() => {
  knowledgeApi.getStats().then(res => {
    stats.value = res.data
  }).catch(() => {
    stats.value = { totalDocuments: 0 }
  })
})
</script>
