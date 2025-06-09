import {createRouter, createWebHistory} from 'vue-router'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'
import PictureManagePage from '@/pages/admin/PictureManagePage.vue'
import ACCESS_ENUM from '@/access/accessEnum'
import test from '@/pages/HomePageIndex/testPage.vue'
import {useLoginUserStore} from '@/stores/useLoginUserStore'
import BasciLayout from '@/layouts/BasciLayout.vue'
import PictureShow from '@/pages/user/PictureShow.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '独立首页',
      component: test,
      meta: {
        access: ACCESS_ENUM.NOT_LOGIN,
      },
    },
    {
      path: '/picture', // 统一使用小写路径
      name: '基础布局',
      component: BasciLayout,
      children: [
        {
          path: '', // 默认子路由，访问/homepage时显示
          component: PictureShow,
          meta: {
            access: ACCESS_ENUM.NOT_LOGIN,
          },
        },
        {
          path: 'user/login',
          name: '用户登录',
          component: UserLoginPage,
          meta: {
            access: ACCESS_ENUM.NOT_LOGIN,
          },
        },
        {
          path: 'user/register',
          name: '用户注册',
          component: UserRegisterPage,
          meta: {
            access: ACCESS_ENUM.NOT_LOGIN,
          },
        },
        {
          path: 'admin/userManage',
          name: '用户管理(管理员)',
          component: UserManagePage,
          meta: {
            access: ACCESS_ENUM.ADMIN,
          },
        },
        {
          path: 'admin/pictureManage',
          name: '图片管理(管理员)',
          component: PictureManagePage,
          meta: {
            access: ACCESS_ENUM.ADMIN,
          },
        },
      ],
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
