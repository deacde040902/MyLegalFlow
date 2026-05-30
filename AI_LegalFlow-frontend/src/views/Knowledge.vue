<template>
  <div class="max-w-5xl mx-auto">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h2 class="text-2xl font-bold text-gray-900">法律知识库</h2>
        <p class="text-gray-500 text-sm mt-1">浏览和检索法律知识文档</p>
      </div>
      <div class="flex items-center space-x-3">
        <el-select 
          v-model="selectedCategory" 
          placeholder="选择分类"
          class="w-40"
        >
          <el-option label="全部" value="" />
          <el-option 
            v-for="cat in categories" 
            :key="cat" 
            :label="cat" 
            :value="cat" 
          />
        </el-select>
      </div>
    </div>

    <div class="bg-white rounded-xl border border-gray-300 p-4 mb-6">
      <div class="flex space-x-3">
        <el-input
          v-model="searchQuery"
          placeholder="搜索知识库..."
          class="flex-1"
          @keyup.enter="search"
        />
        <el-button type="primary" @click="search" class="bg-black hover:bg-gray-800 border-none text-white">搜索</el-button>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div 
        v-for="doc in documents" 
        :key="doc.id"
        class="bg-white rounded-xl p-4 border border-gray-300 hover:shadow-md transition-shadow"
      >
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <span class="inline-block px-2 py-1 bg-gray-100 text-gray-700 rounded text-xs mb-2">
              {{ doc.category }}
            </span>
            <h3 class="font-semibold text-gray-900 mb-2">{{ doc.title }}</h3>
            <p class="text-gray-700 text-sm line-clamp-3">{{ doc.content }}</p>
          </div>
        </div>
        <div class="mt-3 flex items-center justify-between">
          <span class="text-xs text-gray-500">{{ doc.source }}</span>
          <div class="flex flex-wrap gap-1">
            <span 
              v-for="tag in doc.tags?.slice(0, 3)" 
              :key="tag"
              class="px-2 py-0.5 bg-gray-100 text-gray-600 rounded text-xs"
            >
              {{ tag }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="documents.length === 0" class="text-center py-12">
      <div class="text-6xl mb-4">📭</div>
      <p class="text-gray-500">暂无数据</p>
    </div>

    <el-pagination
      v-if="total > 10"
      :total="total"
      :page-size="10"
      layout="prev, pager, next"
      @current-change="handlePageChange"
      class="mt-6 flex justify-center"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { knowledgeApi } from '../api'

const searchQuery = ref('')
const selectedCategory = ref('')
const documents = ref([])
const categories = ref([])
const total = ref(0)
const currentPage = ref(1)

const search = () => {
  loadDocuments()
}

const loadDocuments = async () => {
  try {
    if (searchQuery.value) {
      const res = await knowledgeApi.search(searchQuery.value, selectedCategory.value, 10)
      documents.value = res.data
      total.value = res.data.length
    } else {
      const res = await knowledgeApi.getAllDocuments()
      documents.value = selectedCategory.value 
        ? res.data.filter(doc => doc.category === selectedCategory.value)
        : res.data
      total.value = documents.value.length
    }
  } catch (error) {
    console.error('Failed to load documents:', error)
  }
}

const loadCategories = async () => {
  try {
    const res = await knowledgeApi.getCategories()
    categories.value = res.data
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadDocuments()
}

onMounted(() => {
  loadDocuments()
  loadCategories()
})
</script>
