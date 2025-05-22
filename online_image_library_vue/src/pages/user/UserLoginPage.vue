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
        <a-button type="primary" html-type="submit" :loading="loading" style="width: 100%">登录</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { userLoginUsingPost } from '@/api/userController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router' // 添加这行导入
const loading = ref(false)
const router = useRouter()  // 添加这行
const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})
const loginUserStore = useLoginUserStore()
const handleSubmit = async () => {
  loading.value = true
  try {
    const res = await userLoginUsingPost(formState)
    if (res.data.code === 200) {
      message.success('登录成功')
      console.log('登录成功', res.data)
      if (res.data.data) {
        loginUserStore.setLoginUser(res.data.data)
      }
      router.push('/')
    } else {
      console.log('登录失败', res.data)
      message.error(res.data.message)
    }
  } finally {
    loading.value = false
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
