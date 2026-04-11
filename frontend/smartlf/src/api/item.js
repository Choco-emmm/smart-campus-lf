import request from '@/utils/request'

export function getItemPage(params) { return request({ url: '/item/page', method: 'get', params }) }
export function getMyPublishPage(params) {
    return request({ url: '/item/my-page', method: 'get', params: { pageNum: params.page, pageSize: params.pageSize } })
}
export function getItemDetail(id) { return request({ url: `/item/${id}`, method: 'get' }) }
export function getEditEcho(id) { return request({ url: `/item/edit-echo/${id}`, method: 'get' }) }
export function publishItem(data) { return request({ url: '/item/publish', method: 'post', data }) }
export function updateItem(data) { return request({ url: '/item/update', method: 'put', data }) }
export function uploadImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({ url: '/item/image', method: 'post', data: formData, headers: { 'Content-Type': 'multipart/form-data' } })
}
export function generateAiDesc(itemId) { return request({ url: `/item/ai/generate-desc/${itemId}`, method: 'post' }) }
export function deleteItem(id) { return request({ url: `/item/delete/${id}`, method: 'delete' }) }

// 🌟 新增：举报帖子接口
export function reportItem(data) {
    return request({ url: '/item/report', method: 'post', data })
}
export function applyItemTop(data) {
    return request({
        url: '/item/top/apply',
        method: 'post',
        data
    })
}