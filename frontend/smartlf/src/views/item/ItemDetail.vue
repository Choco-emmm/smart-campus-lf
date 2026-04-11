<template>
  <div class="detail-layout" v-loading="loading">
    <template v-if="detail">
      <div class="main-content">
        <el-card class="post-card" shadow="never">
          <div class="post-header">
            <div class="header-top">
              <el-tag :type="detail.type === 0 ? 'danger' : 'success'" effect="dark">
                {{ detail.type === 0 ? '寻物启事' : '失物招领' }}
              </el-tag>
              <el-tag v-if="detail.role === 1" type="warning" effect="dark" class="ml-10">官方发布</el-tag>
            </div>
            
            <h1 class="post-title">{{ detail.publicDesc }}</h1>
            <div class="post-meta">
              <span @click="goToProfile(detail.userId)" class="nickname-link pointer">{{ detail.publisherNickname }}</span>
              <span class="dot">·</span>
              <span>发布于 {{ formatTime(detail.createTime) }}</span>
            </div>
          </div>
          
          <el-divider border-style="dashed" />
          
          <div class="info-bar">
            <div class="info-item"><el-icon><PriceTag /></el-icon> 物品：{{ detail.itemName }}</div>
            <div class="info-item"><el-icon><Location /></el-icon> 地点：{{ detail.location }}</div>
            <div class="info-item"><el-icon><Clock /></el-icon> 时间：{{ formatTime(detail.eventTime) }}</div>
          </div>
          
          <div class="desc-content">{{ detail.semiPublicDesc || '楼主很懒，没有留下更详细的描述。' }}</div>
          
          <div class="ai-desc" v-if="detail.aiGeneratedDesc">
            <div class="ai-title"><el-icon><MagicStick /></el-icon> AI 智能提炼</div>
            <div class="ai-content">{{ detail.aiGeneratedDesc }}</div>
          </div>
          
          <div class="image-gallery" v-if="detail.imagesUrlList?.length">
            <el-carousel trigger="click" height="400px" :autoplay="false">
              <el-carousel-item v-for="img in detail.imagesUrlList" :key="img">
                <el-image :src="img" fit="contain" style="width:100%; height:100%; background: #f5f7fa;" :preview-src-list="detail.imagesUrlList" />
              </el-carousel-item>
            </el-carousel>
          </div>
        </el-card>

        <el-card class="comment-card" shadow="never">
          <template #header>
            <div class="card-header"><span>全部留言 ({{ commentList.length }})</span></div>
          </template>

          <div class="comment-input-area">
            <el-input v-model="newComment" type="textarea" :rows="3" placeholder="提供线索或进行交流..." resize="none" />
            <div class="comment-action">
              <el-button type="primary" :loading="commenting" @click="submitComment">发表留言</el-button>
            </div>
          </div>

          <div class="comment-list">
            <el-empty v-if="commentList.length === 0" description="暂无留言，快来抢沙发！" :image-size="60" />
            
            <div class="comment-item" v-for="(comment, index) in commentList" :key="comment.id">
              <el-avatar :size="40" :src="comment.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" @click="goToProfile(comment.userId)" class="pointer" />
              
              <div class="comment-main">
                <div class="comment-header">
                  <div class="user-info">
                    <span class="nickname pointer" @click="goToProfile(comment.userId)">{{ comment.nickname }}</span>
                    <el-tag v-if="comment.userId === detail.userId" size="small" type="primary" effect="plain" class="mini-tag">楼主</el-tag>
                    <el-tag v-if="comment.role === 1" size="small" type="warning" effect="dark" class="mini-tag">管理员</el-tag>
                    <span class="time-text">{{ formatTimeSmall(comment.createTime) }}</span>
                  </div>
                  <div class="modern-floor">#{{ index + 1 }}</div>
                </div>
                <div class="comment-text">{{ comment.content }}</div>
              </div>
            </div>
            
          </div>
        </el-card>
      </div>

      <div class="side-bar">
        <el-card class="publisher-card" shadow="never">
          <div class="publisher-info">
            <el-avatar :size="64" :src="detail.publisherAvatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" @click="goToProfile(detail.userId)" class="pointer" />
            <h3 @click="goToProfile(detail.userId)" class="pointer">{{ detail.publisherNickname }}</h3>
            <el-tag v-if="detail.role === 1" type="warning" size="small" effect="dark">官方管理员</el-tag>
          </div>
          <el-divider />
          
          <div class="action-buttons">
            <template v-if="myUserId === detail.userId">
              <div class="owner-actions">
                <el-button type="primary" size="large" class="full-btn" @click="router.push(`/item/edit/${detail.id}`)">
                  <el-icon><Edit /></el-icon> 修改帖子
                </el-button>
                <el-button type="danger" plain size="large" class="full-btn mt-10" @click="handleDelete">
                  <el-icon><Delete /></el-icon> 删除帖子
                </el-button>
              </div>
            </template>
            
            <template v-else>
             <el-button type="primary" size="large" class="full-btn" v-if="!detail.hasSecureCheck" @click="handleContact">
                <el-icon><ChatDotRound /></el-icon> 私聊联系
              </el-button>
              <el-button type="warning" size="large" class="full-btn" v-else @click="handleVerify">
                <el-icon><Key /></el-icon> 认领核验
              </el-button>
            </template>
          </div>
        </el-card>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
