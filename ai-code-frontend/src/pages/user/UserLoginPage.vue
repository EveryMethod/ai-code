<template>
  <div id="userLoginPage">
    <h2 class="title">鱼皮 AI 应用生成 - 用户登录</h2>
    <div class="desc">不写一行代码，生成完整应用</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码长度不能小于 8 位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>
      <div class="tips">
        还没有账号？
        <RouterLink to="/user/register">去注册</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" :loading="submitting" style="width: 100%">
          登录
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { userLogin } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { reactive, ref } from 'vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const formState = reactive<API.UserLoginRequestDTO>({
  userAccount: '',
  userPassword: '',
})
const submitting = ref(false)

const handleSubmit = async (values: API.UserLoginRequestDTO) => {
  submitting.value = true
  try {
    const res = await userLogin(values)
    if (res.data.code === 0) {
      await loginUserStore.fetchLoginUser()
      message.success('登录成功')
      await router.push('/')
    } else {
      message.error('登录失败，' + (res.data.message ?? '未知错误'))
    }
  } catch (error: any) {
    message.error('登录请求失败，请检查后端服务或网络')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
#userLoginPage {
  max-width: 360px;
  margin: 0 auto;
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
