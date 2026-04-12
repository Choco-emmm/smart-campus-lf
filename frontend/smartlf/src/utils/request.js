import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus' // 🌟 引入弹窗组件

const service = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 5000
})

// 请求拦截器
service.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['token'] = token // 携带 token
        }
        return config
    },
    error => Promise.reject(error)
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code === 1 || res.code === 200) {
            return res
        } else {
            // 🌟 1. 专门拦截被封禁的状态 (code 1007)，改为醒目的大弹窗
            if (res.code === 1007 || res.msg?.includes('封禁')) {
                ElMessageBox.alert(res.msg || '您的账号已被封禁！请联系管理员', '账号异常提示', {
                    type: 'error',
                    confirmButtonText: '我知道了',
                    callback: () => {
                        localStorage.removeItem('token')
                        // 如果不在登录页，点完确认再踢回登录页；如果在登录页，就什么都不做，留在原地看提示
                        if (window.location.pathname !== '/login') {
                            window.location.href = '/login'
                        }
                    }
                })
                return Promise.reject(new Error(res.msg))
            }

            // 🌟 2. 自动拦截过期、未登录的状态
            if (
                res.code === 401 ||
                res.msg?.includes('未登录') ||
                res.msg?.includes('过期') ||
                res.msg?.includes('失效') ||
                res.msg?.includes('token')
            ) {
                ElMessage.error('登录状态已失效，请重新登录')
                localStorage.removeItem('token')
                if (window.location.pathname !== '/login') {
                    window.location.href = '/login'
                }
                return Promise.reject(new Error(res.msg))
            }

            // 其他普通业务错误 (如账号密码错误)
            ElMessage.error(res.msg || '系统错误')
            return Promise.reject(new Error(res.msg || 'Error'))
        }
    },
    error => {
        // 兼容 HTTP 状态码层面的 401
        if (error.response && error.response.status === 401) {
            ElMessage.error('登录状态已失效，请重新登录')
            localStorage.removeItem('token')
            if (window.location.pathname !== '/login') {
                window.location.href = '/login'
            }
        } else {
            ElMessage.error('网络请求失败，请检查后端是否启动')
        }
        return Promise.reject(error)
    }
)

export default service