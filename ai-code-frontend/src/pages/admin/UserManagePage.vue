<template>
  <div id="userManagePage">
    <h2 class="page-title">用户管理</h2>
    <!-- 搜索表单 -->
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="账号">
          <a-input v-model:value="searchParams.userAccount" allow-clear placeholder="输入账号" />
        </a-form-item>
        <a-form-item label="用户名">
          <a-input v-model:value="searchParams.userName" allow-clear placeholder="输入用户名" />
        </a-form-item>
        <a-form-item label="角色">
          <a-select
            v-model:value="searchParams.userRole"
            allow-clear
            placeholder="选择角色"
            style="width: 140px"
          >
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="user">普通用户</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit">
              <template #icon><SearchOutlined /></template>搜索
            </a-button>
            <a-button @click="doReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 操作栏 -->
    <div class="action-bar">
      <a-button type="primary" @click="doAdd">
        <template #icon><PlusOutlined /></template>新增用户
      </a-button>
    </div>

    <!-- 表格 -->
    <a-card class="table-card" :bordered="false">
      <a-spin :spinning="loading" tip="加载中…">
        <a-table
          :columns="columns"
          :data-source="data"
          :pagination="pagination"
          :scroll="{ x: 800 }"
          @change="doTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'userAvatar'">
              <a-avatar :size="40" :src="record.userAvatar" shape="square" />
            </template>
            <template v-else-if="column.dataIndex === 'userRole'">
              <a-tag v-if="record.userRole === 'admin'" color="red">管理员</a-tag>
              <a-tag v-else color="blue">普通用户</a-tag>
            </template>
            <template v-else-if="column.dataIndex === 'createTime'">
              <span class="time-text">{{
                dayjs(record.createTime).format('YYYY-MM-DD HH:mm')
              }}</span>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="doEdit(record)">编辑</a-button>
                <a-popconfirm
                  title="确定删除该用户？此操作不可恢复"
                  ok-text="确定删除"
                  cancel-text="取消"
                  ok-type="danger"
                  @confirm="doDelete(record.id)"
                >
                  <a-button type="link" size="small" danger>删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
        <div v-if="!loading && data.length === 0" class="table-empty">
          <a-empty description="暂无用户数据" />
        </div>
      </a-spin>
    </a-card>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :confirm-loading="modalSubmitting"
      @ok="onModalOk"
    >
      <a-form :model="modalForm" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="账号">
          <a-input
            v-model:value="modalForm.userAccount"
            placeholder="请输入账号"
            :disabled="editingId !== ''"
          />
        </a-form-item>
        <a-form-item label="用户名">
          <a-input v-model:value="modalForm.userName" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="头像URL">
          <a-input v-model:value="modalForm.userAvatar" placeholder="头像URL（可选）" />
        </a-form-item>
        <a-form-item label="简介">
          <a-textarea
            v-model:value="modalForm.userProfile"
            placeholder="用户简介（可选）"
            :rows="2"
          />
        </a-form-item>
        <a-form-item label="角色">
          <a-select v-model:value="modalForm.userRole" placeholder="选择角色">
            <a-select-option value="user">普通用户</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item v-if="editingId === ''" label="密码">
          <a-input-password v-model:value="modalPassword" placeholder="请输入密码（至少8位）" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { listUserVoByPage, deleteUser, updateUser, save } from '@/api/userController.ts'
import dayjs from 'dayjs'

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 70,
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
    ellipsis: true,
  },
  {
    title: '用户名',
    dataIndex: 'userName',
    ellipsis: true,
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
    width: 80,
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
    ellipsis: true,
  },
  {
    title: '角色',
    dataIndex: 'userRole',
    width: 100,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 150,
  },
  {
    title: '操作',
    key: 'action',
    width: 140,
  },
]

