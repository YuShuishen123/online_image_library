import {defineStore} from 'pinia'
import {ref} from 'vue'

// 定义用户类型接口
interface LoginUser {
  userId: number | null
  userName: string
}

export const useLoginUserStore = defineStore('loginUser', () => {
  // 初始化为未登录状态
  const loginUser = ref<LoginUser>({
    userId: 0,
    userName: '未登录',
  })

  async function fetchLoginUser() {
    // 测试用户登录，3 秒后登录
    setTimeout(() => {
      loginUser.value = { userName: '测试用户', userId: 1 }
    }, 3000)
  }

  function setLoginUser(newLoginUser: LoginUser) {
    loginUser.value = newLoginUser
  }

  return { loginUser, setLoginUser, fetchLoginUser }
})
