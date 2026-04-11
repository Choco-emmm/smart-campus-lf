<template>
  <div class="detail-layout" v-loading="loading">
    <template v-if="detail">
      <div class="main-content">
        <el-card class="post-card" shadow="never">
          <div class="post-header">
            <div class="header-top">
              <div class="tags">
                <el-tag :type="detail.type === 0 ? 'danger' : 'success'" effect="dark">{{ detail.type === 0 ? '寻物' : '招领' }}</el-tag>
                <el-tag :type="getStatusType(detail.status)" class="ml-10">{{ getStatusText(detail.status) }}</el-tag>
                <el-tag v-if="Number(detail.role) === 1" type="warning" effect="dark" class="ml-10">官方</el-tag>
                <el-tag v-if="detail.isTop" type="warning" effect="plain" class="ml-10"><el-icon><Top /></el-icon> 置顶中</el-tag>
              </div>
              <el-button v-if="myUserId && String(myUserId) !== String(detail.userId)" type="danger" link @click="reportDialogVisible = true">
                <el-icon><Warning /></el-icon> 举报
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
          <div class="image-gallery" v-if="detail.imagesUrlList?.length">
            <el-carousel trigger="click" height="400px" :autoplay="false">
              <el-carousel-item v-for="img in detail.imagesUrlList" :key="img">
                <el-image :src="getImageUrl(img)" fit="contain" class="full-img" :preview-src-list="detail.imagesUrlList.map(i => getImageUrl(i))" />
              </el-carousel-item>
            </el-carousel>
          </div>
        </el-card>

        <el-card class="comment-card" shadow="never" style="margin-top: 20px;">
          <template #header>全部留言 ({{ commentList.length }})</template>
          <div class="comment-input-area">
            <el-input v-model="newComment" type="textarea" :rows="3" placeholder="提供线索或交流..." resize="none" />
            <div style="text-align: right; margin-top: 10px;">
              <el-button type="primary" @click="submitComment">发表留言</el-button>
            </div>
          </div>
          <div class="comment-list">
            <div class="comment-item" v-for="(comment, index) in commentList" :key="comment.id" style="display: flex; gap: 15px; padding: 15px 0; border-bottom: 1px solid #f2f3f5;">
              <el-avatar :size="40" :src="getImageUrl(comment.avatarUrl)" @click="goToProfile(comment.userId)" class="pointer" />
              <div style="flex: 1;">
                <div style="display: flex; justify-content: space-between;">
                  <span style="font-weight: bold;">{{ comment.nickname }}</span>
                  <span style="color: #c0c4cc;">#{{ index + 1 }}</span>
                </div>
                <div style="margin-top: 5px;">{{ comment.content }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <div class="side-bar">
        <el-card class="publisher-card" shadow="never">
          <div class="publisher-info" style="text-align: center;">
            <el-avatar :size="64" :src="getImageUrl(detail.publisherAvatarUrl)" class="pointer" @click="goToProfile(detail.userId)" />
            <h3 class="mt-10">{{ detail.publisherNickname }}</h3>
          </div>
          <el-divider />
          <div class="action-group">
            <div v-if="myUserId && String(myUserId) === String(detail.userId)" class="owner-actions">
              <div class="status-manage">
                <p class="section-label">更改状态：</p>
                <el-select v-model="detail.status" @change="handleStatusChange" style="width: 100%">
                  <el-option :value="0" label="寻找中" />
                  <el-option :value="1" label="锁定中" />
                  <el-option :value="2" label="已结案" />
                </el-select>
              </div>
              <el-divider />
              <el-button type="primary" class="full-btn" @click="router.push(`/item/edit/${detail.id}`)">修改帖子</el-button>
              <el-button v-if="!detail.isTop" type="warning" class="full-btn" @click="handleTopApply">申请置顶</el-button>
              <el-button type="danger" plain class="full-btn" @click="handleDelete">删除此帖</el-button>
            </div>
            <el-button v-else type="primary" class="full-btn" @click="handleContact">私聊联系</el-button>
          </div>
        </el-card>
      </div>
    </template>

    <el-dialog v-model="reportDialogVisible" title="举报违规内容" width="450px" destroy-on-close>
      <div style="margin-bottom: 15px;">请选择举报原因：</div>
      <el-checkbox-group v-model="selectedReasons" style="display: flex; flex-direction: column; gap: 10px;">
        <el-checkbox label="虚假欺诈信息" />
        <el-checkbox label="人身攻击/引战" />
        <el-checkbox label="与失物拾物无关" />
        <el-checkbox label="其他原因" />
      </el-checkbox-group>
      <el-input v-if="selectedReasons.includes('其他原因')" v-model="otherReasonText" type="textarea" :rows="3" placeholder="请补充说明..." style="margin-top: 15px;" />
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReport">确认举报</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { PriceTag, Location, Warning, Top } from '@element-plus/icons-vue'
import { getItemDetail, deleteItem, reportItem, updateItemStatus, applyItemTop } from '@/api/item'
import { getCommentList, addComment } from '@/api/interact'
import { getUserInfo } from '@/api/user'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref(null)
const myUserId = ref(null)
const commentList = ref([])
const newComment = ref('')

const reportDialogVisible = ref(false)
const selectedReasons = ref([])
const otherReasonText = ref('')

const getStatusText = (s) => ({ 0: '寻找中', 1: '锁定中', 2: '已结案' }[s] || '未知')
const getStatusType = (s) => s === 1 ? 'warning' : (s === 2 ? 'info' : 'primary')

const getImageUrl = (url) => {
  if (!url) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  return url.startsWith('http') ? url : `http://localhost:8080${url}`
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getItemDetail(route.params.id)
    detail.value = res.data
    const userRes = await getUserInfo()
    myUserId.value = userRes.data.id
    const commentRes = await getCommentList(route.params.id)
    commentList.value = commentRes.data || []
  } finally { loading.value = false }
}

