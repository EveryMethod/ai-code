<template>
  <div id="appEditPage">
    <a-page-header title="编辑应用" @back="onCancel" />
    <a-row :gutter="24">
      <a-col :span="isAdmin ? 16 : 24">
        <a-card title="基本信息" :bordered="false">
          <a-spin :spinning="loadingApp">
            <a-form
              :model="formState"
              :rules="rules"
              :label-col="{ span: 4 }"
              :wrapper-col="{ span: 18 }"
              @finish="onSubmit"
            >
              <a-form-item label="应用名称" name="appName">
                <a-input
                  v-model:value="formState.appName"
                  placeholder="请输入应用名称"
                  :maxlength="100"
                  show-count
                />
              </a-form-item>

              <a-form-item label="应用封面" name="cover">
                <a-input v-model:value="formState.cover" placeholder="请输入封面图片URL" />
                <div v-if="formState.cover" class="cover-preview">
                  <a-image
                    :src="formState.cover"
                    :width="160"
                    :preview="{ mask: '预览' }"
                    style="border-radius: 8px"
                  />
                </div>
              </a-form-item>

              <a-form-item v-if="isAdmin" label="优先级" name="priority">
                <a-input-number
                  v-model:value="formState.priority"
                  :min="0"
                  :max="99"
                  placeholder="优先级（0-99）"
                  style="width: 200px"
                />
                <div class="form-hint">优先级 ≥ 99 的应用将显示在精选案例中</div>
              </a-form-item>

              <a-form-item :wrapper-col="{ span: 18, offset: 4 }">
                <a-space>
                  <a-button type="primary" html-type="submit" :loading="submitting" size="large">
                    保存修改
                  </a-button>
                  <a-button @click="onCancel" size="large">取消</a-button>
                </a-space>
              </a-form-item>
            </a-form>
          </a-spin>
        </a-card>
      </a-col>

      <a-col v-if="isAdmin && appInfo" :span="8">
        <a-card title="应用详情" :bordered="false" class="detail-card">
          <div class="detail-item">
            <span class="detail-label">ID</span>
            <span class="detail-value">{{ appInfo.id }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">代码类型</span>
            <span class="detail-value">{{ appInfo.codeGenType || '—' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">部署Key</span>
            <span class="detail-value" style="font-size: 12px; word-break: break-all">{{
              appInfo.deployKey || '—'
            }}</span>
          </div>
          <div v-if="appInfo.user" class="detail-item">
            <span class="detail-label">创建者</span>
            <span class="detail-value">
              <a-avatar :size="20" :src="appInfo.user.userAvatar" />
              {{ appInfo.user.userName }}
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">创建时间</span>
            <span class="detail-value">{{
              dayjs(appInfo.createTime).format('YYYY-MM-DD HH:mm')
            }}</span>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import {
  getAppVoById,
  getAppVoByIdByAdmin,
  updateApp,
  updateAppByAdmin,
} from '@/api/appController.ts'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = ref<string>('')
const appInfo = ref<API.AppVO>()
const submitting = ref(false)
const loadingApp = ref(false)

// 判断是否为管理员
const isAdmin = computed(() => loginUserStore.loginUser?.userRole === 'admin')

// 表单数据
const formState = reactive({
  appName: '',
  cover: '',
  priority: 0,
})

// 表单验证规则
const rules = {
  appName: [
    { required: true, message: '请输入应用名称', trigger: 'blur' },
    { min: 1, max: 100, message: '应用名称长度应在 1-100 之间', trigger: 'blur' },
  ],
  cover: [{ type: 'url', message: '请输入有效的URL地址', trigger: 'blur' }],
  priority: [{ type: 'number', min: 0, max: 99, message: '优先级应在 0-99 之间', trigger: 'blur' }],
}

// 加载应用信息
const loadAppInfo = async () => {
  const id = String(route.params.id)
  if (!id || id === 'undefined' || id === 'null') {
    message.error('无效的应用ID')
    await router.push('/')
    return
  }
  appId.value = id
  loadingApp.value = true

  try {
    // 根据用户角色选择不同的接口
    const res = isAdmin.value ? await getAppVoByIdByAdmin({ id }) : await getAppVoById({ id })

    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data
      // 填充表单
      formState.appName = res.data.data.appName || ''
      formState.cover = res.data.data.cover || ''
      formState.priority = res.data.data.priority ?? 0
    } else {
      message.error(res.data.message ?? '获取应用信息失败')
      await router.push('/')
    }
  } catch {
    message.error('获取应用信息失败，请检查网络或登录状态')
    await router.push('/')
  } finally {
    loadingApp.value = false
  }
}

// 提交表单
const onSubmit = async () => {
  if (!appId.value) {
    message.error('应用ID无效')
    return
  }

  submitting.value = true
  try {
    let res
    if (isAdmin.value) {
      // 管理员可以编辑所有字段
      res = await updateAppByAdmin({
        id: appId.value,
        appName: formState.appName,
        cover: formState.cover || undefined,
        priority: formState.priority,
      })
    } else {
      // 普通用户只能编辑应用名称
      res = await updateApp({
        id: appId.value,
        appName: formState.appName,
      })
    }

    if (res.data.code === 0) {
      message.success('保存成功')
      // 跳转回对话页
      await router.push(`/app/chat/${appId.value}`)
    } else {
      message.error(res.data.message ?? '保存失败')
    }
  } catch {
    message.error('保存失败，请检查网络或登录状态')
  } finally {
    submitting.value = false
  }
}

// 取消
const onCancel = () => {
  router.back()
}

// 页面加载时获取应用信息
onMounted(() => {
  loadAppInfo()
})
</script>

<style scoped>
#appEditPage {
  max-width: 960px;
  margin: 0 auto;
}

.cover-preview {
  margin-top: 8px;
}

.form-hint {
  color: #94a3b8;
  font-size: 12px;
  margin-top: 4px;
}

.detail-card {
  border-radius: 12px;
  position: sticky;
  top: 16px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f1f5f9;
  gap: 12px;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  font-size: 13px;
  color: #94a3b8;
  flex-shrink: 0;
}

.detail-value {
  font-size: 13px;
  color: #334155;
  text-align: right;
  display: flex;
  align-items: center;
  gap: 6px;
}
</style>