// 数据
const data = ref<API.UserVO[]>([])
const total = ref(0)
const loading = ref(false)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 弹窗
const modalVisible = ref(false)
const modalTitle = ref('新增用户')
const modalSubmitting = ref(false)
const editingId = ref<string>('')
const modalPassword = ref('')
const modalForm = reactive<API.UserAddRequest>({
  userName: '',
  userAccount: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user',
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await listUserVoByPage({
      ...searchParams,
    })
    if (res.data.code === 0 && res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
      return
    }
    data.value = []
    total.value = 0
    message.error('获取数据失败，' + (res.data.message ?? '未知错误'))
  } catch {
    data.value = []
    total.value = 0
    message.error('获取数据失败，请检查后端服务或登录状态')
  } finally {
    loading.value = false
  }
}

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格变化处理
const doTableChange = (page: { current?: number; pageSize?: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索
const doSearch = () => {
  searchParams.pageNum = 1
  fetchData()
}

// 重置
const doReset = () => {
  searchParams.pageNum = 1
  searchParams.userAccount = undefined
  searchParams.userName = undefined
  searchParams.userRole = undefined
  fetchData()
}

// 删除数据
const doDelete = async (id?: string) => {
  if (!id) return
  const res = await deleteUser({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error('删除失败，' + res.data.message)
  }
}

// 新增
const doAdd = () => {
  editingId.value = ''
  modalTitle.value = '新增用户'
  modalPassword.value = ''
  modalForm.userName = ''
  modalForm.userAccount = ''
  modalForm.userAvatar = ''
  modalForm.userProfile = ''
  modalForm.userRole = 'user'
  modalVisible.value = true
}

// 编辑
const doEdit = (record: API.UserVO) => {
  editingId.value = record.id ?? ''
  modalTitle.value = '编辑用户'
  modalPassword.value = ''
  modalForm.userName = record.userName ?? ''
  modalForm.userAccount = record.userAccount ?? ''
  modalForm.userAvatar = record.userAvatar ?? ''
  modalForm.userProfile = record.userProfile ?? ''
  modalForm.userRole = record.userRole ?? 'user'
  modalVisible.value = true
}

// 弹窗确认
const onModalOk = async () => {
  if (!modalForm.userAccount?.trim()) {
    message.warning('请输入账号')
    return
  }
  if (editingId.value === '' && modalPassword.value.length < 8) {
    message.warning('密码至少8位')
    return
  }
  modalSubmitting.value = true
  try {
    if (editingId.value !== '') {
      const res = await updateUser({
        id: editingId.value,
        userName: modalForm.userName,
        userAvatar: modalForm.userAvatar || undefined,
        userProfile: modalForm.userProfile || undefined,
        userRole: modalForm.userRole,
      })
      if (res.data.code === 0) {
        message.success('更新成功')
        modalVisible.value = false
        fetchData()
      } else {
        message.error(res.data.message ?? '更新失败')
      }
    } else {
      const res = await save({
        userName: modalForm.userName,
        userAccount: modalForm.userAccount,
        userPassword: modalPassword.value,
        userAvatar: modalForm.userAvatar || undefined,
        userProfile: modalForm.userProfile || undefined,
        userRole: modalForm.userRole,
      })
      if (res.data) {
        message.success('新增成功')
        modalVisible.value = false
        fetchData()
      } else {
        message.error('新增失败')
      }
    }
  } catch {
    message.error('操作失败')
  } finally {
    modalSubmitting.value = false
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#userManagePage {
  width: 100%;
}

.page-title {
  margin: 0 0 16px;
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.search-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.action-bar {
  margin-bottom: 16px;
  display: flex;
  gap: 8px;
}

.table-card {
  border-radius: 12px;
}

.table-card :deep(.ant-table) {
  font-size: 13px;
}

.table-card :deep(.ant-table-thead > tr > th) {
  background: #f8fafc;
  font-weight: 600;
  color: #475569;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.time-text {
  font-size: 12px;
  color: #64748b;
}

.table-empty {
  padding: 48px 0;
}
</style>
