<template>
  <div class="common-layout">
    <el-container>
      <el-header class="nav-header">
        <div class="header-left" @click="router.push('/')">
          <img src="@/assets/logo.svg" alt="logo" class="logo" />
          <span class="title">校园 AI 失物招领</span>
        </div>
        
        <div class="header-right">
          <el-menu mode="horizontal" :ellipsis="false" router :default-active="route.path">
            <el-menu-item index="/">广场</el-menu-item>
            <el-menu-item index="/message">消息中心</el-menu-item>
            <el-menu-item index="/publish">发布帖子</el-menu-item>
            
            <el-sub-menu v-if="userInfo.role === 1" index="admin">
              <template #title>管理后台</template>
              <el-menu-item index="/admin/dashboard">数据看板</el-menu-item>
              <el-menu-item index="/admin/users">用户管理</el-menu-item>
              <el-menu-item index="/admin/items">帖子管理</el-menu-item>
            </el-sub-menu>
          </el-menu>

          <div class="user-profile">
            <el-dropdown trigger="click">
              <span class="user-dropdown-link">
                <span class="nickname">{{ userInfo.nickname || '加载中...' }}</span>
                <el-avatar :size="32" :src="getImageUrl(userInfo.avatarUrl)" />
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push(`/profile/${userInfo.id}`)">
                    <el-icon><User /></el-icon> 个人主页
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon> 退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      
      <el-main class="main-body">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getUserInfo } from '@/api/user'
import { ElMessageBox } from 'element-plus'
import { User, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const userInfo = ref({
  id: null,
  nickname: '',
  avatarUrl: '',
  role: 0
})

const getImageUrl = (url) => {
  if (!url) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  return url.startsWith('http') ? url : `http://localhost:8080${url}`
}

const fetchUserInfo = async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 1) {
      userInfo.value = res.data
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('token')
    router.replace('/login')
  })
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.nav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #dcdfe6;
  background: #fff;
  padding: 0 40px;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.logo { width: 32px; height: 32px; }
.title { font-size: 18px; font-weight: bold; color: #409eff; }

.header-right {
  display: flex;
  align-items: center;
}

:deep(.el-menu--horizontal) {
  border-bottom: none !important;
}

.user-profile {
  margin-left: 20px;
  display: flex;
  align-items: center;
}

.user-dropdown-link {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  outline: none;
  padding: 5px 8px;
  border-radius: 4px;
  transition: background 0.3s;
}

.user-dropdown-link:hover {
  background: #f5f7fa;
}

.nickname {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.main-body {
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
  padding: 20px;
}
</style>