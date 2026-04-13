<template>
  <div class="message-container">
    <el-card class="message-card" shadow="never" :body-style="{ padding: 0, display: 'flex', height: '100%' }">
      
      <div class="sidebar">
        <el-menu :default-active="activeTab" class="side-menu" @select="handleMenuSelect">
          <el-menu-item index="chat">
            <el-icon><ChatDotRound /></el-icon> 
            <el-badge :value="totalChatUnread" :hidden="totalChatUnread === 0" class="tab-badge">
              <span>私信聊天</span>
            </el-badge>
          </el-menu-item>
          <el-menu-item index="notice">
            <el-icon><Bell /></el-icon> 
            <el-badge :value="totalNoticeUnread" :hidden="totalNoticeUnread === 0" class="tab-badge">
              <span>留言通知</span>
            </el-badge>
          </el-menu-item>
        </el-menu>

        <div class="session-list" v-if="activeTab === 'chat'" v-loading="sessionLoading">
          <el-empty v-if="sessionList.length === 0" description="暂无私信记录" :image-size="60" />
          <div 
            v-for="session in sessionList" 
            :key="session.targetUserId" 
            class="session-item"
            :class="{ active: currentTargetId === session.targetUserId }"
            @click="selectSession(session)"
          >
            <el-badge :value="session.unreadCount" :hidden="session.unreadCount === 0" :max="99">
              <el-avatar :size="40" :src="getImageUrl(session.targetAvatar)" />
            </el-badge>
            <div class="session-info">
              <div class="session-header">
                <span class="nickname">{{ session.targetNickname }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="main-content">
        <template v-if="activeTab === 'chat'">
          <div v-if="!currentTargetId" class="empty-chat">
            <el-icon size="60" color="#c0c4cc"><ChatSquare /></el-icon>
            <p>选择一个会话开始聊天</p>
          </div>
          <div v-else class="chat-window">
            <div class="chat-header"><span>与 {{ currentTargetName }} 的对话</span></div>
            <div class="chat-history" ref="chatHistoryRef" v-loading="historyLoading">
              <div v-for="msg in chatHistory" :key="msg.id" class="message-bubble-wrapper" :class="msg.senderId === currentTargetId ? 'msg-left' : 'msg-right'">
                <el-avatar v-if="msg.senderId === currentTargetId" :size="36" :src="getImageUrl(currentTargetAvatar)" class="chat-avatar" />
                
                <div class="bubble-content">
                  <div class="bubble" :class="msg.senderId === currentTargetId ? 'bubble-other' : 'bubble-me'">{{ msg.content }}</div>
                  <div class="bubble-time">{{ formatTime(msg.createTime) }}</div>
                </div>
                
                <el-avatar v-if="msg.senderId !== currentTargetId" :size="36" :src="getImageUrl(myAvatar)" class="chat-avatar" />
              </div>
            </div>
            <div class="chat-input-area">
              <el-input v-model="inputMessage" type="textarea" :rows="4" placeholder="说点什么吧..." resize="none" @keydown.enter.prevent="handleEnter" />
              <div class="input-action"><el-button type="primary" :loading="sending" @click="sendMsg">发 送</el-button></div>
            </div>
          </div>
        </template>

        <template v-if="activeTab === 'notice'">
          <div class="notice-window">
            <div class="chat-header"><span>帖子留言提醒</span></div>
            <div class="notice-list" v-loading="noticeLoading">
              <el-empty v-if="noticeList.length === 0" description="暂无新提醒" />
              <el-card 
                v-for="notice in noticeList" 
                :key="notice.itemId" 
                shadow="hover" 
                class="notice-card"
                @click="handleNoticeClick(notice)"
              >
                <div class="notice-content">
                  <div class="notice-title">
                    <el-badge is-dot :hidden="notice.unreadCount === 0">
                      <strong>{{ notice.itemTitle }}</strong>
                    </el-badge>
                    <el-tag size="small" type="danger" style="margin-left: 10px;" v-if="notice.unreadCount > 0">
                      {{ notice.unreadCount }} 条未读
                    </el-tag>
                  </div>
                  <div class="notice-desc">最新回复: {{ notice.lastCommentContent }}</div>
                </div>
                <el-icon><ArrowRight /></el-icon>
              </el-card>
            </div>
          </div>
        </template>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChatDotRound, Bell, ChatSquare, ArrowRight } from '@element-plus/icons-vue'
// 注意这里：去掉了 sendPrivateMessage
import { getChatSessions, getChatHistory, getCommentNotifications } from '@/api/interact'
import { getUserInfo } from '@/api/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const activeTab = ref('chat') 

