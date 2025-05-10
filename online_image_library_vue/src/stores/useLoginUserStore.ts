import {defineStore} from 'pinia'
import {ref} from 'vue'
import {getLoginUserUsingGet} from '@/api/userController'

export const useLoginUserStore = defineStore('loginUser', () => {
  // 初始化为未登录状态
  const loginUser = ref<API.LoginUserVO>({
    createTime: '',
    id: 0,
    updateTime: '',
    userAccount: '',
    userAvatar: '',
    userName: '',
    userProfile: '',
    userRole: 'user',
  })

  async function fetchLoginUser() {
    // 调用api获取登陆用户
    getLoginUserUsingGet().then((res) => {
      // 判断是否登录成功
      console.log(res)
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
