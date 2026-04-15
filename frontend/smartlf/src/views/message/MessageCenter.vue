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
          <el-menu-item index="claim">
            <el-icon><Stamp /></el-icon> 
            <el-badge is-dot :hidden="!hasUnprocessedClaim" class="tab-badge">
              <span>认领申请</span>
            </el-badge>
          </el-menu-item>
          
          <el-menu-item index="ai">
            <el-icon><MagicStick /></el-icon> 
            <span>AI 助手</span>
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

        <template v-if="activeTab === 'claim'">
          <div class="claim-window">
            <div class="chat-header"><span>认领申请管理</span></div>
            <div class="claim-list" v-loading="claimLoading">
              <el-tabs v-model="claimActiveTab" class="custom-tabs" @tab-change="fetchClaims">
                
                <el-tab-pane label="我收到的申请 (帖主审核)" name="received">
                  <el-empty v-if="receivedClaims.length === 0" description="暂无收到认领申请" />
                  <el-card v-for="claim in receivedClaims" :key="claim.id" class="claim-card" shadow="hover">
                    <div class="claim-item-header">
                      <span class="claim-title">申请帖子：<span class="highlight" @click="router.push(`/item/${claim.itemId}`)">{{ claim.itemName }}</span></span>
                      <el-tag :type="getClaimStatusType(claim.status)">{{ getClaimStatusText(claim.status) }}</el-tag>
                    </div>
                    <div class="claim-item-body">
                      <div class="user-info">
                        <el-avatar :size="30" :src="getImageUrl(claim.targetAvatar)" />
                        <span>申请人: {{ claim.targetNickname }}</span>
                      </div>
                      
                      <div class="q-a-box">
                        <p><strong>核验问题:</strong> {{ claim.verifyQuestion || '未设置' }}</p>
                        <p><strong>我的原答案:</strong> <span style="color: #67c23a; font-weight: bold;">{{ claim.verifyAnswer || '未设置' }}</span></p>
                        <el-divider border-style="dashed" style="margin: 8px 0" />
                        <p><strong>对方初始答案:</strong> {{ claim.answer }}</p>

                        <template v-if="claim.supplementQuestion || claim.supplementAnswer">
                          <div class="supplement-area">
                            <p v-if="claim.supplementQuestion"><strong>我提出的补充要求:</strong> {{ claim.supplementQuestion }}</p>
                            <p v-if="claim.supplementAnswer"><strong>对方补充的证据:</strong> <span style="color: #409eff;">{{ claim.supplementAnswer }}</span></p>
                          </div>
                        </template>
                      </div>

                      <div v-if="claim.status === 1 && claim.pickupCode" class="pickup-code-box">
                        <div style="font-size: 15px;">
                          <el-icon><Unlock /></el-icon> 认领已达成！对方的取件码应为：<span class="code">{{ claim.pickupCode }}</span>
                        </div>
                        <div class="expire-time-text">
                          <el-icon><Phone /></el-icon> 系统已向对方展示您的联系方式：<strong>{{ claim.publisherContact || '未预留' }}</strong>
                        </div>
                        <div class="expire-time-text">
                          <el-icon><Clock /></el-icon> 取件码有效期至：{{ formatTime(claim.codeExpireTime) || '已过期' }}
                        </div>
                      </div>

                    </div>
                    <div class="claim-item-footer" v-if="claim.status === 0 || claim.status === 4">
                      <el-button type="success" size="small" @click="handleAudit(claim.id, 1)">同意认领</el-button>
                      <el-button v-if="claim.status === 0" type="warning" size="small" @click="handleAudit(claim.id, 3)" plain>要求补充证据</el-button>
                      <el-button type="danger" size="small" @click="handleAudit(claim.id, 2)" plain>拒绝</el-button>
                    </div>
                  </el-card>
                </el-tab-pane>

                <el-tab-pane label="我发出的申请 (进度追踪)" name="sent">
                  <el-empty v-if="sentClaims.length === 0" description="暂无发出认领申请" />
                  <el-card v-for="claim in sentClaims" :key="claim.id" class="claim-card" shadow="hover">
                    <div class="claim-item-header">
                      <span class="claim-title">目标帖子：<span class="highlight" @click="router.push(`/item/${claim.itemId}`)">{{ claim.itemName }}</span></span>
                      <el-tag :type="getClaimStatusType(claim.status)">{{ getClaimStatusText(claim.status) }}</el-tag>
                    </div>
                    <div class="claim-item-body">
                      
                      <div class="q-a-box">
                        <p><strong>核验问题:</strong> {{ claim.verifyQuestion || '未设置' }}</p>
                        <p><strong>我提交的初始答案:</strong> {{ claim.answer }}</p>

                        <template v-if="claim.supplementQuestion || claim.supplementAnswer">
                          <div class="supplement-area">
                            <p v-if="claim.supplementQuestion"><strong>帖主补充要求:</strong> <span style="color: #e6a23c; font-weight: bold;">{{ claim.supplementQuestion }}</span></p>
                            <p v-if="claim.supplementAnswer"><strong>我的补充证据:</strong> {{ claim.supplementAnswer }}</p>
                          </div>
                        </template>
                      </div>

                      <div v-if="claim.status === 1 && claim.pickupCode" class="pickup-code-box">
                        <div style="font-size: 15px;">
                          <el-icon><Unlock /></el-icon> 审核通过！系统生成的取件码：<span class="code">{{ claim.pickupCode }}</span>
                        </div>
                        <div class="expire-time-text">
                          <el-icon><Phone /></el-icon> 帖主预留联系方式：<span style="color: #409eff; font-weight: bold; font-size: 15px;">{{ claim.publisherContact || '帖主未留联系方式，请发私信联系' }}</span>
                        </div>
                        <div class="expire-time-text">
                          <el-icon><Clock /></el-icon> 取件码有效期至：{{ formatTime(claim.codeExpireTime) || '已过期' }}
                        </div>
                      </div>

                    </div>
                    <div class="claim-item-footer" v-if="claim.status === 3">
                      <el-button type="primary" size="small" @click="promptSupplement(claim.id)">提交补充材料</el-button>
                    </div>
                  </el-card>
                </el-tab-pane>

              </el-tabs>
            </div>
          </div>
        </template>

        <template v-if="activeTab === 'ai'">
          <div class="chat-window">
            <div class="chat-header"><span>🤖 AI 失物招领百事通 (基于大模型)</span></div>
            <div class="chat-history" ref="aiChatHistoryRef">
              <div v-for="msg in aiChatHistory" :key="msg.id" class="message-bubble-wrapper" :class="msg.sender === 'user' ? 'msg-right' : 'msg-left'">
                
                <el-avatar v-if="msg.sender === 'ai'" :size="36" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" class="chat-avatar" />
                
                <div class="bubble-content">
                  <div class="bubble" :class="msg.sender === 'user' ? 'bubble-me' : 'bubble-other'" style="white-space: pre-wrap; line-height: 1.6;">
                    <span v-if="msg.loading" class="typing-indicator"><el-icon class="is-loading"><Loading /></el-icon> AI 思考中...</span>
                    <span v-else>{{ msg.content }}</span>
                  </div>
                </div>
                
                <el-avatar v-if="msg.sender === 'user'" :size="36" :src="getImageUrl(myAvatar)" class="chat-avatar" />
              </div>
            </div>
            
            <div class="chat-input-area">
              <el-input v-model="aiInputMessage" type="textarea" :rows="4" placeholder="问问 AI 怎么找东西、或者失物招领的注意事项..." resize="none" @keydown.enter.prevent="handleAiEnter" />
              <div class="input-action">
                <el-button @click="clearAiSession">清空记忆</el-button>
                <el-button type="primary" :loading="aiSending" @click="sendAiMsg">发 送</el-button>
              </div>
            </div>
          </div>
        </template>

      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref,reactive, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
