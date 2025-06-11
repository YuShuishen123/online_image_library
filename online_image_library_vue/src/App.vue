<template>
  <a-config-provider :locale="appLocale">
    <div id="app">
      <HomePage />
    </div>
  </a-config-provider>
</template>

<script setup lang="ts">
// 引入 Vue 和 Ant Design Vue
import { health } from './api/mainController'
import zhCN from 'ant-design-vue/es/locale/zh_CN' // 引入 ant-design-vue 的中文语言包
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn' // 引入 dayjs 的中文语言包
import HomePage from './pages/HomePageIndex/HomePage.vue'

// 设置 dayjs 默认语言为中文
dayjs.locale('zh-cn')

// 使用 Composition API 和 setup 函数定义响应式数据
const appLocale = zhCN // 设置当前应用的语言环境

health().then((res) => {
  // 处理 API 返回的数据
  console.log(res)
})
</script>
