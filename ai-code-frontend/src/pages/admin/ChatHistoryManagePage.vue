<template>
  <div id="chatHistoryManagePage">
    <h2 class="page-title">对话管理</h2>
    <!-- 搜索表单 -->
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="消息内容">
          <a-input v-model:value="searchParams.message" allow-clear placeholder="输入消息关键词" />
        </a-form-item>
        <a-form-item label="消息类型">
          <a-select
            v-model:value="searchParams.messageType"
            allow-clear
            placeholder="选择类型"
            style="width: 130px"
          >
            <a-select-option value="user">用户</a-select-option>
            <a-select-option value="assistant">AI</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="应用 ID">
          <a-input
            v-model:value="searchParams.appId"
            allow-clear
            placeholder="应用 ID"
            style="width: 130px"
          />
        </a-form-item>
        <a-form-item label="用户 ID">
          <a-input
            v-model:value="searchParams.userId"
            allow-clear
            placeholder="用户 ID"
            style="width: 130px"
          />
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

    <!-- 表格 -->
    <a-card class="table-card" :bordered="false">
      <a-spin :spinning="loading" tip="加载中…">
        <a-table
          :columns="columns"
          :data-source="data"
          :pagination="pagination"
          :scroll="{ x: 1000 }"
          @change="doTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'message'">
              <span class="msg-text">{{ truncateText(record.message, 60) }}</span>
            </template>
            <template v-else-if="column.dataIndex === 'messageType'">
              <a-tag v-if="record.messageType === 'user'" color="blue">用户</a-tag>
              <a-tag v-else-if="record.messageType === 'assistant'" color="green">AI</a-tag>
              <span v-else style="color: #94a3b8">{{ record.messageType }}</span>
            </template>
            <template v-else-if="column.dataIndex === 'createTime'">
              <span class="time-text">{{
                dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss')
              }}</span>
            </template>
          </template>
        </a-table>
        <div v-if="!loading && data.length === 0" class="table-empty">
          <a-empty description="暂无对话数据" />
        </div>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import { listAllChatHistoryByPageForAdmin } from '@/api/chatHistoryController.ts'
import dayjs from 'dayjs'

const loading = ref(false)

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '消息内容',
    dataIndex: 'message',
    ellipsis: true,
  },
  {
    title: '消息类型',
    dataIndex: 'messageType',
    width: 100,
  },
  {
    title: '应用 ID',
    dataIndex: 'appId',
    width: 90,
  },
  {
    title: '用户 ID',
    dataIndex: 'userId',
    width: 90,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180,
  },
]

// 数据
const data = ref<API.ChatHistory[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.ChatHistoryQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  sortField: 'create_time',
  sortOrder: 'desc',
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await listAllChatHistoryByPageForAdmin({
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

// 截断文本
function truncateText(text?: string, maxLen = 60): string {
  if (!text) return '—'
  return text.length > maxLen ? text.slice(0, maxLen) + '…' : text
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
  searchParams.message = undefined
  searchParams.messageType = undefined
  searchParams.appId = undefined
  searchParams.userId = undefined
  fetchData()
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#chatHistoryManagePage {
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

.msg-text {
  font-size: 13px;
  color: #334155;
  word-break: break-all;
}

.time-text {
  font-size: 12px;
  color: #64748b;
}

.table-empty {
  padding: 48px 0;
}
</style>
