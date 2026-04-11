<template>
  <div class="plaza-layout">
    <div class="main-feed">
      <div class="feed-header">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="广场首页" name="all"></el-tab-pane>
          <el-tab-pane label="寻物启事" name="lost"></el-tab-pane>
          <el-tab-pane label="失物招领" name="found"></el-tab-pane>
        </el-tabs>

        <div class="filter-controls">
          <el-select v-model="queryParams.status" placeholder="帖子状态" clearable class="filter-select" @change="handleFilterChange">
            <el-option label="寻找中" :value="0" />
            <el-option label="锁定中" :value="1" />
            <el-option label="已结案" :value="2" />
          </el-select>

          <el-cascader
            v-model="searchLocationPath"
            :options="locationOptions"
            :props="{ checkStrictly: true }"
            placeholder="区域"
            clearable
            class="ml-10 filter-cascader"
            @change="handleFilterChange"
          />
          <el-input
            v-model="searchLocationDetail"
            placeholder="自填地点..."
            class="filter-input ml-5"
            clearable
            @keyup.enter="handleFilterChange"
            @clear="handleFilterChange"
          />

          <el-input v-model="queryParams.keyword" placeholder="搜索物品名称..." class="filter-input ml-10" prefix-icon="Search" clearable @keyup.enter="handleFilterChange" />
          <el-button type="primary" class="ml-10" @click="handleFilterChange">搜索</el-button>
        </div>
      </div>

      <div class="feed-list" v-loading="loading">
        <el-empty v-if="itemList.length === 0" description="没有找到匹配的内容" />
        <div v-for="item in itemList" :key="item.id" class="post-item" @click="goToDetail(item.id)">
          <span class="meta-time">{{ formatTime(item.createTime) }}</span>
          <div class="post-content">
            <div class="post-meta">
              <el-tag :type="item.type === 0 ? 'danger' : 'success'" size="small" effect="dark">{{ item.type === 0 ? '丢失' : '拾取' }}</el-tag>
              <el-tag :type="getStatusType(item.status)" size="small" class="ml-5">{{ getStatusText(item.status) }}</el-tag>
              <el-tag v-if="item.isTop === 1" type="warning" size="small" effect="plain" class="ml-5"><el-icon><Top /></el-icon> 置顶</el-tag>
              <el-tag v-if="item.role === 1" type="warning" size="small" effect="dark" class="ml-5">官方</el-tag>
              <span class="meta-name" @click.stop="goToProfile(item.userId)">{{ item.publisherNickname }}</span>
            </div>
            <h3 class="post-title">{{ item.publicDesc }}</h3>
            <div class="post-info">
              <span><el-icon><PriceTag /></el-icon> {{ item.itemName }}</span>
              <span class="ml-10"><el-icon><Location /></el-icon> {{ item.location }}</span>
            </div>
          </div>
          <div class="post-thumb" v-if="item.coverImage && item.coverImage.trim() !== ''">
            <el-image :src="getImageUrl(item.coverImage)" fit="cover" class="thumb-img" />
          </div>
        </div>
      </div>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="queryParams.page" :total="total" background layout="prev, pager, next" @current-change="fetchData" />
      </div>
    </div>

    <div class="side-bar">
      <div class="side-card action-card">
        <h3>校园失物互助</h3>
        <p>让每一份心爱之物都能回家</p>
        <el-button type="primary" size="large" class="publish-btn" @click="router.push('/publish')">我要发布</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { PriceTag, Location, Search, Top } from '@element-plus/icons-vue'
import { getItemPage } from '@/api/item'

const router = useRouter()
const itemList = ref([])
const total = ref(0)
const loading = ref(false)
const activeTab = ref('all')

const queryParams = reactive({ 
  page: 1, pageSize: 10, type: null, keyword: '', location: '', status: null 
})

