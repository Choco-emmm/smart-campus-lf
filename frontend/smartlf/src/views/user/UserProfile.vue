<template>
  <div class="profile-container" v-loading="loading">
    <el-card class="profile-card" shadow="never" v-if="profile">
      <div class="cover-bg"></div>
      
      <div class="profile-header">
        <el-avatar :size="100" :src="getImageUrl(profile.avatarUrl)" class="user-avatar" />
        <div class="user-main-info">
          <h2 class="nickname">
            {{ profile.nickname }}
            <el-tag size="small" type="warning" effect="dark" v-if="Number(profile.role) === 1" class="ml-10">管理员</el-tag>
          </h2>
          <div class="meta-tags">
            <el-tag type="info" size="small"><el-icon><Calendar /></el-icon> 加入于 {{ formatTime(profile.createTime) }}</el-tag>
          </div>
        </div>
        
        <div class="action-area">
          <el-button v-if="profile.id !== myUserId" type="primary" @click="handleChat">发送私信</el-button>
          
          <template v-else>
            <el-button type="danger" plain @click="pwdDialogVisible = true">修改密码</el-button>
            <el-button type="primary" plain @click="openEditDialog">
              <el-icon><Edit /></el-icon> 编辑资料
            </el-button>
          </template>
        </div>
      </div>
      
      <el-divider style="margin-bottom: 0;" />
      
      <div class="user-content-area">
        <el-tabs v-model="activeTab" class="profile-tabs">
          <el-tab-pane :label="profile.id === myUserId ? '我的发布' : 'Ta的发布'" name="posts">
            <div class="post-list" v-loading="listLoading">
              <el-empty v-if="itemList.length === 0" description="暂无发布记录" />
              <div v-for="item in itemList" :key="item.id" class="post-item" @click="router.push(`/item/${item.id}`)">
                <div class="post-content">
                  <div class="post-meta">
                    <el-tag :type="item.type === 0 ? 'danger' : 'success'" size="small" effect="dark">
                      {{ item.type === 0 ? '丢失' : '拾取' }}
                    </el-tag>
                    <span class="meta-time">{{ formatTime(item.createTime) }}</span>
                  </div>
                  <h3 class="post-title">{{ item.publicDesc }}</h3>
                  <div class="post-info">
                    <span><el-icon><PriceTag /></el-icon> {{ item.itemName }}</span>
                    <span><el-icon><Location /></el-icon> {{ item.location }}</span>
                  </div>
                </div>
                <div class="post-thumb" v-if="item.coverImage">
                  <el-image :src="item.coverImage" fit="cover" class="thumb-img" />
                </div>
              </div>
            </div>

            <div class="pagination-wrapper" v-if="total > 0">
              <el-pagination v-model:current-page="pageParams.page" :total="total" background layout="prev, pager, next" @current-change="fetchUserPosts" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>

    <el-dialog v-model="editDialogVisible" title="修改个人资料" width="450px" destroy-on-close>
      <el-form :model="editForm" label-width="80px" size="large">
        
        <el-form-item label="用户头像">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :http-request="handleAvatarUpload"
          >
            <img v-if="editForm.avatarUrl" :src="getImageUrl(editForm.avatarUrl)" class="avatar-preview" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="用户昵称">
          <el-input v-model="editForm.nickname" placeholder="给别人留下个好印象" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="11位手机号" clearable />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="常用联系邮箱" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateUser" :loading="saving">保存修改</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="pwdDialogVisible" title="修改密码" width="400px" destroy-on-close>
      <el-form :model="pwdForm" label-position="top" size="large">
        <el-form-item label="原密码">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="新密码 (需包含字母和数字)" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleUpdatePwd" :loading="saving">确认修改并重新登录</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted, watch, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Calendar, PriceTag, Location, Edit, Plus } from '@element-plus/icons-vue' // 🌟 引入了 Plus 图标
import { ElMessage } from 'element-plus'
import { getUserProfile, getUserInfo, updateUserInfo, updatePassword } from '@/api/user'
import { getItemPage, uploadImage, getMyPublishPage } from '@/api/item'
const route = useRoute()
const router = useRouter()
const loading = ref(false)
const profile = ref(null)
const myUserId = ref(null)
const activeTab = ref('posts')
const saving = ref(false)

const listLoading = ref(false)
const itemList = ref([])
const total = ref(0)
const pageParams = ref({ page: 1, pageSize: 10, userId: null })

const editDialogVisible = ref(false)
const editForm = reactive({ id: null, nickname: '', avatarUrl: '', phone: '', email: '' })

const pwdDialogVisible = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '' })
const getImageUrl = (url) => {
  if (!url) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  if (url.startsWith('http')) return url
  return `http://localhost:8080${url}` // 拼上你的后端完整地址
}
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUserProfile(route.params.id)
    profile.value = res.data

    try {
      const userRes = await getUserInfo()
      myUserId.value = userRes.data.id
    } catch (e) {}

    pageParams.value.userId = route.params.id
    await fetchUserPosts()
  } finally {
    loading.value = false
  }
}

