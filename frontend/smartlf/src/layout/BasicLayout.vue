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
              <el-badge is-dot :hidden="!hasAnyUnread" class="nav-badge">
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
// 🌟 确保这里只有一行关于 interact 的 import，包含所有需要的接口
import { getCommentNotifications, getPrivateMessageNotifications, getMyReceivedClaims, getMySentClaims } from '@/api/interact'
import { ElMessageBox, ElNotification } from 'element-plus'
import { User, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const userInfo = ref({ id: null, nickname: '', avatarUrl: '', role: 0 })
const msgUnreadCount = ref(0)
const noticeUnreadCount = ref(0)
const hasUnprocessedClaim = ref(false)

const hasAnyUnread = computed(() => msgUnreadCount.value > 0 || noticeUnreadCount.value > 0 || hasUnprocessedClaim.value)

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

const fetchInitialUnread = async () => {
  const token = localStorage.getItem('token')
  if (!token) return
  try {
    const [msgRes, noticeRes, claimReceivedRes, claimSentRes] = await Promise.all([
      getPrivateMessageNotifications(),
      getCommentNotifications(),
      getMyReceivedClaims(),
      getMySentClaims() 
    ])
    msgUnreadCount.value = msgRes.data || 0
    const noticeList = noticeRes.data || []
    noticeUnreadCount.value = noticeList.reduce((sum, item) => sum + item.unreadCount, 0)
    
    const receivedClaims = claimReceivedRes.data || []
    const sentClaims = claimSentRes.data || []

    // 帖主视角需要处理 0 和 4，申请人视角需要处理 3
    hasUnprocessedClaim.value = 
      receivedClaims.some(c => c.status === 0 || c.status === 4) || 
      sentClaims.some(c => c.status === 3)

  } catch (error) {
    console.error("获取未读数据失败:", error)
  }
}

const normalizeChatCreateTime = (msg, raw) => {
  if (msg?.createTime) return msg.createTime

  const candidate =
    msg?.sendTime ??
    msg?.time ??
    msg?.timestamp ??
    raw?.createTime ??
    raw?.sendTime ??
    raw?.time ??
    raw?.timestamp

  if (!candidate) return new Date().toISOString()
  if (typeof candidate === 'number') return new Date(candidate).toISOString()

  const parsed = new Date(candidate)
  return Number.isNaN(parsed.getTime()) ? new Date().toISOString() : parsed.toISOString()
}

const initGlobalWebSocket = () => {
  const token = localStorage.getItem('token')
  console.log('🔍 [WS排查 1] 当前的 Token 是:', token)
  
  if (!token) {
    console.warn('⚠️ [WS排查 2] 因为没有 Token，所以取消了 WebSocket 连接！')
    return
  }

  const wsUrl = `ws://localhost:8080/ws/chat/${token}`
  console.log('🚀 [WS排查 3] 准备连接的 WebSocket 地址:', wsUrl)
  
  const ws = new WebSocket(wsUrl)
  window.globalWs = ws 

  ws.onopen = () => {
    console.log('✅ [WS排查 4] WebSocket 连接成功！后端已接通。')
    window.dispatchEvent(new Event('ws-opened'))
  }
  
  ws.onmessage = (event) => {
    console.log('📩 [WS排查 5] 收到 WebSocket 消息:', event.data)
    const res = JSON.parse(event.data)
    const msgType = res.type || res.msgType

    if (msgType === 'error' && res.code === 401) {
      ElMessage.error(res.message || '登录状态已失效，请重新登录')
      localStorage.removeItem('token') // 清除失效的 Token
      
      if (window.globalWs) {
        window.globalWs.close()
        window.globalWs = null
      }
      
      // 强制跳转回登录页
      router.replace('/login')
      return 
    }
    
    if (msgType === 'chat') {
      const msgData = res.data || res
      msgData.createTime = normalizeChatCreateTime(msgData, res)
      if (window.activeChatId !== msgData.senderId) {
        msgUnreadCount.value++
      }
      window.dispatchEvent(new CustomEvent('ws-chat-message', { detail: msgData }))
      
    } else if (msgType === 'notice') {
      fetchInitialUnread()
      const noticeContent = res.content || res.data || '您有一条新的提醒！'
      ElNotification({ 
        title: '系统通知', 
        message: noticeContent,
        type: 'info', // 🌟 这里改成了 info，会显示醒目的蓝色 i 图标
        position: 'bottom-right',
        duration: 5000 
      })
      window.dispatchEvent(new Event('ws-notice-message'))
    }
  }
  
  ws.onclose = (e) => {
    console.warn(`❌ [WS排查 6] WebSocket 连接已断开！状态码: ${e.code}, 原因: ${e.reason}`)
    window.globalWs = null
  }

  ws.onerror = (error) => {
    console.error('💥 [WS排查 7] WebSocket 发生异常错误:', error)
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
  
  // 🌟 就是这句！被我弄丢的罪魁祸首，加上它页面才会去连后端！
  initGlobalWebSocket() 
  
  window.addEventListener('clear-chat-unread', (e) => { 
    msgUnreadCount.value = Math.max(0, msgUnreadCount.value - e.detail) 
  })
  window.addEventListener('refresh-unread', fetchInitialUnread)
})

onUnmounted(() => {
  window.removeEventListener('refresh-unread', fetchInitialUnread)
  if (window.globalWs) window.globalWs.close()
})
</script>

<style scoped>
.nav-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #dcdfe6; background: #fff; padding: 0 40px; height: 60px; }
.header-left { display: flex; align-items: center; gap: 12px; cursor: pointer; }
.logo { width: 32px; height: 32px; }
.title { font-size: 18px; font-weight: bold; color: #409eff; }
.header-right { display: flex; align-items: center; }
:deep(.el-menu--horizontal) { border-bottom: none !important; }
.nav-badge :deep(.el-badge__content) { top: 20px; right: 5px; }
.user-profile { margin-left: 20px; display: flex; align-items: center; }
.user-dropdown-link { display: flex; align-items: center; gap: 10px; cursor: pointer; outline: none; padding: 5px 8px; border-radius: 4px; transition: background 0.3s; }
.user-dropdown-link:hover { background: #f5f7fa; }
.nickname { font-size: 14px; color: #606266; font-weight: 500; }
.main-body { background-color: #f5f7fa; min-height: calc(100vh - 60px); padding: 20px; }
</style>