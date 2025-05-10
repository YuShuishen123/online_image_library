<template>
  <div id="userLoginPage">
    <h2 class="title">在线图库 - 用户登录</h2>
    <div class="desc">企业级智能协同云图库!!!</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item name="userAccount" :rules="[
        { required: true, message: '请输入账号' },
        { min: 4, max: 16, message: '账号长度只能在 6 到 16 个字符之间' },
      ]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item name="userPassword" :rules="[
        { required: true, message: '请输入密码' },
        { min: 8, max: 16, message: '密码长度只能在 8 到 16 个字符之间' },
      ]">
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>
      <div class="tips">
        没有账号？
        <RouterLink to="/user/register">去注册</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">登录</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import {reactive} from 'vue'
import {userLoginUsingPost} from '@/api/userController'
import {useLoginUserStore} from '@/stores/useLoginUserStore'
import {message} from 'ant-design-vue'

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})
const loginUserStore = useLoginUserStore()
const handleSubmit = async () => {
  const res = await userLoginUsingPost(formState)
  if (res.data.code === 200) {
    console.log('登录成功', res.data)
    // 保存用户信息到本地存储
    // 把状态保存到stores
    if (res.data.data) {
      loginUserStore.setLoginUser(res.data.data)
    }
    // 跳转到首页
    window.location.href = '/'
  } else {
    console.log('登录失败', res.data)
    // 提示登录失败
    message.error(res.data.message)
  }
}
</script>

<style lang="css" scoped>
#userLoginPage {
  max-width: 360px;
  margin: 0 auto;
  margin-top: 100px;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  margin-bottom: 16px;
  color: #bbb;
  font-size: 13px;
  text-align: right;
}
</style>
