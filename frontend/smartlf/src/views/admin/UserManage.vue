<template>
  <div class="admin-container">
    <div class="page-header">
      <h2>用户管理</h2>
      <div style="display: flex; gap: 10px;">
        <el-select v-model="queryParams.status" placeholder="账号状态" clearable @change="fetchData" style="width: 120px;">
          <el-option label="正常" :value="0" />
          <el-option label="已封禁" :value="1" />
        </el-select>
        <el-input v-model="queryParams.keyword" placeholder="搜索用户名/昵称/手机号..." style="width: 250px;" clearable @keyup.enter="fetchData" />
        <el-button type="primary" @click="fetchData">搜索</el-button>
      </div>
    </div>

    <el-table :data="userList" v-loading="loading" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      
      <el-table-column label="头像" width="80" align="center">
        <template #default="{ row }">
          <el-avatar :size="40" :src="getImageUrl(row.avatarUrl)" @click="goToProfile(row.id)" class="pointer" />
        </template>
      </el-table-column>
      
      <el-table-column prop="username" label="用户名" width="120" />
      
      <el-table-column label="昵称" min-width="120">
        <template #default="{ row }">
          <span class="nickname-link" @click="goToProfile(row.id)">{{ row.nickname }}</span>
        </template>
      </el-table-column>
      
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column label="角色" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.role === 1 ? 'warning' : 'info'">{{ row.role === 1 ? '管理员' : '学生' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'danger' : 'success'">{{ row.status === 1 ? '已封禁' : '正常' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleViewDetail(row.id)">查看详情</el-button>
          
          <el-button v-if="row.role !== 1" :type="row.status === 1 ? 'success' : 'danger'" link @click="handleStatusChange(row)">
            {{ row.status === 1 ? '解封' : '封禁' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination v-model:current-page="queryParams.page" :total="total" background layout="prev, pager, next" @current-change="fetchData" />
    </div>

    <el-dialog v-model="detailVisible" title="用户详细档案 (管理员视角)" width="520px" destroy-on-close>
      <div v-if="userDetail" v-loading="detailLoading">
        <div class="user-detail-header">
          <el-avatar :size="80" :src="getImageUrl(userDetail.avatarUrl)" @click="handleGoToProfileFromDialog(userDetail.id)" class="pointer" />
          
          <div class="header-info">
            <h3 @click="handleGoToProfileFromDialog(userDetail.id)" class="nickname-link" style="display: inline-block;">
              {{ userDetail.nickname }}
            </h3>
            <p>ID: {{ userDetail.id }} | @{{ userDetail.username }}</p>
          </div>

          <div style="margin-left: auto;">
            <el-button type="primary" plain size="small" @click="handleGoToProfileFromDialog(userDetail.id)">访问公开主页</el-button>
          </div>
        </div>
        
        <el-divider />
        
        <el-descriptions :column="1" border>
          <el-descriptions-item label="手机号">{{ userDetail.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ userDetail.email }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatTime(userDetail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="最近活跃">{{ formatTime(userDetail.lastActiveTime) || '暂无记录' }}</el-descriptions-item>
          <el-descriptions-item label="违规次数">
            <el-tag type="danger" effect="plain">{{ userDetail.violationCount || 0 }} 次违规下架</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="账号状态">
            <el-tag :type="userDetail.status === 1 ? 'danger' : 'success'">
              {{ userDetail.status === 1 ? '封禁中' : '状态正常' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserPage, updateUserStatus, getUserDetailByAdmin } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const userList = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10, keyword: '', status: null })

const detailVisible = ref(false)
const detailLoading = ref(false)
const userDetail = ref(null)

const getImageUrl = (url) => {
  if (!url) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  return url.startsWith('http') ? url : `http://localhost:8080${url}`
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUserPage(queryParams)
    userList.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

// 跳转主页函数
const goToProfile = (id) => {
  router.push(`/profile/${id}`)
}

// 🌟 从弹窗跳转时，先关掉弹窗再路由跳转，体验更好
const handleGoToProfileFromDialog = (id) => {
  detailVisible.value = false
  goToProfile(id)
}

const handleViewDetail = async (userId) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getUserDetailByAdmin(userId)
    userDetail.value = res.data
  } catch (e) {
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

const handleStatusChange = (row) => {
  const isBanning = row.status === 0
  const actionText = isBanning ? '封禁' : '解封'
  const targetStatus = isBanning ? 1 : 0

  ElMessageBox.confirm(`确定要${actionText}用户【${row.nickname}】吗？`, '高危操作', { type: 'warning' }).then(async () => {
    await updateUserStatus(row.id, targetStatus)
    ElMessage.success(`已${actionText}`)
    fetchData()
  }).catch(() => {})
}

const formatTime = (t) => t ? t.replace('T', ' ').substring(0, 16) : ''
onMounted(() => fetchData())
</script>

<style scoped>
.admin-container { padding: 20px; background: #fff; border-radius: 8px; min-height: calc(100vh - 120px); }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; color: #303133; }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: center; }

/* 交互样式 */
.pointer { cursor: pointer; transition: opacity 0.2s; }
.pointer:hover { opacity: 0.8; }
.nickname-link { color: #1e80ff; cursor: pointer; font-weight: 500; transition: color 0.2s; }
.nickname-link:hover { text-decoration: underline; color: #0056b3; }

/* 详情弹窗样式 */
.user-detail-header { display: flex; align-items: center; gap: 20px; margin-bottom: 15px; }
.header-info h3 { margin: 0 0 5px 0; font-size: 20px; }
.header-info p { margin: 0; color: #909399; font-size: 14px; }
</style>