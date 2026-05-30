<template>
  <div class="max-w-4xl mx-auto">
    <div class="bg-black rounded-2xl p-8 text-white mb-8">
      <h2 class="text-3xl font-bold mb-2">欢迎使用 LegalFlow</h2>
      <p class="text-gray-300">基于多智能体协同的法律工作流系统</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <div class="bg-white rounded-xl p-6 border border-gray-300">
        <img src="/src/resources/picture/L-legalFlow.png" alt="法律问答" class="w-12 h-12 mb-4" />
        <h3 class="text-lg font-semibold text-gray-900 mb-2">法律问答</h3>
        <p class="text-gray-600 text-sm">智能回答法律问题，提供专业法律意见</p>
        <router-link to="/qa" class="mt-4 inline-flex items-center text-black hover:text-gray-600 text-sm font-medium">
          开始问答 →
        </router-link>
      </div>

      <div class="bg-white rounded-xl p-6 border border-gray-300">
        <img src="/src/resources/picture/L_knowleage.png" alt="法律知识库" class="w-12 h-12 mb-4" />
        <h3 class="text-lg font-semibold text-gray-900 mb-2">法律知识库</h3>
        <p class="text-gray-600 text-sm">收录各类法律法规、合同条款和案例</p>
        <router-link to="/knowledge" class="mt-4 inline-flex items-center text-black hover:text-gray-600 text-sm font-medium">
          浏览知识库 →
        </router-link>
      </div>

      <div class="bg-white rounded-xl p-6 border border-gray-300">
        <img src="/src/resources/picture/L_mission.png" alt="任务管理" class="w-12 h-12 mb-4" />
        <h3 class="text-lg font-semibold text-gray-900 mb-2">任务管理</h3>
        <p class="text-gray-600 text-sm">管理合同审查和法律分析任务</p>
        <router-link to="/tasks" class="mt-4 inline-flex items-center text-black hover:text-gray-600 text-sm font-medium">
          查看任务 →
        </router-link>
      </div>
    </div>

    <div class="bg-white rounded-xl p-6 border border-gray-300">
      <h3 class="text-lg font-semibold text-gray-900 mb-4">快捷问答</h3>
      <div class="space-y-3">
        <div 
          v-for="question in quickQuestions" 
          :key="question"
          class="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 cursor-pointer transition-colors border border-gray-200"
          @click="goToQA(question)"
        >
          <span class="text-gray-800">{{ question }}</span>
          <span class="text-gray-600">→</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const quickQuestions = [
  '竞业限制条款有哪些风险？',
  '试用期最长期限是多久？',
  '违约金条款的法律规定是什么？',
  '单方面解除劳动合同需要赔偿吗？'
]

const goToQA = (question) => {
  router.push({ path: '/qa', query: { question } })
}
</script>