const submitComment = async () => {
  if (!newComment.value.trim()) return
  await addComment({ itemId: route.params.id, content: newComment.value })
  newComment.value = ''
  fetchData()
}

const handleStatusChange = async (val) => {
  await updateItemStatus({ id: detail.value.id, status: val })
  ElMessage.success('状态已更新')
}

const confirmReport = async () => {
  if (selectedReasons.value.length === 0) return ElMessage.warning('请选择原因')
  let reason = selectedReasons.value.filter(r => r !== '其他原因').join('; ')
  if (selectedReasons.value.includes('其他原因')) reason += `; 其他: ${otherReasonText.value}`
  
  // 🌟 对接后端 DTO：itemId 和 reason
  await reportItem({ itemId: Number(route.params.id), reason: reason })
  ElMessage.success('举报成功')
  reportDialogVisible.value = false
}

const handleDelete = () => {
  ElMessageBox.confirm('确定删除吗？', '提示').then(async () => {
    await deleteItem(detail.value.id)
    router.replace('/')
  })
}

const handleTopApply = async () => {
    await applyItemTop({ itemId: detail.value.id })
    ElMessage.success('申请已提交，请等待审核')
}

const goToProfile = (id) => router.push(`/profile/${id}`)
const handleContact = () => router.push({ path: '/message', query: { targetId: detail.value.userId, targetName: detail.value.publisherNickname } })
const formatTime = (t) => t ? t.replace('T', ' ').substring(0, 16) : ''
const formatTimeSmall = (t) => t ? t.replace('T', ' ').substring(5, 16) : ''

onMounted(() => fetchData())
</script>

<style scoped>
/* 保持原有样式，仅补全布局 */
.detail-layout { display: flex; gap: 20px; max-width: 1200px; margin: 0 auto; padding: 20px; align-items: flex-start; }
.main-content { flex: 1; min-width: 0; }
.side-bar { width: 300px; position: sticky; top: 20px; }
.full-btn { width: 100%; margin-top: 10px; margin-left: 0 !important; }
.post-title { font-size: 26px; margin: 15px 0; color: #1d2129; }
.info-bar { display: flex; gap: 24px; padding: 16px; background: #f7f8fa; border-radius: 8px; }
.desc-content { font-size: 16px; line-height: 1.8; color: #4e5969; margin: 20px 0; white-space: pre-wrap; }
.pointer { cursor: pointer; }
.mt-10 { margin-top: 10px; }
.ml-10 { margin-left: 10px; }
</style>