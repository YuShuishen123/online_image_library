import axios from 'axios'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

// 创建 Axios 实例
const myAxios = axios.create({
  baseURL: '/api',
  timeout: 60000,
  withCredentials: true,
})

// 转换所有ID字段为字符串
// const convertIdToString = <T>(data: T): T => {
//   if (data === null || data === undefined) {
//     return data
//   }

//   if (typeof data === 'object') {
//     if (Array.isArray(data)) {
//       return data.map((item) => convertIdToString(item)) as T
//     }

//     const result: Record<string, unknown> = {}
//     for (const key in data) {
//       const value = (data as Record<string, unknown>)[key]
//       // 只要字段名包含id就转换为字符串
//       if (key.toLowerCase().includes('id') && value !== null && value !== undefined) {
//         result[key] = String(value)
//       } else {
//         result[key] = convertIdToString(value)
//       }
//     }
//     return result as T
//   }

//   return data
// }

// 全局请求拦截器
myAxios.interceptors.request.use(
  function (config) {
    // Do something before request is sent
    return config
  },
  function (error) {
    // Do something with request error
    return Promise.reject(error)
  },
)

// 全局响应拦截器
myAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    // 未登录
    if (data.code === 40100) {
      // 检查本地存储中是否有登录信息
      const loginUserStore = useLoginUserStore()
      const hasLoginInfo = loginUserStore.loginUser && loginUserStore.loginUser.id

      // 如果没有登录信息，并且不是获取用户信息的请求，并且用户目前不是已经在用户登录页面，则跳转到登录页面
      if (
        !hasLoginInfo &&
        !response.request.responseURL.includes('user/get/login') &&
        !window.location.pathname.includes('/user/login')
      ) {
        message.warning('请先登录')
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  function (error) {
    // Any status codes that falls outside the range of 2xx cause this function to trigger
    // Do something with response error
    return Promise.reject(error)
  },
)

export default myAxios
