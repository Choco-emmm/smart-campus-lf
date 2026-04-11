<template>
  <div class="profile-container" v-loading="loading">
    <el-card class="profile-card" shadow="never" v-if="profile">
      <div class="cover-bg"></div>
      <div class="profile-header">
        <el-avatar :size="100" :src="profile.avatarUrl" class="user-avatar" />
        <div class="user-main-info">
          <h2 class="nickname">
            {{ profile.nickname }}
            <el-tag size="small" type="warning" effect="dark" v-if="profile.role === 1" style="margin-left: 10px;">管理员</el-tag>
          </h2>
          <div class="meta-tags">
            <el-tag type="info" size="small"><el-icon><Calendar /></el-icon> 加入于 {{ formatTime(profile.createTime) }}</el-tag>
            <el-tag v-if="profile.status === 1" type="danger" size="small">已封禁</el-tag>
          </div>
        </div>
        <div class="action-area">
          <el-button v-if="profile.id !== myUserId" type="primary" @click="handleChat">发送私信</el-button>
          <el-button v-else type="info" plain @click="ElMessage.info('编辑功能开发中')">编辑资料</el-button>
        </div>
      </div>
      <el-divider />
      <el-empty description="暂无其他动态" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Calendar } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getUserProfile, getUserInfo } from '@/api/user'

const route = useRoute()
const router = useRouter()
const profile = ref(null)
const myUserId = ref(null)
const loading = ref(false)

const fetchData = async () => {
  loading.value = true
  const res = await getUserProfile(route.params.id)
  profile.value = res.data
  const userRes = await getUserInfo()
  myUserId.value = userRes.data.id
  loading.value = false
}

const handleChat = () => {
  router.push({
    path: '/message',
    query: { targetId: profile.value.id, targetName: profile.value.nickname, targetAvatar: profile.value.avatarUrl }
  })
}

const formatTime = (t) => t ? t.split('T')[0] : ''

onMounted(() => fetchData())
</script>

<style scoped>
/* 保持原有样式 */
.profile-container { max-width: 900px; margin: 0 auto; }
.profile-card { border-radius: 12px; overflow: hidden; position: relative; }
.cover-bg { height: 150px; background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%); }
.profile-header { padding: 0 30px 20px; display: flex; align-items: flex-end; margin-top: -50px; }
.user-avatar { border: 4px solid #fff; flex-shrink: 0; }
.user-main-info { margin-left: 20px; flex: 1; padding-bottom: 5px; }
.nickname { margin: 0 0 10px 0; font-size: 24px; color: #1d2129; display: flex; align-items: center; }
.meta-tags { display: flex; gap: 10px; }
</style>