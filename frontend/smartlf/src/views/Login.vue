<template>
  <div class="login-container">
    <el-card class="login-card" shadow="hover">
      <div class="logo-container">
        <h2>SmartLF 校园失物招领</h2>
        <p class="subtitle">找回你的小确幸</p>
      </div>

      <el-tabs v-model="activeName" class="login-tabs" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" size="large">
            <el-form-item prop="account">
              <el-input v-model="loginForm.account" placeholder="用户名/手机号/邮箱" :prefix-icon="User" clearable />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="submit-btn" :loading="loading" @click="handleLogin">登 录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" size="large">
            <el-form-item prop="username">
              <el-input v-model="registerForm.username" placeholder="设置用户名 (4-16位字母或数字)" :prefix-icon="User" clearable />
            </el-form-item>
            <el-form-item prop="phone">
              <el-input v-model="registerForm.phone" placeholder="手机号 (11位)" clearable />
            </el-form-item>
            <el-form-item prop="email">
              <el-input v-model="registerForm.email" placeholder="邮箱" clearable />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="设置密码 (6-16位，需含字母和数字)" :prefix-icon="Lock" show-password />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" :prefix-icon="CircleCheck" show-password />
            </el-form-item>
            
            <el-form-item prop="role">
              <el-radio-group v-model="registerForm.role">
                <el-radio :value="0">普通学生</el-radio>
                <el-radio :value="1">管理员</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item prop="secretKey" v-if="registerForm.role === 1">
              <el-input v-model="registerForm.secretKey" placeholder="请输入管理员授权密钥" :prefix-icon="Key" clearable @keyup.enter="handleRegister" />
            </el-form-item>

            <el-form-item>
              <el-button type="success" class="submit-btn" :loading="loading" @click="handleRegister">注册并登录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, CircleCheck, Key } from '@element-plus/icons-vue'
import { login, register, checkExist } from '@/api/user' // 引入了 checkExist

const router = useRouter()
const activeName = ref('login')
const loading = ref(false)

// ======== 登录逻辑 ========
const loginFormRef = ref(null)
const loginForm = reactive({ account: '', password: '' })
const loginRules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login({ account: loginForm.account, password: loginForm.password })
        ElMessage.success('登录成功')
        localStorage.setItem('token', res.data.token) 
        router.push('/')
      } finally {
        loading.value = false
      }
    }
  })
}

// ======== 注册逻辑 ========
const registerFormRef = ref(null)
const registerForm = reactive({
  username: '', phone: '', email: '', password: '', confirmPassword: '',
  role: 0,
  secretKey: ''
})

// === 异步失焦查重校验器 ===
const validateUsernameExist = async (rule, value, callback) => {
  if (!value) return callback() // 空值交给 required 去拦截
  try {
    const res = await checkExist('USERNAME', value)
    if (res.data === true) {
      callback(new Error('该用户名已被占用，请换一个试试'))
    } else {
      callback()
    }
  } catch (error) {
    callback() // 网络异常时放行，以免卡死用户
  }
}

const validatePhoneExist = async (rule, value, callback) => {
  if (!value || !/^1[3-9]\d{9}$/.test(value)) return callback() // 格式不对不发请求，省宽带
  try {
    const res = await checkExist('PHONE', value)
    if (res.data === true) {
      callback(new Error('该手机号已注册，请直接登录'))
    } else {
      callback()
    }
  } catch (error) {
    callback()
  }
}

const validateEmailExist = async (rule, value, callback) => {
  // 简单排查一下是不是邮箱格式，如果不是就不去后端查了
  if (!value || !value.includes('@')) return callback() 
  try {
    const res = await checkExist('EMAIL', value)
    if (res.data === true) {
      callback(new Error('该邮箱已注册，请直接登录'))
    } else {
      callback()
    }
  } catch (error) {
    callback()
  }
}

// 二次密码校验
const validatePass2 = (rule, value, callback) => {
  if (value === '') callback(new Error('请再次输入密码'))
  else if (value !== registerForm.password) callback(new Error('两次输入密码不一致!'))
  else callback()
}

// 动态校验密钥
const validateSecret = (rule, value, callback) => {
  if (registerForm.role === 1 && !value) callback(new Error('管理员必须输入授权密钥'))
  else callback()
}

// 详尽的注册校验规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 16, message: '用户名长度需在 4 到 16 个字符之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur'},
    { validator: validateUsernameExist, trigger: 'blur' } // 🌟 触发失焦查重
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入格式正确的 11 位手机号码', trigger: 'blur' },
    { validator: validatePhoneExist, trigger: 'blur' } // 🌟 触发失焦查重
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入格式正确的邮箱地址', trigger: ['blur', 'change'] },
    { validator: validateEmailExist, trigger: 'blur' } // 🌟 触发失焦查重
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { 
      pattern: /^(?=.*[a-zA-Z])(?=.*\d).{6,16}$/, 
      message: '密码长度需为 6-16 位，且必须同时包含字母和数字', 
      trigger: 'blur' 
    }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ],
  secretKey: [
    { validator: validateSecret, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    // 只有所有正则和失焦查重都通过了，才会来到这里
    if (valid) {
      loading.value = true
      try {
        const payload = {
          username: registerForm.username,
          phone: registerForm.phone,
          email: registerForm.email,
          password: registerForm.password,
          role: registerForm.role,
        }
        if (registerForm.role === 1) payload.secretKey = registerForm.secretKey

        await register(payload)
        ElMessage.success('注册成功，请登录')
        loginForm.account = registerForm.username
        loginForm.password = ''
        activeName.value = 'login'
        registerFormRef.value.resetFields()
      } finally {
        loading.value = false
      }
    } else {
      ElMessage.warning('请检查输入格式是否正确')
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
.login-card {
  width: 100%;
  max-width: 420px;
  border-radius: 12px;
  padding: 20px 10px;
  border: none;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
}
.logo-container { text-align: center; margin-bottom: 25px; }
.logo-container h2 { margin: 0; font-size: 24px; color: #303133; }
.subtitle { margin: 8px 0 0; font-size: 14px; color: #909399; }
:deep(.el-tabs__nav-wrap::after) { height: 1px; background-color: #ebeef5; }
.submit-btn { width: 100%; font-size: 16px; letter-spacing: 2px; margin-top: 10px; }
</style>