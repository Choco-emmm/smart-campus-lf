<template>
  <div class="admin-container">
    <div class="page-header">
      <h2>信息审核与请求管理</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="custom-tabs">
      
      <el-tab-pane label="违规举报处理" name="report">
        <div class="filter-area">
          <el-select v-model="reportQuery.status" placeholder="处理状态" clearable style="width: 150px;" @change="fetchReportData">
            <el-option label="待处理" :value="0" />
            <el-option label="已核实(下架)" :value="1" />
            <el-option label="已驳回" :value="2" />
          </el-select>
        </div>

        <el-table :data="reportList" v-loading="reportLoading" border style="width: 100%; margin-top: 15px;">
          <el-table-column prop="id" label="举报单ID" width="100" align="center" />
          <el-table-column prop="itemId" label="被举报原帖ID" width="120" align="center" />
          <el-table-column prop="reason" label="举报理由概览" min-width="200" show-overflow-tooltip />
          <el-table-column label="提交时间" width="170" align="center">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.status === 0" type="primary">待处理</el-tag>
              <el-tag v-else-if="row.status === 1" type="danger">已核实(下架)</el-tag>
              <el-tag v-else type="info">已驳回</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="260" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="warning" link @click="openReportDetail(row.id)">举报详情</el-button>
              
              <template v-if="row.status === 0">
                <el-button type="danger" link @click="handleResolveReport(row.id, 0)">核实下架</el-button>
                <el-button type="info" link @click="handleResolveReport(row.id, 1)">驳回</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination v-model:current-page="reportQuery.page" :total="reportTotal" background layout="prev, pager, next" @current-change="fetchReportData" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="置顶申请审核" name="topApply">
        <div class="filter-area">
          <el-select v-model="topQuery.status" placeholder="审核状态" clearable style="width: 150px;" @change="fetchTopData">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </div>

        <el-table :data="topList" v-loading="topLoading" border style="width: 100%; margin-top: 15px;">
          <el-table-column prop="id" label="申请单号" width="100" align="center" />
          <el-table-column prop="itemId" label="申请原帖ID" width="120" align="center" />
          <el-table-column prop="applyReason" label="申请理由" min-width="200" show-overflow-tooltip />
          <el-table-column label="申请时间" width="170" align="center">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.status === 0" type="primary">待审核</el-tag>
              <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
              <el-tag v-else type="info">已拒绝</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="openItemDetail(row.itemId)">审阅原帖</el-button>
              <template v-if="row.status === 0">
                <el-button type="success" link @click="handleResolveTop(row.id, 0)">同意置顶</el-button>
                <el-button type="danger" link @click="handleResolveTop(row.id, 1)">拒绝</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination v-model:current-page="topQuery.page" :total="topTotal" background layout="prev, pager, next" @current-change="fetchTopData" />
        </div>
      </el-tab-pane>

    </el-tabs>

    <el-dialog v-model="reportDialogVisible" title="举报单详细信息" width="550px" destroy-on-close>
      <div v-if="currentReport" v-loading="reportDetailLoading">
        <el-descriptions :column="1" border size="default">
          <el-descriptions-item label="举报单ID">{{ currentReport.reportId }}</el-descriptions-item>
          <el-descriptions-item label="举报人">
            <el-link type="primary" @click="goToProfile(currentReport.reporterId)">
              {{ currentReport.reporterNickname }} (ID: {{ currentReport.reporterId }})
            </el-link>
          </el-descriptions-item>
          <el-descriptions-item label="关联原帖">
             <el-link type="danger" @click="openItemDetail(currentReport.itemId)">
               【{{ currentReport.itemTitle }}】 - 点击审阅原帖内容
             </el-link>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="long-reason-box">
          <p class="label">完整举报理由：</p>
          <div class="content">{{ currentReport.reason }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="reportDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="原帖信息审阅" width="600px" destroy-on-close>
      <div v-loading="detailLoading" class="preview-detail" v-if="currentItem">
        <div class="preview-header">
          <el-tag :type="currentItem.type === 0 ? 'danger' : 'success'">{{ currentItem.type === 0 ? '丢失' : '拾取' }}</el-tag>
          <h3 class="preview-title">{{ currentItem.publicDesc }}</h3>
        </div>
        <el-descriptions :column="1" border size="small" style="margin-top: 15px;">
          <el-descriptions-item label="物品名称">{{ currentItem.itemName }}</el-descriptions-item>
          <el-descriptions-item label="发生地点">{{ currentItem.location }}</el-descriptions-item>
          <el-descriptions-item label="发布者">{{ currentItem.publisherNickname }} (ID: {{ currentItem.userId }})</el-descriptions-item>
          <el-descriptions-item label="详细描述">{{ currentItem.semiPublicDesc || '无' }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
             <el-tag size="small" :type="currentItem.status === 3 ? 'danger' : 'info'">
               {{ currentItem.status === 3 ? '已违规下架' : '正常' }}
             </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="currentItem.imagesUrlList?.length" class="preview-images">
          <p style="margin-bottom: 10px; color: #606266; font-size: 13px;">附图：</p>
          <el-image 
            v-for="img in currentItem.imagesUrlList" 
            :key="img" 
            :src="getImageUrl(img)" 
            :preview-src-list="currentItem.imagesUrlList.map(i => getImageUrl(i))"
            fit="cover" 
            class="preview-img" 
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭审阅</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getReportPage, getReportDetail, resolveReport, 
  getTopApplyPage, resolveTopApply, 
  getItemDetailByAdmin 
} from '@/api/admin'

const router = useRouter()
const activeTab = ref('report')

// ======== 举报模块状态 ========
const reportLoading = ref(false)
const reportList = ref([])
const reportTotal = ref(0)
const reportQuery = reactive({ page: 1, pageSize: 10, status: null })

const reportDialogVisible = ref(false)
const reportDetailLoading = ref(false)
const currentReport = ref(null)

// ======== 置顶模块状态 ========
const topLoading = ref(false)
const topList = ref([])
const topTotal = ref(0)
const topQuery = reactive({ page: 1, pageSize: 10, status: null })

// ======== 审阅弹窗状态 ========
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const currentItem = ref(null)

// 工具函数
const formatTime = (t) => t ? t.replace('T', ' ').substring(0, 16) : ''
const getImageUrl = (url) => {
  if (!url) return ''
  return url.startsWith('http') ? url : `http://localhost:8080${url}`
}
const goToProfile = (id) => router.push(`/profile/${id}`)

const handleTabChange = (tabName) => {
  if (tabName === 'report' && reportList.value.length === 0) fetchReportData()
  if (tabName === 'topApply' && topList.value.length === 0) fetchTopData()
}

// ======== 举报单 API ========
const fetchReportData = async () => {
  reportLoading.value = true
  try {
    const res = await getReportPage(reportQuery)
    reportList.value = res.data.records || []
    reportTotal.value = res.data.total || 0
  } finally { reportLoading.value = false }
}

const openReportDetail = async (reportId) => {
  reportDialogVisible.value = true
  reportDetailLoading.value = true
  try {
    const res = await getReportDetail(reportId)
    currentReport.value = res.data
  } catch (e) {
    reportDialogVisible.value = false
  } finally {
    reportDetailLoading.value = false
  }
}

const handleResolveReport = (reportId, action) => {
  const actionText = action === 0 ? '核实违规并下架该帖' : '驳回该举报'
  ElMessageBox.confirm(`确定要【${actionText}】吗？`, '处理确认', { 
    type: action === 0 ? 'error' : 'warning' 
  }).then(async () => {
    await resolveReport({ reportId, action, remark: '管理员后台处理' })
    ElMessage.success('处理成功')
    fetchReportData()
  }).catch(() => {})
}

// ======== 置顶单 API ========
const fetchTopData = async () => {
  topLoading.value = true
  try {
    const res = await getTopApplyPage(topQuery)
    topList.value = res.data.records || []
    topTotal.value = res.data.total || 0
  } finally { topLoading.value = false }
}

const handleResolveTop = (applyId, action) => {
  const actionText = action === 0 ? '同意置顶' : '拒绝置顶'
  ElMessageBox.confirm(`确定要【${actionText}】吗？`, '处理确认', { 
    type: action === 0 ? 'success' : 'warning' 
  }).then(async () => {
    await resolveTopApply({ applyId, action, remark: '管理员后台处理' })
    ElMessage.success('处理成功')
    fetchTopData()
  }).catch(() => {})
}

// ======== 打开审阅原帖 ========
const openItemDetail = async (itemId) => {
  detailDialogVisible.value = true
  detailLoading.value = true
  currentItem.value = null
  try {
    const res = await getItemDetailByAdmin(itemId)
    currentItem.value = res.data
  } catch (e) {
    ElMessage.error('无法获取原帖信息，可能物理数据已丢失')
    detailDialogVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  fetchReportData()
})
</script>

<style scoped>
.admin-container { padding: 20px; background: #fff; border-radius: 8px; min-height: calc(100vh - 120px); }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0; color: #303133; }
.filter-area { margin-bottom: 15px; }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: center; }
.custom-tabs :deep(.el-tabs__item) { font-size: 16px; padding: 0 25px; }

/* 审阅弹窗样式 */
.preview-header { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.preview-title { margin: 0; font-size: 18px; color: #1d2129; line-height: 1.4; }
.preview-images { margin-top: 20px; display: flex; flex-wrap: wrap; gap: 10px; }
.preview-img { width: 100px; height: 100px; border-radius: 6px; border: 1px solid #ebeef5; cursor: pointer; }

/* 🌟 长文举报理由样式 */
.long-reason-box {
  margin-top: 20px;
  padding: 15px;
  background-color: #fdf6ec;
  border-radius: 4px;
  border-left: 4px solid #e6a23c;
}
.long-reason-box .label { font-weight: bold; margin-bottom: 8px; color: #606266; }
.long-reason-box .content { line-height: 1.6; color: #303133; white-space: pre-wrap; word-break: break-all; }
</style>