<template>
  <div class="admin-container">
    <div class="page-header">
      <h2>数据看板 (Dashboard)</h2>
      <el-date-picker
        v-model="dateRange"
        type="datetimerange"
        range-separator="至"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        value-format="YYYY-MM-DD HH:mm:ss"
        @change="fetchStats"
      />
    </div>

    <el-row :gutter="20" class="stat-cards" v-loading="loading">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card blue-card">
          <div class="stat-title">总发帖量</div>
          <div class="stat-value">{{ stats.totalItems || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card green-card">
          <div class="stat-title">已找回/结案数量</div>
          <div class="stat-value">{{ stats.solvedItems || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card orange-card">
          <div class="stat-title">活跃用户数 (所选时段)</div>
          <div class="stat-value">{{ stats.activeUsers || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="ai-report-card" shadow="never" v-loading="aiLoading">
      <template #header>
        <div class="card-header">
          <span><el-icon><DataAnalysis /></el-icon> AI 智能平台周报</span>
        </div>
      </template>
      <div class="ai-content markdown-body" v-html="parsedAiSummary || '暂无报告'"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getStatsOverview, getAiSummary } from '@/api/admin'
import { DataAnalysis } from '@element-plus/icons-vue'
import { marked } from 'marked'

const loading = ref(false)
const aiLoading = ref(false)
const dateRange = ref([])
const stats = ref({ totalItems: 0, solvedItems: 0, activeUsers: 0 })
const aiSummary = ref('')

const parsedAiSummary = computed(() => {
  if (!aiSummary.value) return ''
  return marked.parse(aiSummary.value)
})

const fetchStats = async () => {
  loading.value = true
  let params = {}
  if (dateRange.value && dateRange.value.length === 2) {
    params.startTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  try {
    const res = await getStatsOverview(params)
    if (res.code === 1 || res.code === 200) stats.value = res.data
  } finally {
    loading.value = false
  }
}

// 这个方法保留，用于页面刚加载时拉取当天凌晨生成好的数据
const fetchAiSummary = async () => {
  aiLoading.value = true
  try {
    const res = await getAiSummary()
    if (res.code === 1 || res.code === 200) aiSummary.value = res.data
  } finally {
    aiLoading.value = false
  }
}

onMounted(() => {
  fetchStats()
  fetchAiSummary()
})
</script>

<style scoped>
.admin-container { padding: 20px; background: #fff; border-radius: 8px; min-height: calc(100vh - 120px); }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
.stat-cards { margin-bottom: 30px; }
.stat-card { text-align: center; border-radius: 10px; color: #fff; border: none; }
.stat-title { font-size: 16px; opacity: 0.9; margin-bottom: 10px; }
.stat-value { font-size: 36px; font-weight: bold; }
.blue-card { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.green-card { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.orange-card { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
.ai-report-card { border-radius: 10px; }

.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: bold; color: #1d2129; font-size: 16px; }

.markdown-body :deep(h1), .markdown-body :deep(h2), .markdown-body :deep(h3) {
  color: #1d2129;
  margin-top: 20px;
  margin-bottom: 10px;
  font-weight: 600;
}
.markdown-body :deep(h3) { font-size: 18px; }
.markdown-body :deep(p) { line-height: 1.6; color: #4e5969; margin-bottom: 10px; }
.markdown-body :deep(ul), .markdown-body :deep(ol) { padding-left: 20px; margin-bottom: 10px; color: #4e5969; line-height: 1.6; }
.markdown-body :deep(li) { margin-bottom: 5px; }
.markdown-body :deep(strong) { color: #1d2129; font-weight: 600; }
</style>