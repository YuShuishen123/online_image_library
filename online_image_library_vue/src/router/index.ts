import { createRouter, createWebHistory } from 'vue-router'
import ACCESS_ENUM from '@/access/accessEnum'
import HomePage from '@/pages/HomePageIndex/HomePage.vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '独立首页',
      component: HomePage,
      meta: {
        access: ACCESS_ENUM.NOT_LOGIN,
      },
    },
  ],
})

// 全局前置守卫保持不变
router.beforeEach((to, from, next) => {
  const loginUserStore = useLoginUserStore()
  const needAccess = to.meta.access as string

  // 如果不需要权限，直接放行
  if (needAccess === ACCESS_ENUM.NOT_LOGIN) {
    next()
    return
  }

  // 如果需要权限，判断用户是否登录
  if (!loginUserStore.loginUser.id || loginUserStore.loginUser.id === 0) {
    next('/picture/user/login')
    return
  }

  // 如果需要管理员权限，但用户不是管理员，跳转到首页
  if (needAccess === ACCESS_ENUM.ADMIN && loginUserStore.loginUser.userRole !== ACCESS_ENUM.ADMIN) {
    next('/')
    return
  }

  next()
})

export default router