const myAvatar = ref('')
const sessionList = ref([])
const sessionLoading = ref(false)
const currentTargetId = ref(null)
const currentTargetName = ref('')
const currentTargetAvatar = ref('')
const chatHistory = ref([])
const historyLoading = ref(false)
const inputMessage = ref('')
const sending = ref(false)
const chatHistoryRef = ref(null)

const noticeList = ref([])
const noticeLoading = ref(false)

// 🌟 WebSocket 实例
const ws = ref(null)

const getImageUrl = (url) => {
  if (!url) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  if (url.startsWith('http')) return url
  return `http://localhost:8080${url}`
}

const totalChatUnread = computed(() => sessionList.value.reduce((sum, s) => sum + s.unreadCount, 0))
const totalNoticeUnread = computed(() => noticeList.value.reduce((sum, n) => sum + n.unreadCount, 0))

const notifyHeader = () => {
  window.dispatchEvent(new CustomEvent('refresh-unread'))
}

// 🌟 新增：向后端汇报当前正在查看哪个人的窗口，用于精确判断已读/未读
const reportActiveWindow = (targetId) => {
  if (ws.value && ws.value.readyState === WebSocket.OPEN) {
    ws.value.send(JSON.stringify({
      type: 'active_window',
      targetId: targetId // 传 null 代表没在看任何聊天窗
    }))
  }
}

const handleMenuSelect = (index) => {
  activeTab.value = index
  if (index === 'chat') {
    fetchSessions()
    // 切回聊天列表时，如果此时有选中的人，告诉后端焦点恢复了
    if (currentTargetId.value) reportActiveWindow(currentTargetId.value)
  } else {
    fetchNotices()
    // 切到留言通知时，离开了聊天窗，告诉后端清空焦点
    reportActiveWindow(null)
  }
}

const handleNoticeClick = (notice) => {
  router.push(`/item/${notice.itemId}`)
}

const fetchSessions = async () => {
  sessionLoading.value = true
  try {
    const res = await getChatSessions()
    sessionList.value = res.data || []
  } finally { sessionLoading.value = false }
}

const selectSession = async (session) => {
  currentTargetId.value = session.targetUserId
  currentTargetName.value = session.targetNickname
  currentTargetAvatar.value = session.targetAvatar 
  
  // 🌟 选中了一个人，立刻告诉后端：“我正在盯着他看”
  reportActiveWindow(currentTargetId.value)

  const hasUnread = session.unreadCount > 0

  historyLoading.value = true
  try {
    const res = await getChatHistory(currentTargetId.value)
    chatHistory.value = res.data || []
    scrollToBottom()

    if (hasUnread) {
      session.unreadCount = 0 
      notifyHeader()
    }
  } finally { 
    historyLoading.value = false 
  }
}

const checkQueryAndSelect = () => {
  const { targetId, targetName, targetAvatar } = route.query
  if (targetId) {
    activeTab.value = 'chat'
    const tId = Number(targetId)
    
    let session = sessionList.value.find(s => s.targetUserId === tId)
    
    if (!session) {
      session = {
        targetUserId: tId,
        targetNickname: targetName || '新会话',
        targetAvatar: targetAvatar || '',
        unreadCount: 0
      }
      sessionList.value.unshift(session)
    }
    
    selectSession(session)
    
    router.replace({ path: '/message', query: {} })
  }
}

// 🌟 纯 WebSocket 发送消息，不再发 HTTP 请求
const sendMsg = async () => {
  if (!inputMessage.value.trim() || !currentTargetId.value) return
  
  const content = inputMessage.value.trim()
  const msgData = {
    type: 'chat', // 告诉后端这是聊天消息
    receiverId: currentTargetId.value,
    content: content
  }

  // 检查网络连接
  if (!ws.value || ws.value.readyState !== WebSocket.OPEN) {
    ElMessage.error('聊天服务未连接，请刷新页面重试')
    return
  }

  sending.value = true
  try {
    // 1. 发给后端
    ws.value.send(JSON.stringify(msgData))
    
    // 2. 本地直接把消息放到屏幕上（实现秒回馈）
    chatHistory.value.push({ 
      id: Date.now(), 
      senderId: -1, // 本地伪造一个发送者ID，只要跟对方的不同就会渲染在右侧
      content: content, 
      createTime: new Date().toISOString() 
    })
    
    inputMessage.value = ''
    scrollToBottom()
  } catch (error) {
    ElMessage.error('发送失败，请检查网络')
  } finally { 
    sending.value = false 
  }
}

const handleEnter = (e) => { !e.shiftKey ? sendMsg() : (inputMessage.value += '\n') }
const scrollToBottom = () => { nextTick(() => { if (chatHistoryRef.value) chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight }) }

