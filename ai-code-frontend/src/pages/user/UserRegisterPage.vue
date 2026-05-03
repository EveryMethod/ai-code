<template>
  <div id="userRegisterPage">
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
            { min: 8, message: '密码不能小于 8 位' },
          ]"
        >
          <a-input-password
            v-model:value="formState.userPassword"
            size="large"
            placeholder="请输入密码（至少8位）"
          >
            <template #prefix><LockOutlined /></template>
          </a-input-password>
          <div class="password-strength" v-if="formState.userPassword">
            <div class="strength-bar">
              <div
                class="strength-fill"
                :class="strengthClass"
                :style="{ width: strengthPercent + '%' }"
              ></div>
            </div>
            <span class="strength-label" :class="strengthClass">{{ strengthLabel }}</span>
          </div>
        </a-form-item>
        <a-form-item
          name="checkPassword"
          :rules="[
            { required: true, message: '请确认密码' },
            { min: 8, message: '密码不能小于 8 位' },
            { validator: validateCheckPassword },
          ]"
        >
          <a-input-password
            v-model:value="formState.checkPassword"
            size="large"
            placeholder="请确认密码"
          >
            <template #prefix><LockOutlined /></template>
          </a-input-password>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="submitting" size="large" block>
            注册
          </a-button>
        </a-form-item>
      </a-form>
      <div class="auth-footer">
        已有账号？<RouterLink to="/user/login" class="auth-link">去登录</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { userRegister } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { computed, reactive, ref } from 'vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'

const router = useRouter()
const submitting = ref(false)

const formState = reactive<API.UserRegisterRequestDTO>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

// 密码强度计算
const calcStrength = (pwd: string): { score: number; label: string; cls: string } => {
  if (!pwd || pwd.length < 8) return { score: 0, label: '太短', cls: 'strength-weak' }
  let score = 0
  if (pwd.length >= 10) score++
  if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) score++
  if (/\d/.test(pwd)) score++
  if (/[^a-zA-Z0-9]/.test(pwd)) score++
  if (score <= 1) return { score: 25, label: '弱', cls: 'strength-weak' }
  if (score === 2) return { score: 50, label: '中等', cls: 'strength-medium' }
  if (score === 3) return { score: 75, label: '较强', cls: 'strength-good' }
  return { score: 100, label: '强', cls: 'strength-great' }
}

const strengthInfo = computed(() => calcStrength(formState.userPassword))
const strengthPercent = computed(() => strengthInfo.value.score)
const strengthLabel = computed(() => strengthInfo.value.label)
const strengthClass = computed(() => strengthInfo.value.cls)

/**
 * 验证确认密码
 * @param rule
 * @param value
 * @param callback
 */
const validateCheckPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value && value !== formState.userPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.UserRegisterRequestDTO) => {
  submitting.value = true
  try {
    const res = await userRegister(values)
    if (res.data.code === 0) {
      message.success('注册成功')
      router.push({
        path: '/user/login',
        replace: true,
      })
    } else {
      message.error('注册失败，' + res.data.message)
    }
  } catch {
    message.error('注册请求失败，请检查后端服务或网络')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
#userRegisterPage {
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

.password-strength {
  margin-top: 6px;
}

.strength-bar {
  height: 4px;
  background: #e2e8f0;
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 4px;
}

.strength-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.3s ease;
}

.strength-weak {
  background: #ef4444;
  color: #ef4444;
}

.strength-medium {
  background: #f59e0b;
  color: #f59e0b;
}

.strength-good {
  background: #3b82f6;
  color: #3b82f6;
}

.strength-great {
  background: #22c55e;
  color: #22c55e;
}

.strength-label {
  font-size: 12px;
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
