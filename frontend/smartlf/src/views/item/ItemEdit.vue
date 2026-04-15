<template>
  <div class="publish-container" v-loading="pageLoading">
    <el-card class="publish-card" shadow="never">
      <template #header>
        <div class="card-header"><span class="header-title">修改帖子内容</span></div>
      </template>

      <el-form :model="form" label-position="top" class="publish-form">
        <el-row :gutter="40">
          <el-col :md="12">
            <el-form-item label="物品名称">
              <el-input v-model="form.itemName" placeholder="物品名称" size="large" />
            </el-form-item>
            
            <el-form-item label="发生地点" required>
              <div style="display: flex; gap: 10px; width: 100%;">
                <el-cascader
                  v-model="selectedLocationPath"
                  :options="locationOptions"
                  placeholder="选择大区域"
                  style="width: 200px;"
                  size="large"
                  clearable
                />
                <el-input
                  v-model="locationDetail"
                  :placeholder="selectedLocationPath && selectedLocationPath[0] === '其他' ? '请直接填写详细地点...' : '门牌号/自填地点(如: 205)'"
                  size="large"
                  style="flex: 1;"
                />
              </div>
            </el-form-item>

            <el-form-item label="发生时间">
              <el-date-picker v-model="form.eventTime" type="datetime" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:00" style="width: 100%" size="large" />
            </el-form-item>
            <el-form-item label="帖子标题">
              <el-input v-model="form.publicDesc" size="large" />
            </el-form-item>
            <el-form-item label="详情描述">
              <el-input v-model="form.semiPublicDesc" type="textarea" :rows="4" />
            </el-form-item>
          </el-col>

          <el-col :md="12">
            <el-form-item label="物品图片">
              <el-upload action="#" list-type="picture-card" :file-list="fileList" :http-request="handleUpload" :on-remove="handleRemove">
                <el-icon><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            <el-divider content-position="left">隐私与核验修改</el-divider>
            <el-form-item label="核验问题">
              <el-input v-model="form.verifyQuestion" size="large" />
            </el-form-item>
            <el-form-item label="核验暗号">
              <el-input v-model="form.verifyAnswer" size="large" />
            </el-form-item>
            <el-form-item label="私密联系方式">
              <el-input v-model="form.privateContact" size="large" />
            </el-form-item>

            <div class="submit-action">
              <el-button @click="router.back()" size="large">取消</el-button>
              <el-button type="primary" @click="onSubmit" :loading="submitting" size="large" class="publish-btn">提交修改</el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getEditEcho, updateItem, uploadImage } from '@/api/item'

const route = useRoute()
const router = useRouter()
const pageLoading = ref(false)
const submitting = ref(false)

const form = reactive({
  id: null,
  itemName: '',
  eventTime: '',
  location: '',
  publicDesc: '',
  semiPublicDesc: '',
  imagesUrlList: [],
  verifyQuestion: '',
  verifyAnswer: '',
  privateContact: ''
})

const fileList = ref([])

const locationOptions = [
  {
    value: '教学楼', label: '教学楼',
    children: [
      { value: '一号楼', label: '一号楼' }, { value: '二号楼', label: '二号楼' },
      { value: '三号楼', label: '三号楼' }, { value: '四号楼', label: '四号楼' },
      { value: '五号楼', label: '五号楼' }
    ]
  },
  {
    value: '饭堂', label: '饭堂',
    children: [
      { value: '第一饭堂', label: '第一饭堂' }, { value: '第二饭堂', label: '第二饭堂' },
      { value: '第三饭堂', label: '第三饭堂' }, { value: '第四饭堂', label: '第四饭堂' }
    ]
  },
  { value: '图书馆', label: '图书馆' },
  { value: '其他', label: '其他（自填）' }
]

const selectedLocationPath = ref([])
const locationDetail = ref('')

const parseLocationEcho = (locStr) => {
  if (!locStr) return { path: [], detail: '' }
  if (locStr.startsWith('教学楼-')) {
    const parts = locStr.split('-')
    if (parts.length >= 2 && ['一号楼', '二号楼', '三号楼', '四号楼', '五号楼'].includes(parts[1])) {
      return { path: ['教学楼', parts[1]], detail: parts.slice(2).join('-') }
    }
  }
  if (locStr.startsWith('饭堂-')) {
    const parts = locStr.split('-')
    if (parts.length >= 2 && ['第一饭堂', '第二饭堂', '第三饭堂', '第四饭堂'].includes(parts[1])) {
      return { path: ['饭堂', parts[1]], detail: parts.slice(2).join('-') }
    }
  }
  if (locStr.startsWith('图书馆')) {
    const detail = locStr.replace(/^图书馆-?/, '')
    return { path: ['图书馆'], detail: detail }
  }
  return { path: ['其他'], detail: locStr }
}

onMounted(async () => {
  pageLoading.value = true
  const res = await getEditEcho(route.params.id)
  if (res.code === 1 || res.code === 200) {
    Object.assign(form, res.data)
    fileList.value = (res.data.imagesUrlList || []).map(url => ({ url }))

    const parsedLoc = parseLocationEcho(res.data.location)
    selectedLocationPath.value = parsedLoc.path
    locationDetail.value = parsedLoc.detail
    
    form.verifyQuestion = res.data.checkQuestion || ''
    form.verifyAnswer = res.data.checkAnswer || ''
    form.privateContact = res.data.contactInfo || ''
  }
  pageLoading.value = false
})

const handleUpload = async (options) => {
  const res = await uploadImage(options.file)
  if (res.code === 1 || res.code === 200) {
    form.imagesUrlList.push(res.data)
    ElMessage.success('图片上传成功')
  }
}

const handleRemove = (file) => {
  const url = file.url || file.response?.data
  form.imagesUrlList = form.imagesUrlList.filter(u => u !== url)
}

const onSubmit = async () => {
  let base = (selectedLocationPath.value && selectedLocationPath.value.length) ? selectedLocationPath.value.join('-') : ''
  if (base === '其他') {
    form.location = locationDetail.value || '' 
  } else if (base) {
    form.location = locationDetail.value ? `${base}-${locationDetail.value}` : base
  } else {
    form.location = locationDetail.value || '' 
  }

  if (!form.publicDesc || !form.itemName || !form.location) {
    return ElMessage.warning('请填写标题、物品名称及发生地点')
  }

  if ((form.verifyQuestion || form.verifyAnswer || form.privateContact) && 
      !(form.verifyQuestion && form.verifyAnswer)) {
      return ElMessage.warning('设置隐私保护时，【核验问题】和【核验答案】不可为空！')
  }

  submitting.value = true
  try {
    const res = await updateItem(form)
    if (res.code === 1 || res.code === 200) {
      ElMessage.success('修改成功！后台正在同步更新描述信息。')
      router.push(`/item/${form.id}`)
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.publish-container { width: 100%; }
.publish-card { width: 100%; border-radius: 12px; }
.header-title { font-size: 20px; font-weight: 600; }
.publish-form { padding: 10px 20px; }
.submit-action { margin-top: 40px; display: flex; justify-content: flex-end; gap: 20px; }
.publish-btn { width: 160px; }
:deep(.el-form-item__label) { font-weight: 500; color: #606266; padding-bottom: 4px; }
</style>