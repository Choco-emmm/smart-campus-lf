import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/',
      component: () => import('@/layout/BasicLayout.vue'),
      children: [
        { path: '', name: 'Plaza', component: () => import('@/views/plaza/ItemPlaza.vue') },
        { path: 'publish', name: 'Publish', component: () => import('@/views/item/ItemPublish.vue') },
        { path: 'item/:id', name: 'ItemDetail', component: () => import('@/views/item/ItemDetail.vue') },
        { path: 'item/edit/:id', name: 'ItemEdit', component: () => import('@/views/item/ItemEdit.vue') },
        { path: 'message', name: 'MessageCenter', component: () => import('@/views/message/MessageCenter.vue') },
        { path: 'profile/:id', name: 'UserProfile', component: () => import('@/views/user/UserProfile.vue') }
      ]
    }
  ],
})

router.beforeEach((to, from) => {
  const token = localStorage.getItem('token')

  // 如果要去登录页，且已经有token，直接回首页
  if (to.path === '/login' && token) {
    return '/' // 代替 next('/')
  }

  // 如果要去需要权限的页面且没token
  if (to.path !== '/login' && !token) {
    return '/login' // 代替 next('/login')
  }

  return true // 代替 next()，表示放行
})

export default router