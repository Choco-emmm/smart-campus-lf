import request from '@/utils/request'

export function login(data) { return request({ url: '/user/login', method: 'post', data }) }
export function register(data) { return request({ url: '/user/register', method: 'post', data }) }
export function checkExist(type, value) { return request({ url: '/user/isExist', method: 'get', params: { type, value } }) }
export function getUserInfo() { return request({ url: '/user/info', method: 'get' }) }
export function getUserProfile(userId) { return request({ url: `/user/profile/${userId}`, method: 'get' }) }

// 修改用户信息
export function updateUserInfo(data) {
    return request({
        url: '/user/info',
        method: 'put',
        data: data
    })
}

// 🌟 修正：修改密码接口 (根据你的 UserController)
export function updatePassword(data) {
    return request({ url: '/user/password', method: 'put', data })
}

// 🌟 新增：旧密码相同校验接口 (失焦校验用)
export function checkSamePassword(value) {
    return request({ url: '/user/isSame', method: 'get', params: { value } })
}