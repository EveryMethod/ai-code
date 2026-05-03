<template>
  <div id="appManagePage">
    <h2 class="page-title">应用管理</h2>
    <!-- 搜索表单 -->
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="应用名称">
          <a-input v-model:value="searchParams.appName" allow-clear placeholder="输入应用名称" />
        </a-form-item>
        <a-form-item label="代码类型">
          <a-input v-model:value="searchParams.codeGenType" allow-clear placeholder="输入类型" />
        </a-form-item>
        <a-form-item label="优先级">
          <a-input-number
            v-model:value="searchParams.priority"
            placeholder="优先级"
            :min="0"
            style="width: 120px"
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
            <template v-if="column.dataIndex === 'cover'">
              <a-image
                v-if="record.cover"
                :src="record.cover"
                :width="80"
                :preview="{ mask: '预览' }"
                style="border-radius: 6px"
              />
              <span v-else style="color: #cbd5e1">—</span>
            </template>
            <template v-else-if="column.dataIndex === 'priority'">
              <a-tag v-if="(record.priority ?? 0) >= 99" color="purple">精选</a-tag>
              <a-tag v-else-if="(record.priority ?? 0) > 0" color="blue">{{
                record.priority
              }}</a-tag>
              <span v-else style="color: #94a3b8">0</span>
            </template>
            <template v-else-if="column.dataIndex === 'user'">
              <div class="user-cell">
                <a-avatar :size="28" :src="record.user?.userAvatar" />
                <span class="user-name">{{ record.user?.userName ?? '未知' }}</span>
              </div>
            </template>
            <template v-else-if="column.dataIndex === 'createTime'">
              <span class="time-text">{{
                dayjs(record.createTime).format('YYYY-MM-DD HH:mm')
              }}</span>
            </template>
            <template v-else-if="column.dataIndex === 'updateTime'">
              <span class="time-text">{{
                dayjs(record.updateTime).format('YYYY-MM-DD HH:mm')
              }}</span>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="doEdit(record.id)">编辑</a-button>
                <a-popconfirm
                  v-if="(record.priority ?? 0) < 99"
                  title="设为精选应用？"
                  ok-text="确定"
                  cancel-text="取消"
                  @confirm="doSetFeatured(record)"
                >
                  <a-button type="link" size="small">精选</a-button>
                </a-popconfirm>
                <a-popconfirm
                  title="确定删除该应用？此操作不可恢复"
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
          <a-empty description="暂无应用数据" />
        </div>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import { listAppVoByPageByAdmin, deleteAppByAdmin, updateAppByAdmin } from '@/api/appController.ts'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '应用名称',
    dataIndex: 'appName',
    ellipsis: true,
  },
  {
    title: '封面',
    dataIndex: 'cover',
    width: 140,
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    width: 100,
  },
  {
    title: '用户',
    dataIndex: 'user',
    width: 150,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180,
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
  },
]

// 数据
const data = ref<API.AppVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'desc',
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await listAppVoByPageByAdmin({
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
  searchParams.appName = undefined
  searchParams.codeGenType = undefined
  searchParams.priority = undefined
  fetchData()
}

// 编辑
const doEdit = (id?: string) => {
  if (!id) return
  router.push(`/app/edit/${id}`)
}

// 删除
const doDelete = async (id?: string) => {
  if (!id) return
  const res = await deleteAppByAdmin({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error('删除失败，' + (res.data.message ?? '未知错误'))
  }
}

// 设为精选
const doSetFeatured = async (record: API.AppVO) => {
  if (!record.id) return
  const res = await updateAppByAdmin({
    id: record.id,
    priority: 99,
  })
  if (res.data.code === 0) {
    message.success('已设为精选')
    fetchData()
  } else {
    message.error('设置失败，' + (res.data.message ?? '未知错误'))
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#appManagePage {
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

.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-size: 13px;
  color: #334155;
}

.time-text {
  font-size: 12px;
  color: #64748b;
}

.table-empty {
  padding: 48px 0;
}
</style>
