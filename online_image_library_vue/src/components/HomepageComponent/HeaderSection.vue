<template>
  <header class="navbar">
    <div class="container">
      <RouterLink to="/" class="logo">光影<span>AI</span></RouterLink>
      <nav>
        <ul class="nav-links">
          <li>
            <a
              :class="{ active: isActiveMenu('/AIintroduction'), glow: hoverMenu === 'ai' }"
              href="#"
              @mouseenter="hoverMenu = 'ai'"
              @mouseleave="hoverMenu = ''"
              @click.prevent="router.push('/AIintroduction')"
              >智能编辑</a
            >
          </li>
          <li>
            <a
              :class="{ active: isActiveMenu('/publicGallery'), glow: hoverMenu === 'public' }"
              href="#"
              @mouseenter="hoverMenu = 'public'"
              @mouseleave="hoverMenu = ''"
              @click.prevent="goToPublic"
              >公共图库</a
            >
          </li>
          <li>
            <a
              :class="{ active: isActiveMenu('/personalGallery'), glow: hoverMenu === 'personal' }"
              href="#"
              @mouseenter="hoverMenu = 'personal'"
              @mouseleave="hoverMenu = ''"
              @click.prevent="goToUser"
              >个人图库</a
            >
          </li>
        </ul>
      </nav>
      <div class="user-login-status">
        <template v-if="loginUserStore.loginUser.id && loginUserStore.loginUser.id !== 0">
          <a-dropdown>
            <template #overlay>
              <a-menu>
                <a-menu-item>
                  <a href="javascript:" @click="goToUser">个人空间</a>
                </a-menu-item>
                <a-menu-item>
                  <a href="javascript:" @click="logout">退出登录</a>
                </a-menu-item>
              </a-menu>
            </template>
            <a class="ant-dropdown-link user-info" @click.prevent>
              <a-avatar shape="circle" :size="40" :src="loginUserStore.loginUser.userAvatar" />
              <span class="user-name">{{ loginUserStore.loginUser.userName ?? '无名' }}</span>
            </a>
          </a-dropdown>
        </template>
        <template v-else>
          <a href="#" class="cta-button login-btn" @click.prevent="goLogin">登录 / 注册</a>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { userLogout } from '@/api/userController'
import { message } from 'ant-design-vue'
import { onMounted, ref } from 'vue'

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()
const hoverMenu = ref('')

onMounted(() => {
  loginUserStore.fetchLoginUser()
})

function goLogin() {
  router.push('/login')
}
function goToPublic() {
  router.push('/publicGallery')
}
function goToUser() {
  router.push('/personalGallery')
}

function logout() {
  userLogout()
    .then(() => {
      loginUserStore.setLoginUser({ id: 0 })
      message.success('退出登录成功')
    })
    .catch(() => {
      message.error('退出登录失败')
    })
}

// 判断当前激活菜单
function isActiveMenu(path: string) {
  // 首页时不高亮任何菜单项
  if (route.path === '/') return false
  return route.path === path
}
</script>

<style scoped>
.navbar {
  padding: 20px 0;
  background-color: rgba(22, 22, 26, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.navbar .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: var(--text-color);
}

.logo span {
  color: var(--primary-color);
}

.nav-links {
  list-style: none;
  display: flex;
  gap: 25px;
}

.nav-links a {
  color: var(--text-color);
  font-size: 16px;
}

.nav-links a.active {
  color: var(--primary-color);
  font-weight: bold;
  border-bottom: 2px solid var(--primary-color);
  padding-bottom: 2px;
}

.nav-links a.glow {
  position: relative;
  z-index: 1;
  color: var(--primary-color);
  text-shadow:
    0 0 8px var(--primary-color),
    0 0 16px var(--primary-color);
  transition:
    text-shadow 0.2s,
    color 0.2s;
}

.user-login-status {
  display: flex;
  align-items: center;
  height: 60px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 6px 18px;
  border-radius: 32px;
  background: rgba(127, 90, 240, 0.08);
  transition:
    background 0.2s,
    box-shadow 0.2s;
}

.user-info:hover {
  background: rgba(127, 90, 240, 0.18);
  box-shadow: 0 2px 12px rgba(127, 90, 240, 0.1);
}

.user-name {
  color: #fff;
  font-weight: 700;
  font-size: 17px;
  letter-spacing: 1px;
}

.login-btn {
  background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
  color: white;
  padding: 10px 20px;
  border-radius: 50px;
  font-weight: bold;
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
  border: none;
  font-size: 16px;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(127, 90, 240, 0.4);
  color: white;
}
</style>
