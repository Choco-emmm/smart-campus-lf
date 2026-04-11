<template>
  <div class="detail-layout" v-loading="loading">
    <template v-if="detail">
      <div class="main-content">
        <el-card class="post-card" shadow="never">
          <div class="post-header">
            <div class="header-top">
              <div class="tags">
                <el-tag :type="detail.type === 0 ? 'danger' : 'success'" effect="dark">
                  {{ detail.type === 0 ? '寻物' : '招领' }}
                </el-tag>
                <el-tag v-if="Number(detail.role) === 1" type="warning" effect="dark" class="ml-10">官方发布</el-tag>
                <el-tag v-if="detail.isTop" type="warning" effect="plain" class="ml-10">
                  <el-icon><Top /></el-icon> 本帖置顶中
                </el-tag>
              </div>
              <el-button v-if="myUserId && String(myUserId) !== String(detail.userId)" type="danger" link @click="reportDialogVisible = true">
                <el-icon><Warning /></el-icon> 举报违规
              </el-button>
            </div>
            
            <h1 class="post-title">{{ detail.publicDesc }}</h1>
            
            <div class="post-meta">
              <span class="nickname pointer" @click="goToProfile(detail.userId)">{{ detail.publisherNickname }}</span>
              <span class="dot">·</span>
              <span>发布于 {{ formatTime(detail.createTime) }}</span>
            </div>
          </div>
          
          <el-divider border-style="dashed" />
          
          <div class="info-bar">
            <div class="info-item"><el-icon><PriceTag /></el-icon> 物品：{{ detail.itemName }}</div>
            <div class="info-item"><el-icon><Location /></el-icon> 地点：{{ detail.location }}</div>
          </div>
          
          <div class="desc-content">{{ detail.semiPublicDesc || '楼主暂无详细描述' }}</div>
          
          <div v-if="detail.aiGeneratedDesc" class="ai-box">
            <div class="ai-title"><el-icon><MagicStick /></el-icon> AI 智能辅助描述</div>
            <div class="ai-content">{{ detail.aiGeneratedDesc }}</div>
          </div>
          
          <div class="image-gallery" v-if="detail.imagesUrlList?.length">
            <el-carousel trigger="click" height="400px" :autoplay="false">
              <el-carousel-item v-for="img in detail.imagesUrlList" :key="img">
                <el-image :src="getImageUrl(img)" fit="contain" class="full-img" :preview-src-list="detail.imagesUrlList.map(i => getImageUrl(i))" />
              </el-carousel-item>
            </el-carousel>
          </div>
        </el-card>

        <el-card class="comment-card" shadow="never">
          <template #header>全部留言 ({{ commentList.length }})</template>
          <div class="comment-input-area">
            <el-input v-model="newComment" type="textarea" :rows="3" placeholder="提供线索或交流..." resize="none" />
            <div class="comment-btn-wrapper">
              <el-button type="primary" @click="submitComment">发表留言</el-button>
            </div>
          </div>
          <div class="comment-list">
            <el-empty v-if="commentList.length === 0" description="暂无留言" :image-size="60" />
            <div class="comment-item" v-for="(comment, index) in commentList" :key="comment.id">
              <el-avatar :size="40" :src="getImageUrl(comment.avatarUrl)" @click="goToProfile(comment.userId)" class="pointer" />
              <div class="comment-main">
                <div class="comment-header">
                  <span class="comment-user">{{ comment.nickname }}</span>
                  <span class="modern-floor">#{{ index + 1 }}</span>
                </div>
                <div class="comment-text">{{ comment.content }}</div>
                <div class="comment-time">{{ formatTimeSmall(comment.createTime) }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <div class="side-bar">
        <el-card class="publisher-card" shadow="never">
          <div class="publisher-info">
            <el-avatar :size="64" :src="getImageUrl(detail.avatarUrl)" @click="goToProfile(detail.userId)" class="pointer" />
            <h3 @click="goToProfile(detail.userId)" class="pointer">
              {{ detail.publisherNickname }}
              <el-icon v-if="Number(detail.role) === 1" color="#E6A23C" style="margin-left: 4px;"><CircleCheckFilled /></el-icon>
            </h3>
            <p class="publisher-label">{{ Number(detail.role) === 1 ? '官方管理员' : '普通发布者' }}</p>
          </div>
          <el-divider />
          
          <div class="action-group">
            <div v-if="myUserId && String(myUserId) === String(detail.userId)" class="owner-buttons">
              <el-button type="primary" class="full-btn" @click="router.push(`/item/edit/${detail.id}`)">修改帖子</el-button>
              
              <el-button v-if="!detail.isTop" type="warning" class="full-btn" @click="topDialogVisible = true">
                <el-icon><Top /></el-icon> 申请信息置顶
              </el-button>
              
              <el-button type="danger" plain class="full-btn" @click="handleDelete">删除帖子</el-button>
            </div>
            
            <el-button v-else type="primary" class="full-btn" @click="handleContact">私聊联系</el-button>
          </div>
        </el-card>
      </div>
    </template>
    
    <el-empty v-else-if="!loading" description="内容不存在" />

    <el-dialog v-model="topDialogVisible" title="申请信息置顶" width="400px" destroy-on-close>
      <div class="dialog-tip">管理员审核通过后，您的帖子将展示在首页最上方。</div>
      <el-form :model="topForm" label-position="top">
        <el-form-item label="申请理由" required>
          <el-input v-model="topForm.applyReason" type="textarea" placeholder="请说明理由（如：非常紧急等）..." :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="topDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="topSubmitting" @click="handleApplyTop">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { PriceTag, Location, Warning, Top, CircleCheckFilled, MagicStick } from '@element-plus/icons-vue'
import { getItemDetail, deleteItem, reportItem, applyItemTop } from '@/api/item'
import { getCommentList, addComment } from '@/api/interact'
import { getUserInfo } from '@/api/user'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref(null)
const myUserId = ref(null)
const commentList = ref([])
const newComment = ref('')

const topDialogVisible = ref(false)
const topSubmitting = ref(false)
const topForm = reactive({ applyReason: '' })

const getImageUrl = (url) => {
  if (!url) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  if (url.startsWith('http')) return url
  return `http://localhost:8080${url}`
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getItemDetail(route.params.id)
    detail.value = res.data
    
    try {
      const userRes = await getUserInfo()
      myUserId.value = userRes.data.id
      console.log('调试 - 匹配状态:', String(myUserId.value) === String(detail.value.userId))
    } catch (e) { console.warn('未登录') }
    
    fetchComments()
  } catch (e) {
    ElMessage.error('帖子加载失败')
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  const res = await getCommentList(route.params.id)
  commentList.value = res.data || []
}

const handleApplyTop = async () => {
  if (!topForm.applyReason.trim()) return ElMessage.warning('请填写理由')
  topSubmitting.value = true
  try {
    await applyItemTop({ itemId: detail.value.id, applyReason: topForm.applyReason })
    ElMessage.success('申请成功')
    topDialogVisible.value = false
  } finally { topSubmitting.value = false }
}

const submitComment = async () => {
  if (!newComment.value.trim()) return
  await addComment({ itemId: route.params.id, content: newComment.value })
  newComment.value = ''
  fetchComments()
}

const handleDelete = () => {
  ElMessageBox.confirm('确定删除此贴？', '警告').then(async () => {
    await deleteItem(detail.value.id)
    router.replace('/')
  })
}

const goToProfile = (id) => router.push(`/profile/${id}`)
const handleContact = () => {
  if (!detail.value) return
  router.push({ path: '/message', query: { targetId: detail.value.userId, targetName: detail.value.publisherNickname } })
}

const formatTime = (t) => t ? t.replace('T', ' ').substring(0, 16) : ''
const formatTimeSmall = (t) => t ? t.replace('T', ' ').substring(5, 16) : ''

onMounted(() => fetchData())
</script>

<style scoped>
.detail-layout { display: flex; gap: 20px; max-width: 1200px; margin: 0 auto; padding: 20px; }
.main-content { flex: 1; min-width: 0; }
.side-bar { width: 300px; position: sticky; top: 20px; }
.post-card, .comment-card { border-radius: 12px; margin-bottom: 20px; border: none; box-shadow: 0 2px 12px rgba(0,0,0,0.04); }

.post-title { font-size: 26px; margin: 15px 0; color: #1d2129; line-height: 1.4; }
.info-bar { display: flex; gap: 24px; padding: 16px; background: #f7f8fa; border-radius: 8px; margin: 20px 0; }
.desc-content { font-size: 16px; line-height: 1.8; color: #4e5969; margin: 20px 0; white-space: pre-wrap; min-height: 80px; }

/* AI 辅助描述展示样式 */
.ai-box { background: #f0f7ff; border-radius: 8px; padding: 15px; border-left: 4px solid #409eff; margin: 20px 0; }
.ai-title { color: #409eff; font-weight: bold; margin-bottom: 8px; display: flex; align-items: center; gap: 5px; }
.ai-content { font-size: 14px; color: #606266; line-height: 1.6; }

/* 🌟 发表留言按钮间距修复 */
.comment-input-area { margin-bottom: 20px; }
.comment-btn-wrapper { margin-top: 15px; display: flex; justify-content: flex-end; }

.comment-item { display: flex; gap: 15px; padding: 15px 0; border-bottom: 1px solid #f2f3f5; }
.comment-main { flex: 1; }
.comment-header { display: flex; justify-content: space-between; align-items: center; }
.comment-user { font-weight: bold; font-size: 14px; color: #1d2129; }
.modern-floor { color: #c0c4cc; font-size: 12px; }

.publisher-info { text-align: center; padding: 10px 0; }
.publisher-label { color: #86909c; font-size: 12px; margin-top: 5px; }

/* 🌟 侧边栏按钮间距修复 */
.owner-buttons { display: flex; flex-direction: column; gap: 12px; }
.full-btn { width: 100%; margin: 0 !important; height: 40px; }

.dialog-tip { margin-bottom: 15px; color: #909399; font-size: 13px; }
.ml-10 { margin-left: 10px; }
.pointer { cursor: pointer; }
</style>