const fetchNotices = async () => {
  noticeLoading.value = true
  try {
    const res = await getCommentNotifications()
    noticeList.value = res.data || []
  } finally { noticeLoading.value = false }
}

const formatTime = (t) => t ? t.replace('T', ' ').substring(0, 16) : ''

// 🌟 初始化 WebSocket
const initWebSocket = () => {
  const token = localStorage.getItem('token') 
  if (!token) return

  // 建立连接
  const wsUrl = `ws://localhost:8080/ws/chat/${token}`
  ws.value = new WebSocket(wsUrl)

  ws.value.onopen = () => {
    console.log('聊天 WebSocket 连接成功')
    // 如果一进来就处于聊天 tab 且选中了人，补发一次焦点状态
    if (activeTab.value === 'chat' && currentTargetId.value) {
      reportActiveWindow(currentTargetId.value)
    }
  }

  ws.value.onmessage = (event) => {
    try {
      const newMsg = JSON.parse(event.data)
      
      // 1. 如果消息是当前正在聊天的人发来的，直接弹到屏幕上
      if (currentTargetId.value === newMsg.senderId) {
        chatHistory.value.push(newMsg)
        scrollToBottom()
      } else {
        // 2. 如果是别人发来的，给左侧会话列表加小红点
        let session = sessionList.value.find(s => s.targetUserId === newMsg.senderId)
        if (session) {
          session.unreadCount += 1
          // 把有新消息的人顶到最上面
          sessionList.value = [session, ...sessionList.value.filter(s => s.targetUserId !== newMsg.senderId)]
        } else {
          // 如果是个之前没聊过的新人，刷新一下会话列表
          fetchSessions()
        }
        // 触发全局顶部导航栏的铃铛红点
        notifyHeader()
      }
    } catch (e) {
      console.error('解析 WebSocket 消息失败', e)
    }
  }

  ws.value.onclose = () => {
    console.log('聊天 WebSocket 连接关闭')
  }

  ws.value.onerror = (error) => {
    console.error('聊天 WebSocket 发生错误', error)
  }
}

onMounted(async () => { 
  try {
    const res = await getUserInfo()
    myAvatar.value = res.data.avatarUrl
  } catch (e) {}
  
  await fetchSessions()
  checkQueryAndSelect() 
  fetchNotices()

  // 🌟 组件挂载时启动 WebSocket
  initWebSocket()
})

// 🌟 离开页面时一定要断开连接，避免内存泄漏
onUnmounted(() => {
  if (ws.value) {
    ws.value.close()
  }
})
</script>

<style scoped>
.message-container { height: calc(100vh - 120px); max-height: 800px; }
.message-card { height: 100%; border-radius: 12px; overflow: hidden; border: none; box-shadow: 0 4px 16px rgba(0,0,0,0.05); }
.sidebar { width: 260px; background-color: #fafafa; border-right: 1px solid #f0f0f0; }
.side-menu { border-right: none; background: transparent; padding: 10px 0; }
.tab-badge :deep(.el-badge__content) { top: 12px; right: -2px; }
.session-list { flex: 1; overflow-y: auto; }

.session-item { display: flex; align-items: center; padding: 15px; cursor: pointer; transition: all 0.2s; }
.session-item:hover { background-color: #f2f3f5; }
.session-item.active { background-color: #e8f3ff; border-left: 3px solid #1e80ff; }
.session-info { flex: 1; margin-left: 12px; overflow: hidden; }
.session-header { display: flex; align-items: center; height: 100%; }
.nickname { font-weight: 500; color: #1d2129; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-size: 15px; }

.main-content { flex: 1; background: #fff; overflow: hidden; }
.chat-window { height: 100%; display: flex; flex-direction: column; }
.chat-history { flex: 1; padding: 20px; overflow-y: auto; background-color: #f7f8fa; }
.message-bubble-wrapper { display: flex; margin-bottom: 20px; }
.msg-right { justify-content: flex-end; }
.bubble { padding: 10px 14px; border-radius: 8px; font-size: 14px; max-width: 70%; line-height: 1.5; }
.bubble-other { background: #fff; color: #1d2129; border: 1px solid #e5e6eb; }
.bubble-me { background: #1e80ff; color: #fff; }
.bubble-time { font-size: 11px; color: #c0c4cc; margin-top: 4px; text-align: center; width: 100%; display: block; clear: both; }
.notice-list { padding: 20px; overflow-y: auto; }
.notice-card { margin-bottom: 12px; cursor: pointer; border-radius: 10px; }
.notice-card :deep(.el-card__body) { display: flex; justify-content: space-between; align-items: center; }
</style>