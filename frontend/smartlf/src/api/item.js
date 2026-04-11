import request from '@/utils/request'

// 分页查询
export function getItemPage(params) {
    return request({ url: '/item/page', method: 'get', params })
}
// 🌟 新增：专门获取当前登录用户的发布（含下架）
export function getMyPublishPage(params) {
    // params: { pageNum, pageSize }
    return request({
        url: '/item/my-page',
        method: 'get',
        params: {
            pageNum: params.page,
            pageSize: params.pageSize
        }
    })
}


// 获取详情
export function getItemDetail(id) {
    return request({ url: `/item/${id}`, method: 'get' })
}

// 获取编辑回显数据 (包含敏感暗号)
export function getEditEcho(id) {
    return request({ url: `/item/edit-echo/${id}`, method: 'get' })
}

// 发布帖子
export function publishItem(data) {
    return request({ url: '/item/publish', method: 'post', data })
}

// 修改帖子
export function updateItem(data) {
    return request({ url: '/item/update', method: 'put', data })
}

// 图片上传
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

// AI 润色
export function generateAiDesc(itemId) {
    return request({ url: `/item/ai/generate-desc/${itemId}`, method: 'post' })
}