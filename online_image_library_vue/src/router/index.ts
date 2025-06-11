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
import checkAccess from '@/access/checkAccess'

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

// 首次获取信息
let firstFetch = true

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser

  if (firstFetch) {
    // 页面首次加载时，等待后端返回信息再校验用户权限
    await loginUserStore.fetchLoginUser()
    loginUser = loginUserStore.loginUser
    firstFetch = false
  }

  const needAccess = (to.meta?.access as string) ?? ACCESS_ENUM.NOT_LOGIN

  // 要跳转的页面必须要登陆
  if (needAccess !== ACCESS_ENUM.NOT_LOGIN) {
    // 如果没登陆，跳转到登录页面
    if (!loginUser || !loginUser.userRole || loginUser.userRole === ACCESS_ENUM.NOT_LOGIN) {
      next(`/login?redirect=${to.fullPath}`)
      return
    }
    // 如果已经登陆了，但是权限不足，那么跳转到无权限页面
    if (!checkAccess(loginUser, needAccess)) {
      next('/')
      return
    }
  }
  next()
})

export default router