// 🌟 新增 MagicStick, Loading 图标
import { ChatDotRound, Bell, ChatSquare, ArrowRight, Stamp, Unlock, Clock, Phone, MagicStick, Loading } from '@element-plus/icons-vue'
import { getChatSessions, getChatHistory, getCommentNotifications, getMyReceivedClaims, getMySentClaims, auditClaim, supplementClaim } from '@/api/interact'
import { getUserInfo } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

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

const claimActiveTab = ref('received')
const claimLoading = ref(false)
const receivedClaims = ref([])
const sentClaims = ref([])

const getImageUrl = (url) => {
  if (!url) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  return url.startsWith('http') ? url : `http://localhost:8080${url}`
}

const totalChatUnread = computed(() => sessionList.value.reduce((sum, s) => sum + s.unreadCount, 0))
const totalNoticeUnread = computed(() => noticeList.value.reduce((sum, n) => sum + n.unreadCount, 0))

const hasUnprocessedClaim = computed(() => {
  const receivedNeedsAction = receivedClaims.value.some(c => c.status === 0 || c.status === 4)
  const sentNeedsAction = sentClaims.value.some(c => c.status === 3)
  return receivedNeedsAction || sentNeedsAction
})

const reportActiveWindow = (targetId) => {
  window.activeChatId = targetId 
  if (window.globalWs && window.globalWs.readyState === WebSocket.OPEN) {
    window.globalWs.send(JSON.stringify({ type: 'active_window', targetId: targetId }))
  }
}

