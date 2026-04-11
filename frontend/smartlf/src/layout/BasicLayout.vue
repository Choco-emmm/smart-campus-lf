<template>
  <div class="layout-wrapper">
    <header class="forum-header">
      <div class="header-inner">
        <div class="logo-area" @click="router.push('/')">
          <span class="logo-text">SmartLF 校园失物招领</span>
        </div>
        
        <el-menu :default-active="route.path" mode="horizontal" router class="header-menu" :ellipsis="false">
          <el-menu-item index="/">广场首页</el-menu-item>
          <el-menu-item index="/publish">发布信息</el-menu-item>
          <el-menu-item index="/message">
            <el-badge :is-dot="hasGlobalUnread" class="nav-badge">
              消息中心
            </el-badge>
          </el-menu-item>
        </el-menu>

        <div class="user-area">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="el-dropdown-link pointer">
              <el-avatar :size="36" :src="myAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人主页</el-dropdown-item>
                <el-dropdown-item divided command="logout" style="color: #F56C6C;">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main class="forum-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserInfo } from '@/api/user'
import { getChatSessions, getCommentNotifications } from '@/api/interact'

const route = useRoute()
const router = useRouter()
const myUserId = ref(null)
const myAvatar = ref('')
const hasGlobalUnread = ref(false)

// 🌟 核心：拉取最新未读状态
const refreshUnreadStatus = async () => {
  if (!localStorage.getItem('token')) return
  try {
    const [sessions, notices] = await Promise.all([
      getChatSessions(),
      getCommentNotifications()
    ])
    const unreadChat = (sessions.data || []).some(s => s.unreadCount > 0)
    const unreadNotice = (notices.data || []).some(n => n.unreadCount > 0)
    hasGlobalUnread.value = unreadChat || unreadNotice
  } catch (e) {}
}

const getImageUrl = (url) => {
  if (!url) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  if (url.startsWith('http')) return url
  return `http://localhost:8080${url}`
}

const fetchMyInfo = async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 1) {
      myUserId.value = res.data.id
      // 🌟 修复：直接处理好图片的显示路径
      myAvatar.value = getImageUrl(res.data.avatarUrl)
    }
  } catch (e) {}
}

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    router.push('/login')
  } else if (command === 'profile') {
    router.push(`/profile/${myUserId.value}`)
  }
}

// 🌟 监听路由变化，切换页面时重刷红点
watch(() => route.path, () => {
  refreshUnreadStatus()
})

onMounted(() => {
  fetchMyInfo()
  refreshUnreadStatus()
  // 🌟 监听自定义事件：当消息中心通知我“消息已读”时，立刻重刷
  window.addEventListener('refresh-unread', refreshUnreadStatus)
})

onUnmounted(() => {
  window.removeEventListener('refresh-unread', refreshUnreadStatus)
})
</script>

<style scoped>
.layout-wrapper { min-height: 100vh; display: flex; flex-direction: column; background-color: #f5f7f9; }
.forum-header { position: sticky; top: 0; z-index: 100; background: #fff; border-bottom: 1px solid #e5e6eb; box-shadow: 0 1px 4px rgba(0,0,0,0.03); }
.header-inner { max-width: 1200px; margin: 0 auto; height: 60px; display: flex; align-items: center; padding: 0 20px; }
.logo-text { font-size: 22px; font-weight: 800; color: #1e80ff; cursor: pointer; }
.header-menu { flex: 1; margin-left: 40px; border-bottom: none; }
.nav-badge :deep(.el-badge__content.is-fixed.is-dot) { right: -2px; top: 16px; }
.user-area { margin-left: 20px; }
.pointer { cursor: pointer; outline: none; }
.forum-main { flex: 1; width: 100%; max-width: 1200px; margin: 20px auto; padding: 0 20px; box-sizing: border-box; }
</style>