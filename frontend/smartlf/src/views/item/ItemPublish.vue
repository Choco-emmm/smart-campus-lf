<template>
  <div class="publish-container" v-loading="submitting">
    <el-card class="publish-card" shadow="never">
      <template #header>
        <div class="card-header"><span class="header-title">发布失物 / 招领</span></div>
      </template>

      <el-form :model="form" label-position="top" class="publish-form">
        <el-row :gutter="40">
          <el-col :md="12">
            <el-form-item label="帖子类型" required>
              <el-radio-group v-model="form.type" size="large">
                <el-radio-button :value="0">寻找失物 (丢失)</el-radio-button>
                <el-radio-button :value="1">失物招领 (拾取)</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="物品名称" required>
              <el-input v-model="form.itemName" placeholder="如：黑色联想笔记本电脑" size="large" />
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
              <el-date-picker v-model="form.eventTime" type="datetime" placeholder="丢失或拾取的时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:00" style="width: 100%" size="large" />
            </el-form-item>
            <el-form-item label="帖子标题 (公开)" required>
              <el-input v-model="form.publicDesc" placeholder="一句话概括，如：在二饭捡到一把黑色雨伞" size="large" />
            </el-form-item>
            <el-form-item label="详情描述 (公开)">
              <el-input v-model="form.semiPublicDesc" type="textarea" :rows="4" placeholder="描述物品外观特征、当时情景等..." />
            </el-form-item>
          </el-col>

          <el-col :md="12">
            <el-form-item label="物品图片">
              <el-upload action="#" list-type="picture-card" :file-list="fileList" :http-request="handleUpload" :on-remove="handleRemove">
                <el-icon><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            
            <el-divider content-position="left">隐私与核验设置 (可选)</el-divider>
            
            <el-form-item label="核验问题 (防冒领)">
              <el-input v-model="form.verifyQuestion" placeholder="如：水杯手柄处有什么图案？" size="large" />
            </el-form-item>
            <el-form-item label="核验答案">
              <el-input v-model="form.verifyAnswer" placeholder="如：哆啦A梦" size="large" />
            </el-form-item>
            <el-form-item label="私密联系方式">
              <el-input v-model="form.privateContact" placeholder="如：微信号 xxxx。对方回答正确后才可见" size="large" />
            </el-form-item>

            <div class="submit-action">
              <el-button @click="router.back()" size="large">取消</el-button>
              <el-button type="primary" @click="onSubmit" :loading="submitting" size="large" class="publish-btn">立即发布</el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { publishItem, uploadImage, generateAiDesc } from '@/api/item'

const router = useRouter()
const submitting = ref(false)

const form = reactive({
  itemName: '',
  eventTime: '',
  location: '',
  publicDesc: '',
  semiPublicDesc: '',
  imagesUrlList: [],
  type: 0,
  verifyQuestion: '',
  verifyAnswer: '',
  privateContact: ''
})

const fileList = ref([])
const selectedLocationPath = ref([])
const locationDetail = ref('')

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

  submitting.value = true
  try {
    const res = await publishItem(form)
    if (res.code === 1 || res.code === 200) {
      const itemId = res.data
      generateAiDesc(itemId).catch(e => console.error('AI润色触发失败', e))
      
      ElMessage.success('发布成功！AI 正在为您生成智能描述...')
      router.push('/')
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