const handleMenuSelect = (index) => {
  activeTab.value = index
  if (index === 'chat') {
    fetchSessions()
    if (currentTargetId.value) reportActiveWindow(currentTargetId.value)
  } else if (index === 'notice') {
    fetchNotices()
    reportActiveWindow(null)
  } else if (index === 'claim') {
    fetchClaims()
    reportActiveWindow(null)
  } else if (index === 'ai') {
    reportActiveWindow(null)
    scrollToAiBottom()
  }
  window.dispatchEvent(new Event('refresh-unread'))
}

const handleNoticeClick = (notice) => { router.push(`/item/${notice.itemId}`) }

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
  reportActiveWindow(currentTargetId.value)

  const hasUnreadCount = session.unreadCount
  historyLoading.value = true
  try {
    const res = await getChatHistory(currentTargetId.value)
    chatHistory.value = res.data || []
    scrollToBottom()

    if (hasUnreadCount > 0) {
      session.unreadCount = 0 
      window.dispatchEvent(new CustomEvent('clear-chat-unread', { detail: hasUnreadCount }))
    }
  } finally { historyLoading.value = false }
}

const checkQueryAndSelect = () => {
  const { targetId, targetName, targetAvatar } = route.query
  if (targetId) {
    activeTab.value = 'chat'
    const tId = Number(targetId)
    let session = sessionList.value.find(s => s.targetUserId === tId)
    if (!session) {
      session = { targetUserId: tId, targetNickname: targetName || '新会话', targetAvatar: targetAvatar || '', unreadCount: 0 }
      sessionList.value.unshift(session)
    }
    selectSession(session)
    router.replace({ path: '/message', query: {} })
  }
}

const sendMsg = async () => {
  if (!inputMessage.value.trim() || !currentTargetId.value) return
  const content = inputMessage.value.trim()
  const msgData = { type: 'chat', receiverId: currentTargetId.value, content: content }

  if (!window.globalWs || window.globalWs.readyState !== WebSocket.OPEN) {
    return ElMessage.error('聊天服务未连接，请刷新页面')
  }

  sending.value = true
  try {
    window.globalWs.send(JSON.stringify(msgData))
    chatHistory.value.push({ id: Date.now(), senderId: -1, content: content, createTime: new Date().toISOString() })
    inputMessage.value = ''
    scrollToBottom()
  } catch (error) {
    ElMessage.error('发送失败')
  } finally { sending.value = false }
}

const handleEnter = (e) => { !e.shiftKey ? sendMsg() : (inputMessage.value += '\n') }
const scrollToBottom = () => { nextTick(() => { if (chatHistoryRef.value) chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight }) }

// ==========================================
// 🌟 AI 会话核心逻辑区
// ==========================================
const aiChatHistoryRef = ref(null)
const aiInputMessage = ref('')
const aiSending = ref(false)

// 1. 生成唯一 SessionId 以利用后端的记忆体
const generateUUID = () => window.crypto?.randomUUID ? crypto.randomUUID() : (Math.random().toString(36).substring(2) + Date.now().toString(36))
const aiSessionId = ref(generateUUID())

