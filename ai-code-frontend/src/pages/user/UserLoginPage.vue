<template>
  <div id="userLoginPage">
    <div class="auth-header">
      <img class="auth-logo" src="@/assets/logo.png" alt="" />
      <h2 class="title">AI 应用生成</h2>
      <p class="desc">不写一行代码，生成完整应用</p>
    </div>
    <div class="auth-card">
      <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
        <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
          <a-input v-model:value="formState.userAccount" size="large" placeholder="请输入账号">
            <template #prefix><UserOutlined /></template>
          </a-input>
        </a-form-item>
        <a-form-item
          name="userPassword"
          :rules="[
            { required: true, message: '请输入密码' },
            { min: 8, message: '密码长度不能小于 8 位' },
          ]"
        >
          <a-input-password
            v-model:value="formState.userPassword"
            size="large"
            placeholder="请输入密码"
          >
            <template #prefix><LockOutlined /></template>
          </a-input-password>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="submitting" size="large" block>
            登录
          </a-button>
        </a-form-item>
      </a-form>
      <div class="auth-footer">
        还没有账号？<RouterLink to="/user/register" class="auth-link">去注册</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { userLogin } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { reactive, ref } from 'vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
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
      const raw = route.query.redirect
      let redirect = ''
      if (typeof raw === 'string') {
        try {
          redirect = decodeURIComponent(raw)
        } catch {
          redirect = raw
        }
      }
      if (redirect.startsWith('/') && !redirect.startsWith('//')) {
        await router.push(redirect)
      } else {
        await router.push('/')
      }
    } else {
      message.error('登录失败，' + (res.data.message ?? '未知错误'))
    }
  } catch {
    message.error('登录请求失败，请检查后端服务或网络')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
#userLoginPage {
  max-width: 400px;
  margin: 40px auto 0;
  padding: 0 16px;
}

.auth-header {
  text-align: center;
  margin-bottom: 28px;
}

.auth-logo {
  width: 56px;
  height: 56px;
  margin-bottom: 12px;
}

.title {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.desc {
  margin: 0;
  color: #94a3b8;
  font-size: 14px;
}

.auth-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px 28px;
  box-shadow: 0 4px 24px rgba(15, 23, 42, 0.06);
}

.auth-footer {
  text-align: center;
  font-size: 14px;
  color: #94a3b8;
}

.auth-link {
  color: #1890ff;
  font-weight: 500;
}
</style>