const locationOptions = [
  {
    value: '教学楼', label: '教学楼',
    children: [
      { value: '一号楼', label: '一号楼' }, { value: '二号楼', label: '二号楼' },
      { value: '三号楼', label: '三号楼' }, { value: '四号楼', label: '四号楼' },
      { value: '五号楼', label: '五号楼' }
    ]
  },
  {
    value: '饭堂', label: '饭堂',
    children: [
      { value: '第一饭堂', label: '第一饭堂' }, { value: '第二饭堂', label: '第二饭堂' },
      { value: '第三饭堂', label: '第三饭堂' }, { value: '第四饭堂', label: '第四饭堂' }
    ]
  },
  { value: '图书馆', label: '图书馆' },
  { value: '其他', label: '其他（自填）' }
]

const searchLocationPath = ref([])
const searchLocationDetail = ref('')

const getStatusText = (s) => ({ 0: '寻找中', 1: '锁定中', 2: '已结案' }[s] || '未知')
const getStatusType = (s) => s === 1 ? 'warning' : (s === 2 ? 'info' : 'primary')

const getImageUrl = (url) => {
  if (!url) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  if (url.startsWith('http')) return url
  return `http://localhost:8080${url}` // 🌟 保持与 request.js 端口一致
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getItemPage(queryParams)
    itemList.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

// 🌟 核心修复：任何搜索动作触发时，先强制组装地址
const handleFilterChange = () => {
  let base = (searchLocationPath.value && searchLocationPath.value.length) ? searchLocationPath.value.join('-') : ''
  if (base === '其他') {
    queryParams.location = searchLocationDetail.value || ''
  } else if (base) {
    queryParams.location = searchLocationDetail.value ? `${base}-${searchLocationDetail.value}` : base
  } else {
    // 🌟 如果完全没选左边的地址框，只填了右边详细地址
    queryParams.location = searchLocationDetail.value || ''
  }

  queryParams.page = 1 // 重置页码
  fetchData()
}

const handleTabChange = (val) => {
  queryParams.type = val === 'all' ? null : (val === 'lost' ? 0 : 1)
  handleFilterChange()
}

const goToDetail = (id) => router.push(`/item/${id}`)
const goToProfile = (id) => router.push(`/profile/${id}`)
const formatTime = (t) => t ? t.split('T')[0] : ''

onMounted(() => fetchData())
</script>

<style scoped>
.plaza-layout { display: flex; gap: 20px; align-items: flex-start; }
.main-feed { flex: 1; min-width: 0; background: #fff; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.04); }
.feed-header { padding: 10px 20px 0; border-bottom: 1px solid #f0f0f0; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; }
.filter-controls { display: flex; align-items: center; padding-bottom: 10px; }
.filter-select { width: 100px; }
.filter-cascader { width: 120px; }
.filter-input { width: 150px; }
.ml-10 { margin-left: 10px; }
.ml-5 { margin-left: 5px; }

.post-item { display: flex; justify-content: space-between; padding: 20px; border-bottom: 1px solid #f0f0f0; cursor: pointer; transition: all 0.2s; position: relative; }
.post-item:hover { background: #fafafa; }
.meta-time { position: absolute; top: 20px; right: 20px; font-size: 13px; color: #86909c; }
.post-content { flex: 1; min-width: 0; padding-right: 120px; }
.post-meta { display: flex; align-items: center; margin-bottom: 10px; font-size: 13px; }
.meta-name { margin-left: 10px; color: #4e5969; font-weight: 500; }
.post-title { margin: 0 0 10px 0; font-size: 17px; font-weight: 600; color: #1d2129; }
.post-info { font-size: 13px; color: #86909c; display: flex; align-items: center; }
.post-thumb { margin-left: 20px; width: 140px; height: 90px; border-radius: 6px; overflow: hidden; flex-shrink: 0; margin-top: 25px; }
.thumb-img { width: 100%; height: 100%; }

.side-bar { width: 300px; flex-shrink: 0; }
.side-card { background: #fff; border-radius: 8px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.04); }
.publish-btn { width: 100%; height: 45px; }
.pagination-wrapper { padding: 20px; display: flex; justify-content: center; }
</style>