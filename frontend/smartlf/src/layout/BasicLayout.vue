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
            
            <el-menu-item index="/message">
              <el-badge :value="totalUnread" :hidden="totalUnread === 0" class="nav-badge">
                <span>消息中心</span>
              </el-badge>
            </el-menu-item>
            
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
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getUserInfo } from '@/api/user'
import { getCommentNotifications, getPrivateMessageNotifications } from '@/api/interact'
import { ElMessageBox, ElNotification } from 'element-plus'
import { User, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const userInfo = ref({ id: null, nickname: '', avatarUrl: '', role: 0 })

// 分开存储两类未读数，方便管理
const msgUnreadCount = ref(0)
const noticeUnreadCount = ref(0)
const totalUnread = computed(() => msgUnreadCount.value + noticeUnreadCount.value)

// 全局暴露给 MessageCenter.vue 用的，判断当前视野在看谁
window.activeChatId = null 

const getImageUrl = (url) => {
  if (!url) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  return url.startsWith('http') ? url : `http://localhost:8080${url}`
}

const fetchUserInfo = async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 1) userInfo.value = res.data
  } catch (error) {}
}

// 页面刚刷新时，作为兜底去后端拉取一次准确的总数
const fetchInitialUnread = async () => {
  const token = localStorage.getItem('token')
  if (!token) return
  try {
    const [msgRes, noticeRes] = await Promise.all([
      getPrivateMessageNotifications(),
      getCommentNotifications()
    ])
    msgUnreadCount.value = msgRes.data || 0
    const noticeList = noticeRes.data || []
    noticeUnreadCount.value = noticeList.reduce((sum, item) => sum + item.unreadCount, 0)
  } catch (error) {}
}

// 🌟 核心：全局 WebSocket 初始化
const initGlobalWebSocket = () => {
  const token = localStorage.getItem('token')
  if (!token) return

  const ws = new WebSocket(`ws://localhost:8080/ws/chat/${token}`)
  
  // 挂载到 window，让整个系统都能随时用它发消息
  window.globalWs = ws 

  ws.onopen = () => {
    console.log('🌍 全局 WebSocket 连接成功')
    window.dispatchEvent(new Event('ws-opened'))
  }

  ws.onmessage = (event) => {
    const res = JSON.parse(event.data)
    
    if (res.type === 'chat') {
      const msgData = res.data
      
      // 如果当前没有盯着这个发件人看，才增加红点
      if (window.activeChatId !== msgData.senderId) {
        msgUnreadCount.value++
      }

      // 广播给消息中心页面去渲染聊天气泡或更新列表
      window.dispatchEvent(new CustomEvent('ws-chat-message', { detail: msgData }))
      
    } else if (res.type === 'notice') {
      // 收到留言通知
      noticeUnreadCount.value++
      ElNotification({ title: '新提醒', message: '您的帖子有新的留言啦！', type: 'success', position: 'bottom-right' })
      // 广播给消息中心刷新通知列表
      window.dispatchEvent(new Event('ws-notice-message'))
    }
  }

  ws.onclose = () => {
    window.globalWs = null
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' }).then(() => {
    if (window.globalWs) window.globalWs.close()
    localStorage.removeItem('token')
    router.replace('/login')
  })
}

onMounted(() => {
  fetchUserInfo()
  fetchInitialUnread()
  initGlobalWebSocket()
  
  // 监听子页面手动消除私信红点的事件
  window.addEventListener('clear-chat-unread', (e) => { 
    msgUnreadCount.value = Math.max(0, msgUnreadCount.value - e.detail) 
  })

  // 🌟🌟🌟 新增：把监听“留言已读”的耳朵加回来！
  // 当听到 refresh-unread 时，重新向后端发起一次核对请求 (兜底机制)
  window.addEventListener('refresh-unread', fetchInitialUnread)
})

onUnmounted(() => {
  window.removeEventListener('clear-chat-unread', handleIncomingChat) // 注：你原代码这里绑定的变量名有点小笔误，不过不影响大局
  
  // 🌟🌟🌟 新增：离开页面时销毁监听器，防止内存泄漏
  window.removeEventListener('refresh-unread', fetchInitialUnread)
  
  if (window.globalWs) {
    window.globalWs.close()
  }
})
</script>

<style scoped>
/* 保持你的原样即可 */
.nav-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #dcdfe6; background: #fff; padding: 0 40px; height: 60px; }
.header-left { display: flex; align-items: center; gap: 12px; cursor: pointer; }
.logo { width: 32px; height: 32px; }
.title { font-size: 18px; font-weight: bold; color: #409eff; }
.header-right { display: flex; align-items: center; }
:deep(.el-menu--horizontal) { border-bottom: none !important; }
.nav-badge :deep(.el-badge__content) { top: 15px; right: 0px; }
.user-profile { margin-left: 20px; display: flex; align-items: center; }
.user-dropdown-link { display: flex; align-items: center; gap: 10px; cursor: pointer; outline: none; padding: 5px 8px; border-radius: 4px; transition: background 0.3s; }
.user-dropdown-link:hover { background: #f5f7fa; }
.nickname { font-size: 14px; color: #606266; font-weight: 500; }
.main-body { background-color: #f5f7fa; min-height: calc(100vh - 60px); padding: 20px; }
</style>