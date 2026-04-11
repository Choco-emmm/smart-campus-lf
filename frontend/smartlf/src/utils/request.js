import axios from 'axios'
import { ElMessage } from 'element-plus'

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
            // 🌟 自动拦截过期、未登录或被封禁的状态
            if (
                res.code === 401 ||
                res.msg?.includes('未登录') ||
                res.msg?.includes('过期') ||
                res.msg?.includes('失效') ||
                res.msg?.includes('token')
            ) {
                ElMessage.error('登录状态已失效，请重新登录')
                localStorage.removeItem('token')
                window.location.href = '/login' // 强制跳回登录页
                return Promise.reject(new Error(res.msg))
            }

            ElMessage.error(res.msg || '系统错误')
            return Promise.reject(new Error(res.msg || 'Error'))
        }
    },
    error => {
        // 兼容 HTTP 状态码层面的 401
        if (error.response && error.response.status === 401) {
            ElMessage.error('登录状态已失效，请重新登录')
            localStorage.removeItem('token')
            window.location.href = '/login'
        } else {
            ElMessage.error('网络请求失败，请检查后端是否启动')
        }
        return Promise.reject(error)
    }
)

export default service