const aiChatHistory = ref([
  { id: 1, sender: 'ai', content: '你好！我是校园失物招领专属 AI 小助手。你可以问我寻物技巧、失物招领流程，或者让我帮你写一段清晰的招领描述~', loading: false }
])

const scrollToAiBottom = () => { nextTick(() => { if (aiChatHistoryRef.value) aiChatHistoryRef.value.scrollTop = aiChatHistoryRef.value.scrollHeight }) }

const clearAiSession = () => {
  ElMessageBox.confirm('确定要清空与 AI 的当前对话记忆吗？', '提示', { type: 'warning' }).then(() => {
    aiSessionId.value = generateUUID() // 刷新 ID 即可斩断后端的 ChatMemory
    aiChatHistory.value = [{ id: Date.now(), sender: 'ai', content: '记忆已清空，我们重新开始吧！', loading: false }]
  }).catch(() => {})
}

const sendAiMsg = async () => {
  if (!aiInputMessage.value.trim() || aiSending.value) return
  const userText = aiInputMessage.value.trim()
  aiInputMessage.value = ''
  aiChatHistory.value.push({ id: Date.now(), sender: 'user', content: userText })

  // 🌟 修改点 1: 使用 reactive 定义消息对象，使其自身具备响应式
  const aiMsgObj = reactive({ 
    id: Date.now() + 1, 
    sender: 'ai', 
    content: '', 
    loading: true 
  })
  aiChatHistory.value.push(aiMsgObj)
  scrollToAiBottom()
  aiSending.value = true

  try {
    const token = localStorage.getItem('token') || ''
    const response = await fetch(`http://localhost:8080/ai/chat/stream?sessionId=${aiSessionId.value}&message=${encodeURIComponent(userText)}`, {
      method: 'GET',
      headers: { 'token': token, 'Accept': 'text/event-stream' }
    })

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      buffer += decoder.decode(value, { stream: true })
      
      // 🌟 修改点 2: 增强解析逻辑，处理可能出现的 \r\n (Windows) 换行符
      const events = buffer.split(/\n\n|\r\n\r\n/)
      buffer = events.pop() 

      for (const event of events) {
        if (aiMsgObj.loading) aiMsgObj.loading = false // 收到第一行数据时关闭 loading
        
        const lines = event.split(/\r?\n/)
        for (const line of lines) {
          if (line.startsWith('data:')) {
            let text = line.substring(5)
            if (text.startsWith(' ')) text = text.substring(1) 
            aiMsgObj.content += text // 🌟 这里修改的是 reactive 对象，UI 会实时跳字
          }
        }
      }
      scrollToAiBottom()
    }
  } catch (error) {
    aiMsgObj.loading = false
    aiMsgObj.content += '\n\n*(连接中断)*'
  } finally {
    aiSending.value = false
  }
}
const handleAiEnter = (e) => { !e.shiftKey ? sendAiMsg() : (aiInputMessage.value += '\n') }
// ==========================================

const fetchNotices = async () => {
  noticeLoading.value = true
  try {
    const res = await getCommentNotifications()
    noticeList.value = res.data || []
  } finally { noticeLoading.value = false }
}

const getClaimStatusText = (s) => ({ 0: '待审核', 1: '已同意', 2: '已拒绝', 3: '要求补充证据', 4: '证据已补充(待决)' }[s] || '未知')
const getClaimStatusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger', 3: 'info', 4: 'primary' }[s] || 'info')

const fetchClaims = async () => {
  claimLoading.value = true
  try {
    const [receivedRes, sentRes] = await Promise.all([
      getMyReceivedClaims(),
      getMySentClaims()
    ])
    receivedClaims.value = receivedRes.data || []
    sentClaims.value = sentRes.data || []
  } catch(e) {
    console.error("加载列表失败", e)
  } finally {
    claimLoading.value = false
  }
}

