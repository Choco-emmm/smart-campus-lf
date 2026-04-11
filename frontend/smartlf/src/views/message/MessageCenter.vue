<template>
  <div class="message-container">
    <el-card class="message-card" shadow="never" :body-style="{ padding: 0, display: 'flex', height: '100%' }">
      
      <div class="sidebar">
        <el-menu :default-active="activeTab" class="side-menu" @select="handleMenuSelect">
          <el-menu-item index="chat"><el-icon><ChatDotRound /></el-icon> <span>私信聊天</span></el-menu-item>
          <el-menu-item index="notice"><el-icon><Bell /></el-icon> <span>留言通知</span></el-menu-item>
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
              <el-avatar :size="40" :src="session.targetAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
            </el-badge>
            <div class="session-info">
              <div class="session-header">
                <span class="nickname">{{ session.targetNickname }}</span>
                <span class="time">{{ formatTimeSmall(session.lastMessageTime) }}</span>
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
              <div 
                v-for="msg in chatHistory" 
                :key="msg.id" 
                class="message-bubble-wrapper"
                :class="msg.senderId === currentTargetId ? 'msg-left' : 'msg-right'"
              >
                <el-avatar v-if="msg.senderId === currentTargetId" :size="36" :src="currentTargetAvatar" class="chat-avatar" />
                <div class="bubble-content">
                  <div class="bubble-time" v-if="msg.senderId === currentTargetId">{{ formatTime(msg.createTime) }}</div>
                  <div class="bubble" :class="msg.senderId === currentTargetId ? 'bubble-other' : 'bubble-me'">{{ msg.content }}</div>
                  <div class="bubble-time" v-if="msg.senderId !== currentTargetId">{{ formatTime(msg.createTime) }}</div>
                </div>
                <el-avatar v-if="msg.senderId !== currentTargetId" :size="36" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" class="chat-avatar" />
              </div>
            </div>
            <div class="chat-input-area">
              <el-input v-model="inputMessage" type="textarea" :rows="4" placeholder="按 Enter 发送，Shift + Enter 换行" resize="none" @keydown.enter.prevent="handleEnter" />
              <div class="input-action"><el-button type="primary" :loading="sending" @click="sendMsg">发 送</el-button></div>
            </div>
          </div>
        </template>

        <template v-if="activeTab === 'notice'">
          <div class="notice-window">
            <div class="chat-header"><span>帖子留言提醒</span></div>
            <div class="notice-list" v-loading="noticeLoading">
              <el-empty v-if="noticeList.length === 0" description="暂无新留言提醒" />
              <el-card 
                v-for="notice in noticeList" 
                :key="notice.itemId" 
                shadow="hover" 
                class="notice-card"
                @click="$router.push(`/item/${notice.itemId}`)"
              >
                <div class="notice-content">
                  <div class="notice-title">
                    <el-badge is-dot class="item" v-if="notice.unreadCount > 0"><strong>{{ notice.itemTitle }}</strong></el-badge>
                    <strong v-else>{{ notice.itemTitle }}</strong>
                    <el-tag size="small" type="danger" style="margin-left: 10px;" v-if="notice.unreadCount > 0">{{ notice.unreadCount }} 条新留言</el-tag>
                  </div>
                  <div class="notice-desc">最新留言摘要: {{ notice.lastCommentContent }}</div>
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
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Bell, ChatSquare, ArrowRight } from '@element-plus/icons-vue'
import { getChatSessions, getChatHistory, sendPrivateMessage, getCommentNotifications } from '@/api/interact'

const route = useRoute()
const router = useRouter()
const activeTab = ref('chat') 

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

const handleMenuSelect = (index) => {
  activeTab.value = index
  index === 'chat' ? fetchSessions() : fetchNotices()
}

const fetchSessions = async () => {
  sessionLoading.value = true
  try {
    const res = await getChatSessions()
    sessionList.value = res.data || []
    
    // 🌟 核心：解析路由跳转携带的新联系人参数，创建临时会话
    const query = route.query
    if (query.targetId && activeTab.value === 'chat') {
      const tId = Number(query.targetId)
      // 检查列表里是否已经有和这个人的聊天记录
      let existSession = sessionList.value.find(s => s.targetUserId === tId)
      
      // 没有的话，主动“捏造”一个临时会话，塞到列表最上方
      if (!existSession) {
        existSession = {
          targetUserId: tId,
          targetNickname: query.targetName || '新联系人',
          targetAvatar: query.targetAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
          lastMessageTime: new Date().toISOString(),
          unreadCount: 0
        }
        sessionList.value.unshift(existSession)
      }
      
      // 自动选中这个人开启聊天
      selectSession(existSession)
      // 清理 URL 上的参数，防止页面刷新重复触发
      router.replace('/message') 
    }
  } finally {
    sessionLoading.value = false
  }
}

