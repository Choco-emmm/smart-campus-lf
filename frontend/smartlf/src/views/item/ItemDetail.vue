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
              <el-tag v-if="detail.role === 1" type="warning" effect="dark" class="ml-10">管理员发布</el-tag>
            </div>
            <h1 class="post-title">{{ detail.publicDesc }}</h1>
            <div class="post-meta">
              <span @click="goToProfile(detail.userId)" class="nickname-link">{{ detail.publisherNickname }}</span>
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

          <div class="desc-content">{{ detail.semiPublicDesc || '暂无详细描述' }}</div>

          <div class="ai-desc" v-if="detail.aiGeneratedDesc">
            <div class="ai-title"><el-icon><MagicStick /></el-icon> AI 智能描述</div>
            <div class="ai-content">{{ detail.aiGeneratedDesc }}</div>
          </div>

          <div class="image-gallery" v-if="detail.imagesUrlList?.length">
            <el-carousel trigger="click" height="400px" :autoplay="false">
              <el-carousel-item v-for="img in detail.imagesUrlList" :key="img">
                <el-image :src="img" fit="contain" class="full-img" :preview-src-list="detail.imagesUrlList" />
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
            <el-empty v-if="commentList.length === 0" description="暂无留言" :image-size="60" />
            
            <div class="comment-item" v-for="(comment, index) in commentList" :key="comment.id">
              <el-avatar :size="40" :src="comment.avatarUrl" @click="goToProfile(comment.userId)" class="pointer" />
              <div class="comment-main">
                <div class="comment-user">
                  <div class="user-info">
                    <span class="nickname pointer" @click="goToProfile(comment.userId)">{{ comment.nickname }}</span>
                    <div class="tags-row">
                      <el-tag v-if="comment.userId === detail.userId" size="small" type="primary" effect="plain" class="mini-tag">楼主</el-tag>
                      <el-tag v-if="comment.role === 1" size="small" type="warning" effect="dark" class="mini-tag">管理员</el-tag>
                    </div>
                  </div>
                  <div class="floor-info">
                    <span class="floor-text">{{ index + 1 }} 楼</span>
                    <span class="time-text">{{ formatTimeSmall(comment.createTime) }}</span>
                  </div>
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
            <el-avatar :size="64" :src="detail.publisherAvatarUrl" @click="goToProfile(detail.userId)" class="pointer" />
            <h3 @click="goToProfile(detail.userId)" class="pointer">{{ detail.publisherNickname }}</h3>
            <el-tag v-if="detail.role === 1" type="warning" size="small" effect="dark">官方管理员</el-tag>
          </div>
          <el-divider />
          <div class="action-buttons">
            <template v-if="myUserId !== detail.userId">
              <el-button type="primary" size="large" class="full-btn" v-if="!detail.hasSecureCheck" @click="handleContact">
                <el-icon><ChatDotRound /></el-icon> 私聊联系
              </el-button>
              <el-button type="warning" size="large" class="full-btn" v-else @click="handleVerify">
                <el-icon><Key /></el-icon> 认领核验
              </el-button>
            </template>
            <div v-else class="self-tip">这是你发布的帖子</div>
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
import { PriceTag, Location, Clock, MagicStick, ChatDotRound, Key } from '@element-plus/icons-vue'
import { getItemDetail } from '@/api/item'
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
    const userRes = await getUserInfo()
    myUserId.value = userRes.data.id
    fetchComments()
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  const res = await getCommentList(route.params.id)
  commentList.value = res.data || []
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
  router.push({
    path: '/message',
    query: { targetId: detail.value.userId, targetName: detail.value.publisherNickname, targetAvatar: detail.value.publisherAvatarUrl }
  })
}

const handleVerify = () => {
  ElMessageBox.prompt(`核验问题：${detail.value.verifyQuestion}`, '认领核验').then(() => {
    ElMessage.success('已提交')
  })
}

const goToProfile = (userId) => router.push(`/profile/${userId}`)
const formatTime = (t) => t ? t.replace('T', ' ').substring(0, 16) : ''
const formatTimeSmall = (t) => t ? t.replace('T', ' ').substring(5, 16) : ''

onMounted(() => fetchData())
</script>

<style scoped>
.detail-layout { display: flex; gap: 20px; align-items: flex-start; }
.main-content { flex: 1; display: flex; flex-direction: column; gap: 20px; }
.post-card, .comment-card { border-radius: 12px; background: #fff; border: none; box-shadow: 0 2px 12px rgba(0,0,0,0.04); }
.ml-10 { margin-left: 10px; }
.post-title { font-size: 26px; margin: 15px 0 10px; color: #1d2129; line-height: 1.4; }
.post-meta { font-size: 14px; color: #86909c; }
.nickname-link { color: #1e80ff; cursor: pointer; font-weight: 500; }
.info-bar { display: flex; gap: 24px; padding: 16px; background: #f7f8fa; border-radius: 8px; margin: 20px 0; font-size: 15px; color: #4e5969; }
.info-item { display: flex; align-items: center; gap: 6px; }
.desc-content { font-size: 17px; color: #1d2129; line-height: 1.8; margin-bottom: 25px; white-space: pre-wrap; }
.ai-desc { background: linear-gradient(135deg, #e8f3ff 0%, #f0f7ff 100%); padding: 18px; border-radius: 10px; border: 1px solid #d0e6ff; margin-bottom: 20px; }
.ai-title { color: #1e80ff; font-weight: bold; margin-bottom: 8px; display: flex; align-items: center; gap: 6px; }
.comment-item { display: flex; gap: 16px; padding: 20px 0; border-bottom: 1px solid #f2f3f5; }
.comment-main { flex: 1; }
.comment-user { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px; }
.user-info { display: flex; align-items: center; gap: 10px; }
.tags-row { display: flex; gap: 5px; }
.mini-tag { height: 20px; border-radius: 4px; padding: 0 6px; }
.nickname { font-weight: 600; color: #1d2129; }
.floor-text { display: block; font-size: 12px; font-weight: bold; color: #c0c4cc; }
.time-text { font-size: 11px; color: #86909c; }
.side-bar { width: 300px; flex-shrink: 0; display: flex; flex-direction: column; gap: 20px; }
.publisher-card { border-radius: 12px; text-align: center; padding: 10px; }
.full-btn { width: 100%; height: 48px; font-size: 16px; }
.self-tip { padding: 12px; background: #f2f3f5; color: #86909c; border-radius: 8px; font-size: 14px; border: 1px dashed #e5e6eb; text-align: center; }
.pointer { cursor: pointer; }
</style>