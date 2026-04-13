import request from '@/utils/request'

// ================= 留言相关 =================
// 查看某帖子的所有留言
export function getCommentList(itemId) {
    return request({ url: `/interact/comment/list/${itemId}`, method: 'get' })
}

// 发表留言
export function addComment(data) {
    return request({ url: '/interact/comment/add', method: 'post', data: data })
}

// 获取留言提醒列表 (有哪些帖子有新留言)
export function getCommentNotifications() {
    return request({ url: '/interact/comment/notifications', method: 'get' })
}

// ================= 私信相关 =================
// 获取消息页的会话列表
export function getChatSessions() {
    return request({ url: '/interact/message/sessions', method: 'get' })
}

// 获取与某人的聊天记录 (会自动设为已读)
export function getChatHistory(targetUserId) {
    return request({ url: `/interact/message/history/${targetUserId}`, method: 'get' })
}

// 注：sendPrivateMessage 接口已被移除，现在发送私信完全通过 WebSocket 实时传输完成