const selectSession = async (session) => {
  currentTargetId.value = session.targetUserId
  currentTargetName.value = session.targetNickname
  currentTargetAvatar.value = session.targetAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  session.unreadCount = 0 
  
  historyLoading.value = true
  try {
    const res = await getChatHistory(currentTargetId.value)
    chatHistory.value = res.data || []
    scrollToBottom()
  } finally {
    historyLoading.value = false
  }
}

const sendMsg = async () => {
  if (!inputMessage.value.trim()) return
  if (!currentTargetId.value) return ElMessage.warning('请先选择聊天对象')

  sending.value = true
  try {
    await sendPrivateMessage({ receiverId: currentTargetId.value, content: inputMessage.value.trim() })
    chatHistory.value.push({
      id: Date.now(),
      senderId: -1, // 设置为非当前聊天对象的ID，气泡就会出现在右侧
      content: inputMessage.value.trim(),
      createTime: new Date().toISOString()
    })
    inputMessage.value = ''
    scrollToBottom()
    fetchSessions() // 刷新左侧会话最后一条消息的时间
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
  } finally {
    noticeLoading.value = false
  }
}

const formatTime = (timeStr) => timeStr ? timeStr.replace('T', ' ').substring(0, 16) : ''
const formatTimeSmall = (timeStr) => timeStr ? timeStr.split('T')[0].substring(5) : ''

onMounted(() => { fetchSessions() })
</script>

<style scoped>
/* 样式与上版保持一致 */
.message-container { height: calc(100vh - 120px); max-height: 800px; }
.message-card { height: 100%; border-radius: 8px; overflow: hidden; }
.sidebar { width: 260px; background-color: #fafafa; border-right: 1px solid #e4e6eb; display: flex; flex-direction: column; }
.side-menu { background-color: transparent; border-right: none; }
.session-list { flex: 1; overflow-y: auto; padding: 10px 0; }
.session-item { display: flex; align-items: center; padding: 12px 15px; cursor: pointer; transition: background 0.2s; }
.session-item:hover { background-color: #f0f2f5; }
.session-item.active { background-color: #e6f1ff; }
.session-info { margin-left: 12px; flex: 1; overflow: hidden; }
.session-header { display: flex; justify-content: space-between; align-items: center; }
.session-header .nickname { font-size: 14px; font-weight: 500; color: #1d2129; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.session-header .time { font-size: 12px; color: #8a919f; }
.main-content { flex: 1; display: flex; flex-direction: column; background-color: #fff; overflow: hidden; }
.chat-header { height: 60px; border-bottom: 1px solid #e4e6eb; display: flex; align-items: center; padding: 0 20px; font-size: 16px; font-weight: 600; color: #1d2129; }
.empty-chat { flex: 1; display: flex; flex-direction: column; justify-content: center; align-items: center; color: #8a919f; }
.chat-window { flex: 1; display: flex; flex-direction: column; height: 100%; }
.chat-history { flex: 1; padding: 20px; overflow-y: auto; background-color: #f7f8fa; }
.message-bubble-wrapper { display: flex; margin-bottom: 20px; align-items: flex-start; }
.msg-left { justify-content: flex-start; }
.msg-right { justify-content: flex-end; }
.chat-avatar { flex-shrink: 0; margin: 0 12px; }
.bubble-content { display: flex; flex-direction: column; max-width: 60%; }
.msg-right .bubble-content { align-items: flex-end; }
.bubble { padding: 10px 14px; border-radius: 8px; font-size: 14px; line-height: 1.5; word-break: break-all; white-space: pre-wrap; }
.bubble-other { background-color: #fff; border: 1px solid #e4e6eb; color: #1d2129; border-top-left-radius: 2px; }
.bubble-me { background-color: #1e80ff; color: #fff; border-top-right-radius: 2px; }
.bubble-time { font-size: 11px; color: #c0c4cc; margin-bottom: 4px; }
.chat-input-area { height: 160px; border-top: 1px solid #e4e6eb; padding: 15px; background-color: #fff; display: flex; flex-direction: column; }
.chat-input-area :deep(.el-textarea__inner) { border: none; box-shadow: none; padding: 0; }
.chat-input-area :deep(.el-textarea__inner):focus { box-shadow: none; }
.input-action { margin-top: auto; display: flex; justify-content: flex-end; }
.notice-window { flex: 1; display: flex; flex-direction: column; }
.notice-list { padding: 20px; overflow-y: auto; }
.notice-card { margin-bottom: 15px; cursor: pointer; border-radius: 8px; }
.notice-card :deep(.el-card__body) { display: flex; justify-content: space-between; align-items: center; }
.notice-title { font-size: 16px; color: #1d2129; margin-bottom: 8px; }
.notice-desc { font-size: 13px; color: #8a919f; }
</style>