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
            <el-tag size="small" type="danger" effect="dark" v-if="profile.status === 1" class="ml-10">已封禁</el-tag>
          </h2>
          <div class="meta-tags">
            <el-tag type="info" size="small"><el-icon><Calendar /></el-icon> 加入于 {{ formatTime(profile.createTime) }}</el-tag>
          </div>
        </div>
        
        <div class="action-area">
          <template v-if="profile.id !== myUserId">
            <el-button type="primary" @click="handleChat">发送私信</el-button>
            <el-button 
              v-if="myRole === 1 && profile.role !== 1" 
              :type="profile.status === 1 ? 'success' : 'danger'" 
              plain 
              @click="handleStatusChange"
            >
              {{ profile.status === 1 ? '解除封禁' : '封禁该账号' }}
            </el-button>
          </template>
          
          <template v-else>
            <el-button type="danger" plain @click="openPwdDialog">修改密码</el-button>
            <el-button type="primary" plain @click="openEditDialog"><el-icon><Edit /></el-icon> 编辑资料</el-button>
          </template>
        </div>
      </div>
      
      <el-divider style="margin-bottom: 0;" />
      
      <div class="user-content-area">
        <div class="section-title"><h3>动态列表</h3></div>
        <div class="post-list" v-loading="listLoading">
          <el-empty v-if="itemList.length === 0" description="暂无发布记录" />
          <div v-for="item in itemList" :key="item.id" class="post-item" @click="goToDetail(item.id)">
            <div class="post-content">
              <div class="post-meta">
                <el-tag :type="item.type === 0 ? 'danger' : 'success'" size="small" effect="dark">{{ item.type === 0 ? '丢失' : '拾取' }}</el-tag>
                <el-tag :type="getStatusType(item.status)" size="small" class="ml-10">{{ getStatusText(item.status) }}</el-tag>
                <span class="meta-time">{{ formatTime(item.createTime) }}</span>
              </div>
              <h3 class="post-title">{{ item.publicDesc }}</h3>
              <div class="post-info"><span><el-icon><Location /></el-icon> {{ item.location }}</span></div>
            </div>
            <div class="post-thumb" v-if="item.coverImage">
              <el-image :src="getImageUrl(item.coverImage)" fit="cover" class="thumb-img" />
            </div>
          </div>
        </div>
        <div class="pagination-wrapper" v-if="total > 0">
          <el-pagination v-model:current-page="pageParams.page" :total="total" background layout="prev, pager, next" @current-change="fetchUserPosts" />
        </div>
      </div>
    </el-card>

    <el-dialog v-model="editDialogVisible" title="修改资料" width="450px" destroy-on-close>
      <el-form :model="editForm" label-width="80px" size="large">
        <el-form-item label="用户头像">
          <el-upload class="avatar-uploader" action="#" :show-file-list="false" :http-request="handleAvatarUpload">
            <img v-if="editForm.avatarUrl" :src="getImageUrl(editForm.avatarUrl)" class="avatar-preview" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="昵称"><el-input v-model="editForm.nickname" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="editForm.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="editForm.email" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateUser" :loading="saving">保存修改</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="pwdDialogVisible" title="修改安全密码" width="450px" @close="resetPwdForm">
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="90px" size="large">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入当前旧密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码 (6-20位)" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次确认新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleUpdatePassword" :loading="pwdSaving">确认修改</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Calendar, Location, Edit, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserProfile, getUserInfo, updateUserInfo, checkSamePassword, updatePassword } from '@/api/user'
import { getOthersPublishPage, uploadImage, getMyPublishPage } from '@/api/item'
import { updateUserStatus } from '@/api/admin'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const profile = ref(null)
const myUserId = ref(null)
const myRole = ref(0) 
const itemList = ref([])
const total = ref(0)
const editDialogVisible = ref(false)
const editForm = reactive({ nickname: '', avatarUrl: '', phone: '', email: '' })
const listLoading = ref(false)
const pageParams = ref({ page: 1, pageSize: 10 })
const saving = ref(false)

// ======== 🌟 密码修改相关逻辑 ========
const pwdDialogVisible = ref(false)
const pwdSaving = ref(false)
const pwdFormRef = ref(null)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 旧密码的异步失焦校验逻辑
const validateOldPwd = async (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请输入当前密码'))
  }
  try {
    const res = await checkSamePassword(value)
    if (res.data === true) {
      callback() 
    } else {
      callback(new Error('当前密码错误，请重新输入'))
    }
  } catch (error) {
    callback(new Error('校验失败，请检查网络'))
  }
}

// 🌟 新密码校验逻辑：判断不能与旧密码相同
const validateNewPwd = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度需在 6 到 20 个字符'))
  } else if (value === pwdForm.oldPassword) {
    // 👇 前端直接判断，不需要经过后端
    callback(new Error('新密码不能与当前密码相同！'))
  } else {
    // 如果确认密码已经填了，需要触发一下确认密码的再次校验
    if (pwdForm.confirmPassword !== '') {
      pwdFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

// 确认密码校验逻辑
const validateConfirmPwd = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的新密码不一致!'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [
    { required: true, validator: validateOldPwd, trigger: 'blur' }
  ],
  newPassword: [
    // 🌟 应用新的自定义校验规则
    { required: true, validator: validateNewPwd, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPwd, trigger: 'blur' }
  ]
}

