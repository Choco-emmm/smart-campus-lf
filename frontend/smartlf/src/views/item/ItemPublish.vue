<template>
  <div class="publish-container">
    <el-card class="publish-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="header-title">发布失物招领</span>
        </div>
      </template>

      <el-form :model="form" label-position="top" class="publish-form">
        <el-row :gutter="40">
          
          <el-col :xs="24" :md="12">
            <el-form-item label="信息类型">
              <el-radio-group v-model="form.type" size="large">
                <el-radio-button :value="0">我丢了东西 (寻物)</el-radio-button>
                <el-radio-button :value="1">我捡了东西 (招领)</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="物品名称">
              <el-input v-model="form.itemName" placeholder="如：黑色雨伞、校园卡" size="large" />
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
                  :placeholder="selectedLocationPath && selectedLocationPath[0] === '其他' ? '请直接填写详细地点...' : '自填地点(如: 205)'"
                  size="large"
                  style="flex: 1;"
                />
              </div>
            </el-form-item>

            <el-form-item label="发生时间">
              <el-date-picker 
                v-model="form.eventTime" 
                type="datetime" 
                placeholder="选择日期时间" 
                value-format="YYYY-MM-DD HH:mm:ss" 
                style="width: 100%"
                size="large"
              />
            </el-form-item>

            <el-form-item label="帖子标题 (简明扼要)">
              <el-input v-model="form.publicDesc" placeholder="一句话描述，将展示在列表页" size="large" />
            </el-form-item>

            <el-form-item label="详情描述">
              <el-input v-model="form.semiPublicDesc" type="textarea" :rows="4" placeholder="更多细节描述（可选）" />
            </el-form-item>
          </el-col>

          <el-col :xs="24" :md="12">
            <el-form-item label="物品图片 (建议上传以提高找回率)">
              <div class="upload-wrapper">
                <el-upload action="#" list-type="picture-card" :auto-upload="true" :http-request="handleUpload" :on-remove="handleRemove">
                  <el-icon><Plus /></el-icon>
                </el-upload>
              </div>
            </el-form-item>

            <el-divider content-position="left">隐私与核验保护 (选填)</el-divider>

            <el-form-item label="认领核验问题">
              <el-input v-model="form.verifyQuestion" placeholder="如：水杯底部有什么贴纸？" size="large" />
            </el-form-item>

            <el-form-item label="核验预设答案">
              <el-input v-model="form.verifyAnswer" placeholder="预设正确答案，防止冒领" size="large" />
            </el-form-item>

            <el-form-item label="私密联系方式">
              <el-input v-model="form.privateContact" placeholder="仅审核通过后对方可见 (如微信号/手机号)" size="large" />
            </el-form-item>

            <div class="submit-action">
              <el-button @click="$router.back()" size="large">取 消</el-button>
              <el-button type="primary" @click="onSubmit" :loading="submitting" size="large" class="publish-btn">立 即 发 布</el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { uploadImage, publishItem, generateAiDesc } from '@/api/item'

const router = useRouter()
const submitting = ref(false)

const form = reactive({
  type: 0,
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

// 级联选择数据
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

const handleUpload = async (options) => {
  try {
    const res = await uploadImage(options.file)
    if (res.code === 1 || res.code === 200) {
      form.imagesUrlList.push(res.data)
      ElMessage.success('图片上传成功')
    }
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

const handleRemove = (file) => {
  const url = file.response?.data || file.url
  form.imagesUrlList = form.imagesUrlList.filter(u => u !== url)
}

const onSubmit = async () => {
  // 🌟 核心修复：点击发布的瞬间，强制拼装地址！
  let base = (selectedLocationPath.value && selectedLocationPath.value.length) ? selectedLocationPath.value.join('-') : ''
  if (base === '其他') {
    form.location = locationDetail.value || '' // 选其他，只存详情
  } else if (base) {
    form.location = locationDetail.value ? `${base}-${locationDetail.value}` : base // 拼接
  } else {
    form.location = locationDetail.value || '' // 什么都不选，只填了详情
  }

  if (!form.publicDesc || !form.itemName || !form.location) {
    return ElMessage.warning('请填写标题、物品名称及发生地点')
  }
  
  submitting.value = true
  try {
    const res = await publishItem(form)
    if (res.code === 1 || res.code === 200) {
      ElMessage.success('发布成功') 
      if (res.data) {
        generateAiDesc(res.data).catch(err => { console.warn("AI异常:", err) })
      }
      router.push('/')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.publish-container { width: 100%; }
.publish-card { width: 100%; border-radius: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-title { font-size: 20px; font-weight: 600; color: #303133; }
.publish-form { padding: 10px 20px; }
.upload-wrapper { width: 100%; }
.submit-action { margin-top: 40px; display: flex; justify-content: flex-end; gap: 20px; }
.publish-btn { width: 160px; letter-spacing: 2px; }
:deep(.el-form-item__label) { font-weight: 500; color: #606266; padding-bottom: 4px; }
</style>