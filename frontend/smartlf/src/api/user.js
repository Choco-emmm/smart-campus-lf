import request from '@/utils/request'

export function login(data) { return request({ url: '/user/login', method: 'post', data }) }
export function register(data) { return request({ url: '/user/register', method: 'post', data }) }
export function checkExist(type, value) { return request({ url: '/user/isExist', method: 'get', params: { type, value } }) }
export function getUserInfo() { return request({ url: '/user/info', method: 'get' }) }
export function getUserProfile(userId) { return request({ url: `/user/profile/${userId}`, method: 'get' }) }

// 🌟 修改用户信息
export function updateUserInfo(data) {
    return request({
        url: '/user/info', // 🌟 必须改成 /user/info
        method: 'put',     // 🌟 必须改成 put 请求
        data: data
    })
} export function updatePassword(data) {
    return request({ url: '/user/update-password', method: 'put', data })
}