const handleAudit = (id, action) => {
  if (action === 3) {
    ElMessageBox.prompt('请输入您需要对方补充说明的内容：', '要求补充证据', {
      confirmButtonText: '发送要求',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '补充要求不能为空'
    }).then(async ({ value }) => {
      await auditClaim({ claimId: id, status: action, supplementQuestion: value }) 
      ElMessage.success('已发送补充证据要求')
      fetchClaims()
      window.dispatchEvent(new Event('refresh-unread'))
    }).catch(() => {})
    return;
  }

  const actionName = action === 1 ? '同意该认领' : '拒绝该认领'
  ElMessageBox.confirm(`确定要执行操作：【${actionName}】 吗？`, '审核确认', { type: 'warning' }).then(async () => {
    await auditClaim({ claimId: id, status: action }) 
    ElMessage.success('审核操作成功')
    fetchClaims()
    window.dispatchEvent(new Event('refresh-unread'))
  }).catch(() => {})
}

const promptSupplement = (id) => {
  ElMessageBox.prompt('请输入补充的细节描述或证据信息：', '提交补充材料', {
    confirmButtonText: '提交',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '内容不能为空'
  }).then(async ({ value }) => {
    await supplementClaim({ claimId: id, supplementAnswer: value }) 
    ElMessage.success('补充材料提交成功')
    fetchClaims()
  }).catch(() => {})
}

const formatTime = (t) => t ? t.replace('T', ' ').substring(0, 16) : ''

const handleIncomingChat = (e) => {
  const newMsg = e.detail
  if (currentTargetId.value === newMsg.senderId) {
    chatHistory.value.push(newMsg)
    scrollToBottom()
  } else {
    let session = sessionList.value.find(s => s.targetUserId === newMsg.senderId)
    if (session) {
      session.unreadCount += 1
      sessionList.value = [session, ...sessionList.value.filter(s => s.targetUserId !== newMsg.senderId)]
    } else {
      fetchSessions()
    }
  }
}

const handleIncomingNotice = () => { 
  if (activeTab.value === 'notice') fetchNotices() 
  fetchClaims()
}

const handleWsOpened = () => { if (activeTab.value === 'chat' && currentTargetId.value) reportActiveWindow(currentTargetId.value) }

onMounted(async () => { 
  try {
    const res = await getUserInfo()
    myAvatar.value = res.data.avatarUrl
  } catch (e) {}
  
  await fetchSessions()
  checkQueryAndSelect() 
  fetchNotices()
  fetchClaims()

  window.addEventListener('ws-chat-message', handleIncomingChat)
  window.addEventListener('ws-notice-message', handleIncomingNotice)
  window.addEventListener('ws-opened', handleWsOpened)
})

onUnmounted(() => {
  window.removeEventListener('ws-chat-message', handleIncomingChat)
  window.removeEventListener('ws-notice-message', handleIncomingNotice)
  window.removeEventListener('ws-opened', handleWsOpened)
  reportActiveWindow(null)
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
.chat-window, .claim-window { height: 100%; display: flex; flex-direction: column; }
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

/* 认领模块特有样式 */
.claim-list { flex: 1; padding: 15px 25px; overflow-y: auto; background-color: #f7f8fa; }
.custom-tabs :deep(.el-tabs__item) { font-size: 15px; }
.claim-card { margin-bottom: 15px; border-radius: 8px; }
.claim-item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; padding-bottom: 10px; border-bottom: 1px dashed #ebeef5; }
.claim-title { font-weight: bold; color: #303133; }
.highlight { color: #409eff; cursor: pointer; }
.highlight:hover { text-decoration: underline; }
.claim-item-body { margin-bottom: 15px; font-size: 14px; color: #606266; }
.user-info { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.q-a-box { background: #f5f7fa; padding: 15px; border-radius: 6px; }
.q-a-box p { margin: 6px 0; }
.supplement-area { margin-top: 10px; padding-top: 10px; border-top: 1px dashed #dcdfe6; background-color: #fafafa; padding: 10px; border-radius: 4px; }
.pickup-code-box { margin-top: 12px; padding: 15px; background: #f0f9eb; color: #67c23a; border-radius: 6px; font-weight: bold; border: 1px solid #e1f3d8; }
.expire-time-text { font-size: 13px; color: #909399; margin-top: 8px; border-top: 1px dashed #e1f3d8; padding-top: 8px; font-weight: normal; display: flex; align-items: center; gap: 4px;}
.code { color: #f56c6c; font-size: 18px; margin-left: 5px; letter-spacing: 1px; }
.claim-item-footer { text-align: right; }
</style>