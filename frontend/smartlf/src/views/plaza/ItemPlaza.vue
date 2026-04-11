<template>
  <div class="plaza-layout">
    <div class="main-feed">
      <div class="feed-header">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="广场首页" name="all"></el-tab-pane>
          <el-tab-pane label="寻物启事" name="lost"></el-tab-pane>
          <el-tab-pane label="失物招领" name="found"></el-tab-pane>
        </el-tabs>
        <div class="search-box">
          <el-input v-model="queryParams.keyword" placeholder="搜索物品..." prefix-icon="Search" clearable @keyup.enter="fetchData" />
        </div>
      </div>

      <div class="feed-list" v-loading="loading">
        <el-empty v-if="itemList.length === 0" />
        <div v-for="item in itemList" :key="item.id" class="post-item" @click="goToDetail(item.id)">
          <div class="post-content">
            <div class="post-meta">
              <el-tag :type="item.type === 0 ? 'danger' : 'success'" size="small" effect="dark">
                {{ item.type === 0 ? '丢失' : '拾取' }}
              </el-tag>
              <el-tag v-if="item.role === 1" type="warning" size="small" effect="dark" class="ml-5">官方</el-tag>
              <span class="meta-name" @click.stop="goToProfile(item.userId)">{{ item.publisherNickname }}</span>
              <span class="meta-time">{{ formatTime(item.createTime) }}</span>
            </div>
            <h3 class="post-title">{{ item.publicDesc }}</h3>
            <div class="post-info">
              <span><el-icon><PriceTag /></el-icon> {{ item.itemName }}</span>
              <span><el-icon><Location /></el-icon> {{ item.location }}</span>
            </div>
          </div>
          <div class="post-thumb" v-if="item.coverImage">
            <el-image :src="item.coverImage" fit="cover" class="thumb-img" />
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { PriceTag, Location, Search } from '@element-plus/icons-vue'
import { getItemPage } from '@/api/item'

const router = useRouter()
const itemList = ref([])
const total = ref(0)
const loading = ref(false)
const activeTab = ref('all')
const queryParams = ref({ page: 1, pageSize: 10, type: null, keyword: '' })

const fetchData = async () => {
  loading.value = true
  const res = await getItemPage(queryParams.value)
  itemList.value = res.data.records || []
  total.value = res.data.total || 0
  loading.value = false
}

const handleTabChange = (val) => {
  queryParams.value.type = val === 'all' ? null : (val === 'lost' ? 0 : 1)
  fetchData()
}

const goToDetail = (id) => router.push(`/item/${id}`)
const goToProfile = (id) => router.push(`/profile/${id}`)
const formatTime = (t) => t ? t.split('T')[0] : ''

onMounted(() => fetchData())
</script>

<style scoped>
.plaza-layout { display: flex; gap: 20px; align-items: flex-start; }
.main-feed { flex: 1; background: #fff; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.04); }
.feed-header { padding: 10px 20px 0; border-bottom: 1px solid #f0f0f0; display: flex; justify-content: space-between; align-items: center; }
.search-box { width: 200px; }
.post-item { display: flex; justify-content: space-between; padding: 20px; border-bottom: 1px solid #f0f0f0; cursor: pointer; transition: background 0.2s; }
.post-item:hover { background: #fafafa; }
.post-content { flex: 1; }
.post-meta { display: flex; align-items: center; margin-bottom: 10px; font-size: 13px; }
.ml-5 { margin-left: 5px; }
.meta-name { margin-left: 10px; color: #4e5969; font-weight: 500; cursor: pointer; }
.meta-name:hover { color: #1e80ff; }
.meta-time { margin-left: auto; color: #86909c; }
.post-title { margin: 0 0 10px 0; font-size: 16px; color: #1d2129; }
.post-info { font-size: 13px; color: #86909c; display: flex; gap: 15px; }
.post-thumb { margin-left: 20px; width: 120px; height: 80px; border-radius: 4px; overflow: hidden; }
.thumb-img { width: 100%; height: 100%; }
.side-bar { width: 300px; flex-shrink: 0; }
.side-card { background: #fff; border-radius: 8px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.04); }
.publish-btn { width: 100%; font-weight: bold; }
.pagination-wrapper { padding: 20px; display: flex; justify-content: center; }
</style>