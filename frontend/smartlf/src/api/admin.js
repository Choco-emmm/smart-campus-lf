import request from '@/utils/request'

// ================= 1. 平台数据看板 =================
export function getStatsOverview(params) {
    return request({ url: '/admin/stats/overview', method: 'get', params })
}
export function getAiSummary() {
    return request({ url: '/admin/ai/summary', method: 'get' })
}

// ================= 2. 用户管理 =================
export function getUserPage(data) {
    return request({ url: '/admin/user/page', method: 'post', data })
}
export function getUserDetailByAdmin(userId) {
    return request({ url: `/admin/user/${userId}`, method: 'get' })
}
export function updateUserStatus(userId, status) {
    return request({ url: `/admin/user/status/${userId}`, method: 'put', params: { status } })
}

// ================= 3. 举报与信息治理 =================
// 🌟 新增：管理员无视下架状态强查帖子详情
export function getItemDetailByAdmin(itemId) {
    return request({ url: `/admin/item/detail/${itemId}`, method: 'get' })
}
// 一键违规下架
export function banItem(itemId) {
    return request({ url: `/admin/item/ban/${itemId}`, method: 'put' })
}
// 分页查询举报列表
export function getReportPage(data) {
    return request({ url: '/admin/report/page', method: 'post', data })
}
// 处理举报 (action: 0核实下架, 1驳回)
export function resolveReport(data) {
    return request({ url: '/admin/report/resolve', method: 'put', data })
}
export function getReportDetail(reportId) {
    return request({ url: `/admin/report/${reportId}`, method: 'get' })
}
// ================= 4. 置顶审核 =================
// 分页查询置顶申请
export function getTopApplyPage(data) {
    return request({ url: '/admin/top-apply/page', method: 'post', data })
}
// 处理置顶申请 (action: 0同意, 1拒绝)
export function resolveTopApply(data) {
    return request({ url: '/admin/top-apply/resolve', method: 'put', data })
}
// 🌟 新增：管理员一键置顶帖子
export function toggleTopByAdmin(itemId, isTop) {
    return request({
        url: `/admin/item/top/${itemId}`,
        method: 'put',
        params: { isTop } // 后端是用 @RequestParam 接收的
    })
}
