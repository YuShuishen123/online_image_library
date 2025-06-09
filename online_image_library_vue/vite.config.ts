import {fileURLToPath, URL} from 'node:url'
import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

export default defineConfig({
  server: {
    port: 5173, // 明确指定端口
    host: true, // 监听所有地址
    open: true, // 自动打开浏览器
    proxy: {
      '/api': {
        target: 'http://localhost:6789',
        changeOrigin: true, // 建议添加，改变请求头中的host值
        // rewrite: (path) => path.replace(/^\/api/, '') // 如果需要路径重写可以取消注释
      },
    },
  },
  plugins: [vue(), vueDevTools()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  base: process.env.NODE_ENV === 'production' ? '/your-project-name/' : '/', // 根据环境动态设置
})
