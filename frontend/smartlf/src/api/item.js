import request from '@/utils/request'

// 广场分页查询
export function getItemPage(params) {
    return request({
        url: '/item/page',
        method: 'get',
        params // 对应后端的 QueryDTO
    })
}

// 专门获取当前登录用户的发布（含下架）
export function getMyPublishPage(params) {
    return request({
        url: '/item/my-page',
        method: 'get',
        params: {
            pageNum: params.page,
            pageSize: params.pageSize
        }
    })
}

// 获取帖子详情
export function getItemDetail(id) { return request({ url: `/item/${id}`, method: 'get' }) }

// 获取编辑回显数据
export function getEditEcho(id) { return request({ url: `/item/edit-echo/${id}`, method: 'get' }) }

// 发布与修改
export function publishItem(data) { return request({ url: '/item/publish', method: 'post', data }) }
export function updateItem(data) { return request({ url: '/item/update', method: 'put', data }) }

// 图片上传
export function uploadImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({ url: '/item/image', method: 'post', data: formData, headers: { 'Content-Type': 'multipart/form-data' } })
}

// 功能性接口
export function generateAiDesc(itemId) { return request({ url: `/item/ai/generate-desc/${itemId}`, method: 'post' }) }
export function deleteItem(id) { return request({ url: `/item/delete/${id}`, method: 'delete' }) }
export function reportItem(data) { return request({ url: '/item/report', method: 'post', data }) }
export function applyItemTop(data) { return request({ url: '/item/top/apply', method: 'post', data }) }
export function updateItemStatus(data) { return request({ url: '/item/status', method: 'put', data }) }