const openPwdDialog = () => {
  pwdDialogVisible.value = true
}

const resetPwdForm = () => {
  if (pwdFormRef.value) {
    pwdFormRef.value.resetFields()
  }
}

const handleUpdatePassword = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (valid) {
      pwdSaving.value = true
      try {
        const res = await updatePassword({
          oldPassword: pwdForm.oldPassword,
          newPassword: pwdForm.newPassword
        })
        if (res.code === 1 || res.code === 200) {
          ElMessage.success('密码修改成功，请使用新密码重新登录！')
          pwdDialogVisible.value = false
          localStorage.removeItem('token')
          if (window.globalWs) window.globalWs.close()
          router.replace('/login')
        }
      } finally {
        pwdSaving.value = false
      }
    }
  })
}
// ======== 🌟 密码修改相关逻辑结束 ========

const getStatusText = (s) => ({ 0: '寻找中', 1: '锁定中', 2: '已结案', 3: '已下架' }[s] || '未知')
const getStatusType = (s) => s === 1 ? 'warning' : (s === 2 ? 'info' : (s === 3 ? 'danger' : 'primary'))

const getImageUrl = (url) => {
  if (!url) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  if (url.startsWith('http')) return url
  return `http://localhost:8080${url}`
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUserProfile(route.params.id)
    profile.value = res.data
    const userRes = await getUserInfo()
    myUserId.value = userRes.data.id
    myRole.value = Number(userRes.data.role)
    fetchUserPosts()
  } finally { loading.value = false }
}

const fetchUserPosts = async () => {
  listLoading.value = true
  try {
    let res = (Number(route.params.id) === myUserId.value) 
      ? await getMyPublishPage({ page: pageParams.value.page, pageSize: 10 }) 
      : await getOthersPublishPage(route.params.id, { page: pageParams.value.page, pageSize: 10 }) 
      
    itemList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error("获取帖子失败", e)
  } finally {
    listLoading.value = false
  }
}

const handleStatusChange = () => {
  const isBanning = profile.value.status === 0
  const actionText = isBanning ? '封禁' : '解封'
  const targetStatus = isBanning ? 1 : 0

  ElMessageBox.confirm(`确定要${actionText}用户【${profile.value.nickname}】吗？`, '高危操作', { type: 'warning' }).then(async () => {
    await updateUserStatus(profile.value.id, targetStatus)
    ElMessage.success(`已${actionText}`)
    fetchData() 
  }).catch(() => {})
}

const openEditDialog = async () => {
  const res = await getUserInfo()
  Object.assign(editForm, res.data)
  editDialogVisible.value = true
}

const handleAvatarUpload = async (options) => {
  const res = await uploadImage(options.file)
  if (res.code === 1 || res.code === 200) {
    editForm.avatarUrl = res.data
    ElMessage.success('上传成功')
  }
}

const handleUpdateUser = async () => {
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

const goToDetail = (id) => router.push(`/item/${id}`)
const handleChat = () => router.push({ path: '/message', query: { targetId: profile.value.id, targetName: profile.value.nickname, targetAvatar: profile.value.avatarUrl } })
const formatTime = (t) => t ? t.split('T')[0] : ''
onMounted(() => fetchData())
</script>

<style scoped>
.profile-container { max-width: 900px; margin: 0 auto; }
.profile-card { border-radius: 12px; overflow: hidden; position: relative; border: none; box-shadow: 0 2px 12px rgba(0,0,0,0.04); }
.cover-bg { height: 150px; background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%); }
.profile-header { padding: 0 30px 20px; display: flex; align-items: flex-end; margin-top: -50px; }
.user-avatar { border: 4px solid #fff; background-color: #fff; flex-shrink: 0; }
.user-main-info { margin-left: 20px; flex: 1; padding-bottom: 5px; }
.nickname { margin: 0 0 10px 0; font-size: 24px; color: #1d2129; display: flex; align-items: center; }
.ml-10 { margin-left: 10px; }
.meta-tags { display: flex; gap: 10px; }
.action-area { padding-bottom: 5px; display: flex; gap: 10px; }
.user-content-area { padding: 20px 30px 30px; background-color: #fafafa; min-height: 400px; }
.section-title h3 { margin: 0 0 20px 0; font-size: 18px; color: #1d2129; border-left: 4px solid #1e80ff; padding-left: 10px; }
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
.avatar-uploader :deep(.el-upload) { border: 1px dashed #dcdfe6; border-radius: 6px; cursor: pointer; }
.avatar-uploader-icon { font-size: 28px; color: #8c939d; width: 100px; height: 100px; line-height: 100px; text-align: center; }
.avatar-preview { width: 100px; height: 100px; display: block; object-fit: cover; }
</style>