<template>
  <div id="globalHeader">
    <a-row :wrap="false">
      <a-col flex="200px">
        <RouterLink to="/">
          <div class="title-bar">
            <img class="logo" src="../assets/logo.svg" alt="logo" />
            <div class="title">鱼皮云图库</div>
          </div>
        </RouterLink>
      </a-col>
      <a-col flex="auto">
        <a-menu v-model:selectedKeys="current" mode="horizontal" :items="menus" @click="doMenuClick" />
      </a-col>
      <a-col flex="120px">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id != 0">
            <a-dropdown>
              <a class="ant-dropdown-link" @click.prevent>
                <a-space>
                  <a-avatar shape="square" :size="32" :src="loginUserStore.loginUser.userAvatar">
                  </a-avatar>
                  {{ loginUserStore.loginUser.userName ?? '无名' }}
                </a-space>
              </a>
              <template #overlay>
                <a-menu>
                  <a-menu-item>
                    <a href="javascript:" @click="logout">退出登陆</a>
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" @click="router.push('/user/login')">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>


  </div>
</template>
<script lang="ts" setup>
import { computed, h, ref } from 'vue'
import type { MenuProps } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { type RouteRecordRaw, useRouter } from "vue-router";
import { userLogoutUsingPost } from '@/api/userController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { HomeOutlined } from '@ant-design/icons-vue'
import checkAccess from '@/access/checkAccess';

const router = useRouter()
const loginUserStore = useLoginUserStore()



// 监听路由变化，更新当前选中菜单
router.afterEach((to) => {
  current.value = [to.path];
});

// 退出登录
const logout = async () => {
  const res = await userLogoutUsingPost()
  if (res.data.code === 200) {
    // 清空用户信息
    await loginUserStore.setLoginUser({
      id: 0,
    });
    message.success('退出登录成功')
    // 跳转到首页
    router.push('/')
  } else {
    message.error("退出登录失败," + res.data.message)
  }
}

// 路由跳转事件
const doMenuClick = ({ key }: { key: string }) => {
  router.push({
    path: key,
  });
};

// 当前选中菜单
const current = ref<string[]>(['home'])
const originItems = ref<MenuProps['items']>([
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  }
])
// 过滤菜单项
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const menuToRouteItem = (menu: any): RouteRecordRaw => {
  // 获取所有路由
  const routes = router.getRoutes()
  // 根据菜单的key查找对应的路由
  const route = routes.find((route) => route.path === menu.key)
  // 如果找到对应路由则返回，否则返回一个默认的空路由对象
  return route || ({} as RouteRecordRaw)
}

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  // 过滤条件是一个回调函数 (menu) => { ... }, 返回 true 表示保留该菜单项，返回 false 表示过滤掉该菜单项
  return menus?.filter((menu) => {
    // 通过menu的key值找到对应的路由字段
    const item = menuToRouteItem(menu)
    // 如果是菜单项中没有对应的路由，就说明该菜单是自定义的，予以保留，返回true
    if (!item.path) {
      return true
    }
    // 如果有hideInMenu标记为true，则隐藏
    if (item.meta?.hideInMenu) {
      return false
    }
    // 根据权限过滤菜单，有权限则返回true，会保留该菜单
    return checkAccess(loginUserStore.loginUser, item.meta?.access as string)
  })
}

// 展示在菜单的路由数组
const menus = computed(() => {
  return filterMenus(originItems.value)
})

</script>

<style scoped>
.title-bar {
  display: flex;
  align-items: center;
}

.title {
  color: black;
  font-size: 18px;
  margin-left: 16px;
}

.logo {
  height: 48px;
}
</style>
