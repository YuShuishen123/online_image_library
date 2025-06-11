import { createRouter, createWebHistory } from 'vue-router'
import ACCESS_ENUM from '@/access/accessEnum'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import MainLayout from '@/components/HomepageComponent/MainLayout.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import PictureShow from '@/pages/user/PictureShow.vue'
import UserSpace from '@/pages/user/UserSpace.vue'
import FeaturesSection from '@/components/HomepageComponent/FeaturesSection.vue'
import AIIntroduction from '@/pages/AI/AIIntroduction.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '首页',
      component: MainLayout,
      meta: {
        access: ACCESS_ENUM.NOT_LOGIN,
      },
    },
    {
      path: '/AIintroduction',
      name: 'AI功能详细介绍页',
      component: AIIntroduction,
      meta: {
        access: ACCESS_ENUM.NOT_LOGIN,
      },
    },
    {
      path: '/login',
      name: '登陆',
      component: UserLoginPage,
      meta: {
        access: ACCESS_ENUM.NOT_LOGIN,
      },
    },
    {
      path: '/register',
      name: '注册',
      component: UserRegisterPage,
      meta: {
        access: ACCESS_ENUM.NOT_LOGIN,
      },
    },
    {
      path: '/publicGallery',
      name: '公共图库',
      component: PictureShow,
      meta: {
        access: ACCESS_ENUM.NOT_LOGIN,
      },
    },
    {
      path: '/personalGallery',
      name: '个人图库',
      component: UserSpace,
      meta: {
        access: ACCESS_ENUM.USER,
      },
    },
    {
      path: '/AI',
      name: 'AI',
      component: FeaturesSection,
      meta: {
        access: ACCESS_ENUM.NOT_LOGIN,
      },
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
})

// 全局前置守卫
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
    next('/login')
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
