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
        // 🌟 修复点：修改帖子页面必须放在 children 里，且不带前导斜杠（它会继承父级的 /）
        { path: 'item/edit/:id', name: 'ItemEdit', component: () => import('@/views/item/ItemEdit.vue') },
        { path: 'message', name: 'MessageCenter', component: () => import('@/views/message/MessageCenter.vue') },
        { path: 'profile/:id', name: 'UserProfile', component: () => import('@/views/user/UserProfile.vue') }
      ]
    }
  ],
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router