const fetchUserPosts = async () => {
  listLoading.value = true
  try {
    let res;
    // 🌟 核心逻辑：判断是看自己还是看别人
    if (Number(route.params.id) === myUserId.value) {
      // 调用你的专用 /my-page 接口
      res = await getMyPublishPage(pageParams.value)
    } else {
      // 调用广场分页接口 + userId 过滤
      res = await getItemPage({
        ...pageParams.value,
        userId: route.params.id
      })
    }
    itemList.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    listLoading.value = false
  }
}

const openEditDialog = async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 1 || res.code === 200) {
      editForm.id = res.data.id
      editForm.nickname = res.data.nickname
      editForm.avatarUrl = res.data.avatarUrl
      editForm.phone = res.data.phone
      editForm.email = res.data.email
      editDialogVisible.value = true
    }
  } catch (error) {}
}

// 🌟 复用帖子发图 API 上传头像
const handleAvatarUpload = async (options) => {
  try {
    const res = await uploadImage(options.file)
    if (res.code === 1 || res.code === 200) {
      editForm.avatarUrl = res.data // 把后端返回的图片链接直接赋给表单
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(res.msg || '上传失败')
    }
  } catch (error) {
    // 错误已被拦截器处理
  }
}

const handleUpdateUser = async () => {
  if (!editForm.nickname.trim()) return ElMessage.warning('昵称不能为空')
  saving.value = true
  try {
    const res = await updateUserInfo(editForm)
    if (res.code === 1 || res.code === 200) {
      ElMessage.success('修改成功')
      editDialogVisible.value = false
      fetchData() 
      window.location.reload() 
    }
  } finally {
    saving.value = false
  }
}

const handleUpdatePwd = async () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) return ElMessage.warning('请填写完整密码')
  saving.value = true
  try {
    const res = await updatePassword(pwdForm)
    if (res.code === 1 || res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      localStorage.removeItem('token')
      pwdDialogVisible.value = false
      router.push('/login')
    }
  } finally {
    saving.value = false
  }
}

const handleChat = () => router.push({ path: '/message', query: { targetId: profile.value.id, targetName: profile.value.nickname, targetAvatar: profile.value.avatarUrl } })
const formatTime = (t) => t ? t.split('T')[0] : ''
const goToDetail = (id) => router.push(`/item/${id}`)

watch(() => route.params.id, (newId) => { if (newId) fetchData() })
onMounted(() => fetchData())
</script>

<style scoped>
.profile-container { max-width: 900px; margin: 0 auto; }
.profile-card { border-radius: 12px; overflow: hidden; position: relative; border: none; box-shadow: 0 2px 12px rgba(0,0,0,0.04); }
.profile-card :deep(.el-card__body) { padding: 0; }
.cover-bg { height: 150px; background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%); }
.profile-header { padding: 0 30px 20px; display: flex; align-items: flex-end; margin-top: -50px; }
.user-avatar { border: 4px solid #fff; background-color: #fff; flex-shrink: 0; }
.user-main-info { margin-left: 20px; flex: 1; padding-bottom: 5px; }
.nickname { margin: 0 0 10px 0; font-size: 24px; color: #1d2129; display: flex; align-items: center; }
.ml-10 { margin-left: 10px; }
.meta-tags { display: flex; gap: 10px; }
.action-area { padding-bottom: 5px; display: flex; gap: 10px;}
.user-content-area { padding: 0 30px 30px; background-color: #fafafa; min-height: 400px; }
.post-list { display: flex; flex-direction: column; gap: 15px; }
.post-item { display: flex; justify-content: space-between; padding: 20px; background: #fff; border-radius: 8px; border: 1px solid #f0f0f0; cursor: pointer; transition: all 0.2s; }
.post-item:hover { border-color: #e4e6eb; box-shadow: 0 4px 10px rgba(0,0,0,0.05); transform: translateY(-2px); }
.post-content { flex: 1; }
.post-meta { display: flex; align-items: center; margin-bottom: 10px; font-size: 13px; }
.meta-time { margin-left: 15px; color: #86909c; }
.post-title { margin: 0 0 10px 0; font-size: 16px; color: #1d2129; }
.post-info { font-size: 13px; color: #86909c; display: flex; gap: 15px; }
.post-thumb { margin-left: 20px; width: 120px; height: 80px; border-radius: 4px; overflow: hidden; }
.thumb-img { width: 100%; height: 100%; }
.pagination-wrapper { padding: 20px 0 0; display: flex; justify-content: center; }

/* 🌟 头像上传组件样式 */
.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}
.avatar-preview {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}
</style>