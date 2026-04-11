<template>
  <div class="layout-wrapper">
    <header class="forum-header">
      <div class="header-inner">
        <div class="logo-area" @click="router.push('/')">
          <span class="logo-text">SmartLF 校园失物招领</span>
        </div>
        
        <el-menu :default-active="route.path" mode="horizontal" router class="header-menu" :ellipsis="false">
          <el-menu-item index="/">全部广场</el-menu-item>
          <el-menu-item index="/publish">发布求助</el-menu-item>
          <el-menu-item index="/message">消息中心</el-menu-item>
        </el-menu>

        <div class="user-area">
          <el-dropdown trigger="click">
            <span class="el-dropdown-link">
              <el-avatar :size="32" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout" style="color: #F56C6C;">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main class="forum-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const handleLogout = () => {
  localStorage.removeItem('token')
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.layout-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.forum-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: #fff;
  border-bottom: 1px solid #e4e6eb;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #1e80ff;
  cursor: pointer;
  letter-spacing: 1px;
}

.header-menu {
  flex: 1;
  margin: 0 40px;
  border-bottom: none;
  height: 60px;
}

.el-menu-item {
  font-size: 16px;
}

.forum-main {
  flex: 1;
  width: 100%;
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
  box-sizing: border-box;
}
</style>