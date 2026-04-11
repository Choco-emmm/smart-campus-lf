import request from '@/utils/request'

export function login(data) {
    return request({ url: '/user/login', method: 'post', data: data })
}

export function register(data) {
    return request({ url: '/user/register', method: 'post', data: data })
}

export function checkExist(type, value) {
    return request({
        url: '/user/isExist',
        method: 'get',
        params: { type: type, value: value }
    })
}

// 获取当前登录用户自己的信息
export function getUserInfo() {
    return request({ url: '/user/info', method: 'get' })
}

// 查看他人公开主页
export function getUserProfile(userId) {
    return request({ url: `/user/profile/${userId}`, method: 'get' })
}