import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLoginUser } from '@/api/userController'

export const useLoginUserStore = defineStore('loginUser', () => {
  // 初始化为未登录状态
  const loginUser = ref<API.LoginUserVO>({
    createTime: '',
    id: 'NOT_LOGIN',
    updateTime: '',
    userAccount: '',
    userAvatar: '',
    userName: '',
    userProfile: '',
    userRole: 'user',
  })

  async function fetchLoginUser() {
    // 调用api获取登陆用户
    getLoginUser().then((res) => {
      if (res.data.code === 200) {
        if (res.data.data) {
          setLoginUser(res.data.data)
        }
      }
    })
  }

  function setLoginUser(newLoginUserVO: API.LoginUserVO) {
    loginUser.value = newLoginUserVO
  }

  return { loginUser, setLoginUser, fetchLoginUser }
})