// 🌟 别忘了引入 Delete 图标
import { PriceTag, Location, Clock, MagicStick, ChatDotRound, Key, Edit, Delete } from '@element-plus/icons-vue'
import { getItemDetail, deleteItem } from '@/api/item' // 🌟 引入 deleteItem API
import { getCommentList, addComment } from '@/api/interact'
import { getUserInfo } from '@/api/user'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref(null)
const myUserId = ref(null)

const commentList = ref([])
const newComment = ref('')
const commenting = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getItemDetail(route.params.id)
    detail.value = res.data
    
    try {
      const userRes = await getUserInfo()
      if (userRes.code === 1 || userRes.code === 200) {
        myUserId.value = userRes.data.id
      }
    } catch (e) {}

    await fetchComments()
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  try {
    const res = await getCommentList(route.params.id)
    if (res.code === 1 || res.code === 200) {
      commentList.value = res.data || []
      
      // 🌟 核心修复：如果你是楼主，看完评论后，后端已经设为已读了
      // 此时立刻通知全站（导航栏）刷新红点状态
      if (myUserId.value === detail.value.userId) {
        window.dispatchEvent(new CustomEvent('refresh-unread'))
      }
    }
  } catch (error) {}
}

const submitComment = async () => {
  if (!newComment.value.trim()) return ElMessage.warning('请输入内容')
  commenting.value = true
  try {
    await addComment({ itemId: route.params.id, content: newComment.value.trim() })
    ElMessage.success('留言成功')
    newComment.value = ''
    fetchComments()
  } finally {
    commenting.value = false
  }
}

const handleContact = () => {
  router.push({ path: '/message', query: { targetId: detail.value.userId, targetName: detail.value.publisherNickname, targetAvatar: detail.value.publisherAvatarUrl } })
}

const handleVerify = () => {
  ElMessageBox.prompt(`核验问题：${detail.value.verifyQuestion}`, '认领核验').then(() => {
    ElMessage.success('已提交')
  }).catch(() => {})
}

// 🌟 核心：删除功能
const handleDelete = () => {
  ElMessageBox.confirm(
    '确定要永久删除这篇帖子吗？删除后不可恢复！',
    '高危操作',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    const res = await deleteItem(route.params.id)
    if (res.code === 1 || res.code === 200) {
      ElMessage.success('帖子已成功删除')
      router.replace('/') // 删除后跳回广场首页
    }
  }).catch(() => {}) // 取消就什么都不做
}

