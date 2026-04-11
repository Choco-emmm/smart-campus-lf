import request from '@/utils/request'

// 分页查询广场列表
export function getItemPage(params) {
    return request({
        url: '/item/page',
        method: 'get',
        params: params
    })
}

// 获取物品详情
export function getItemDetail(id) {
    return request({
        url: `/item/${id}`,
        method: 'get'
    })
}

// 发布失物/招领帖 (注意：这里假设你后端已经改成了返回新帖子的 ID)
export function publishItem(data) {
    return request({
        url: '/item/publish',
        method: 'post',
        data: data
    })
}

// 上传图片（返回 URL）
export function uploadImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/item/image',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// AI 一键生成/润色物品描述
export function generateAiDesc(itemId) {
    return request({
        url: `/item/ai/generate-desc/${itemId}`,
        method: 'post'
    })
}