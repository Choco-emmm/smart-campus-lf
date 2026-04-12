import request from '@/utils/request'

// 获取数据看板概览
export function getStatsOverview(params) {
    return request({
        url: '/admin/stats/overview',
        method: 'get',
        params
    })
}

// 获取AI周报
export function getAiSummary() {
    return request({
        url: '/admin/ai/summary',
        method: 'get'
    })
}

// 分页查询用户
export function getUserPage(data) {
    return request({
        url: '/admin/user/page',
        method: 'post',
        data
    })
}

// 封禁/解封用户
export function updateUserStatus(userId, status) {
    return request({
        url: `/admin/user/status/${userId}`,
        method: 'put',
        params: { status }
    })
}

// 查看用户详情（管理员视角）
export function getUserDetailByAdmin(userId) {
    return request({
        url: `/admin/user/${userId}`,
        method: 'get'
    })
}

// 分页查询举报
export function getReportPage(data) {
    return request({
        url: '/admin/report/page',
        method: 'post',
        data
    })
}

// 查看举报单详情
export function getReportDetail(reportId) {
    return request({
        url: `/admin/report/${reportId}`,
        method: 'get'
    })
}

// 处理举报
export function resolveReport(data) {
    return request({
        url: '/admin/report/resolve',
        method: 'put',
        data
    })
}

// 分页查询置顶申请
export function getTopApplyPage(data) {
    return request({
        url: '/admin/top-apply/page',
        method: 'post',
        data
    })
}

// 审批置顶
export function resolveTopApply(data) {
    return request({
        url: '/admin/top-apply/resolve',
        method: 'put',
        data
    })
}

// 一键违规下架帖子
export function banItem(itemId) {
    return request({
        url: `/admin/item/ban/${itemId}`,
        method: 'put'
    })
}

// 管理员一键置顶/取消置顶帖子
export function toggleTopByAdmin(itemId, isTop) {
    return request({
        url: `/admin/item/top/${itemId}`,
        method: 'put',
        params: { isTop }
    })
}

// 管理员强制查看帖子底表详情
export function getItemDetailByAdmin(itemId) {
    return request({
        url: `/admin/item/detail/${itemId}`,
        method: 'get'
    })
}