const goToProfile = (userId) => router.push(`/profile/${userId}`)
const formatTime = (t) => t ? t.replace('T', ' ').substring(0, 16) : ''
const formatTimeSmall = (t) => t ? t.replace('T', ' ').substring(5, 16) : ''

onMounted(() => fetchData())
</script>

<style scoped>
.owner-actions {
  display: flex;
  flex-direction: column;
  width: 100%;
}
.mt-10 {
  margin-top: 12px;
}
.full-btn {
  width: 100%;
  margin-left: 0 !important; /* 强制消除组件自带的左边距 */
}
.detail-layout { display: flex; gap: 20px; align-items: flex-start; }
.main-content { flex: 1; display: flex; flex-direction: column; gap: 20px; }
.post-card, .comment-card { border-radius: 12px; background: #fff; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: none; }
.header-top { display: flex; align-items: center; margin-bottom: 10px; }
.ml-10 { margin-left: 10px; }
.post-title { font-size: 26px; margin: 0 0 10px; color: #1d2129; line-height: 1.4; }
.post-meta { font-size: 14px; color: #86909c; display: flex; align-items: center; }
.nickname-link { color: #1e80ff; cursor: pointer; font-weight: 500; }
.nickname-link:hover { text-decoration: underline; }
.dot { margin: 0 8px; }
.info-bar { display: flex; gap: 24px; padding: 16px; background: #f7f8fa; border-radius: 8px; margin: 20px 0; font-size: 15px; color: #4e5969; }
.info-item { display: flex; align-items: center; gap: 6px; }
.desc-content { font-size: 17px; color: #1d2129; line-height: 1.8; margin-bottom: 25px; white-space: pre-wrap; }
.ai-desc { background: linear-gradient(135deg, #e8f3ff 0%, #f0f7ff 100%); padding: 18px; border-radius: 10px; border: 1px solid #d0e6ff; margin-bottom: 20px; }
.ai-title { color: #1e80ff; font-weight: bold; margin-bottom: 8px; display: flex; align-items: center; gap: 6px; }
.ai-content { color: #4e5969; font-size: 14.5px; line-height: 1.6; font-style: italic; }
.image-gallery { border-radius: 8px; overflow: hidden; }

/* 现代版评论样式 */
.comment-input-area { margin-bottom: 25px; }
.comment-action { display: flex; justify-content: flex-end; margin-top: 12px; }
.comment-item { display: flex; gap: 16px; padding: 20px 0; border-bottom: 1px solid #f2f3f5; position: relative; }
.comment-item:last-child { border-bottom: none; }
.comment-main { flex: 1; }
.comment-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px; }
.user-info { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.nickname { font-weight: 600; color: #1d2129; font-size: 14px; }
.time-text { font-size: 12px; color: #86909c; margin-left: 5px; }
.mini-tag { height: 18px; border-radius: 4px; padding: 0 4px; font-size: 10px; line-height: 16px; }
.modern-floor { font-size: 13px; font-weight: 600; color: #c0c4cc; font-family: Consolas, Monaco, monospace; }
.comment-text { font-size: 15px; color: #4e5969; line-height: 1.6; padding-right: 20px; }

/* 侧边栏 */
.side-bar { width: 300px; flex-shrink: 0; display: flex; flex-direction: column; gap: 20px; }
.publisher-card { border-radius: 12px; text-align: center; padding: 15px 10px; }
.publisher-info { display: flex; flex-direction: column; align-items: center; gap: 12px; }
.publisher-info h3 { margin: 0; font-size: 18px; }
.btn-group { display: flex; flex-direction: column; width: 100%; }
.full-btn { width: 100%; height: 48px; font-size: 16px; margin-left: 0 !important; }
.mt-10 { margin-top: 10px; }
.pointer { cursor: pointer; }
</style>