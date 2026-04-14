import request from '@/utils/request'

// ================= 留言相关 =================
export function getCommentList(itemId) {
    return request({ url: `/interact/comment/list/${itemId}`, method: 'get' })
}
export function addComment(data) {
    return request({ url: '/interact/comment/add', method: 'post', data: data })
}
export function getCommentNotifications() {
    return request({ url: '/interact/comment/notifications', method: 'get' })
}

// ================= 私信相关 =================
export function getChatSessions() {
    return request({ url: '/interact/message/sessions', method: 'get' })
}
export function getChatHistory(targetUserId) {
    return request({ url: `/interact/message/history/${targetUserId}`, method: 'get' })
}
export function getPrivateMessageNotifications() {
    return request({ url: '/interact/message/notifications', method: 'get' })
}

// ================= 认领申请功能 =================
export function submitClaim(data) {
    return request({ url: '/interact/claim/submit', method: 'post', data: data })
}
export function supplementClaim(data) {
    return request({ url: '/interact/claim/supplement', method: 'post', data: data })
}
export function auditClaim(data) {
    return request({ url: '/interact/claim/audit', method: 'post', data: data })
}
export function getMyReceivedClaims() {
    return request({ url: '/interact/claim/received', method: 'get' })
}
// 👇 就是这里！把 /sent 改成 /submitted 👇
export function getMySentClaims() {
    return request({ url: '/interact/claim/submitted', method: 'get' })
}