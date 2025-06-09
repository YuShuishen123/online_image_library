<template>
  <div id="basciLayout">
    <a-layout class="main-layout">
      <a-layout-header :style="headerStyle" class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout-content :style="contentStyle" class="content">
        <router-view />
      </a-layout-content>
      <a-layout-footer class="footer">
        <div class="footer-content">
          <div class="copyright">
            <span>© 2023-{{ currentYear }} </span>
            <a class="footer-link" href="https://github.com/YuShuishen123?tab=repositories" target="_blank">
              Github</a>
            <span> | Create By YuShuishen</span>
          </div>
          <div class="footer-info">
            <span>在线图片库 - 让创意无限绽放</span>
          </div>
        </div>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from '../components/GlobalHeader.vue'
// 获取当前年份用于版权信息
const currentYear = new Date().getFullYear()

// 这里可以定义您的样式变量
const headerStyle = {
  background: '#fff',
  padding: '0 20px',
  borderBottom: '1px solid #f0f0f0',
}

// 这里可以定义您的样式变量
const contentStyle = {
  padding: '24px',
  minHeight: '280px',
}
</script>

<style scoped>
#basciLayout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-layout {
  flex: 1 0 auto;
  display: flex;
  flex-direction: column;
}

.header {
  flex-shrink: 0;
}

.content {
  flex: 1 0 auto;
  min-height: 0;
}

.footer {
  flex-shrink: 0;
  text-align: center;
  padding: 16px 50px;
  background: #f5f5f5;
  color: rgba(0, 0, 0, 0.65);
}

.footer-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* Copyright */
.copyright {
  margin-bottom: 8px;
  font-size: 14px;
}

/* Footer Info */
.footer-info {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.footer-link {
  color: #1890ff;
  text-decoration: none;
  transition: color 0.3s;
  font-weight: 500;
}

.footer-link:hover {
  color: #40a9ff;
  text-decoration: underline;
}
</style>
