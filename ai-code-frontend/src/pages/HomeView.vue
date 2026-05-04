<template>
  <div class="home">
    <section class="home-hero">
      <div class="hero-inner">
        <h1 class="hero-title">AI 应用生成平台</h1>
        <p class="hero-sub">一句话轻松创建网站应用</p>

        <div class="prompt-card">
          <a-textarea
            v-model:value="initPrompt"
            :rows="3"
            :maxlength="2000"
            class="prompt-input"
            placeholder="帮我创建个人博客网站"
            :bordered="false"
            @pressEnter="quickCreate"
          />
          <div class="prompt-toolbar">
            <a-button
              type="primary"
              shape="circle"
              class="send-btn"
              :loading="creating"
              @click="onCreateApp"
            >
              <ArrowUpOutlined />
            </a-button>
          </div>
        </div>

        <div class="suggest-tags">
          <a-button v-for="t in suggestTags" :key="t" class="tag-btn" @click="initPrompt = t">
            {{ t }}
          </a-button>
        </div>
      </div>
    </section>

    <section class="home-panel">
      <div class="panel-inner">
        <h2 class="section-title">我的作品</h2>
        <div v-if="!loginUserStore.loginUser?.id" class="need-login">
          <a-empty description="登录后查看我的应用">
            <a-button type="primary" href="/user/login">去登录</a-button>
          </a-empty>
        </div>
        <template v-else>
          <a-form layout="inline" class="search-row" :model="mySearch" @finish="reloadMyApps">
            <a-form-item label="名称">
              <a-input v-model:value="mySearch.appName" allow-clear placeholder="按应用名称搜索" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" html-type="submit">搜索</a-button>
            </a-form-item>
          </a-form>
          <a-spin :spinning="myLoading" tip="加载中…">
            <div v-if="myApps.length" class="card-grid">
              <a-card
                v-for="item in myApps"
                :key="item.id"
                class="app-card"
                hoverable
                @click="goChat(String(item.id!), item)"
              >
                <template #cover>
                  <div class="thumb-wrap">
                    <img
                      class="thumb"
                      :src="item.cover || placeholderCover"
                      alt=""
                      loading="lazy"
                    />
                    <!-- 悬浮操作按钮 -->
                    <div class="card-overlay">
                      <a-button
                        v-if="item.deployKey"
                        type="primary"
                        size="large"
                        class="overlay-btn overlay-btn--view"
                        @click.stop="openDeployedApp(item.deployKey)"
                      >
                        查看作品
                      </a-button>
                      <a-button
                        type="primary"
                        size="large"
                        class="overlay-btn overlay-btn--chat"
                        @click.stop="goChat(String(item.id!), item)"
                      >
                        查看对话
                      </a-button>
                    </div>
                  </div>
                </template>
                <!-- 应用信息区域：左右结构 -->
                <div class="app-info-row">
                  <!-- 左侧：用户头像 -->
                  <div class="app-info-left">
                    <a-avatar :size="40" :src="item.user?.userAvatar" shape="circle" />
                  </div>
                  <!-- 右侧：上下结构 -->
                  <div class="app-info-right">
                    <!-- 上方：应用标题 -->
                    <div class="app-title">{{ item.appName || '未命名应用' }}</div>
                    <!-- 下方：用户昵称 -->
                    <div class="app-creator">{{ item.user?.userName ?? '用户' }}</div>
                  </div>
                </div>
              </a-card>
            </div>
            <div v-else-if="!myLoading" class="empty-block">
              <a-empty description="暂无应用，在上方输入想法开始创建" />
            </div>
            <div v-if="myLoadError" class="error-block">
              <a-result status="warning" title="加载失败">
                <template #extra>
                  <a-button type="primary" @click="fetchMyApps">重试</a-button>
                </template>
              </a-result>
            </div>
          </a-spin>
          <div v-if="myTotal > 0" class="pager">
            <a-pagination
              v-model:current="mySearch.pageNum"
              v-model:page-size="mySearch.pageSize"
              :total="myTotal"
              :page-size-options="['6']"
              :show-total="formatPageTotal"
              @change="onMyPageChange"
            />
          </div>
        </template>
      </div>
    </section>

    <section class="home-panel home-panel--last">
      <div class="panel-inner">
        <h2 class="section-title">精选案例</h2>
        <a-form layout="inline" class="search-row" :model="goodSearch" @finish="reloadGoodApps">
          <a-form-item label="名称">
            <a-input v-model:value="goodSearch.appName" allow-clear placeholder="按应用名称搜索" />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" html-type="submit">搜索</a-button>
          </a-form-item>
        </a-form>
        <a-spin :spinning="goodLoading" tip="加载中…">
          <div v-if="goodApps.length" class="card-grid">
            <a-card
              v-for="item in goodApps"
              :key="item.id"
              class="app-card"
              hoverable
              @click="goChat(String(item.id!), item)"
            >
              <template #cover>
                <div class="thumb-wrap">
                  <img class="thumb" :src="item.cover || placeholderCover" alt="" loading="lazy" />
                  <!-- 悬浮操作按钮 -->
                  <div class="card-overlay">
                    <a-button
                      v-if="item.deployKey"
                      type="primary"
                      size="large"
                      class="overlay-btn overlay-btn--view"
                      @click.stop="openDeployedApp(item.deployKey)"
                    >
                      查看作品
                    </a-button>
                    <a-button
                      type="primary"
                      size="large"
                      class="overlay-btn overlay-btn--chat"
                      @click.stop="goChat(String(item.id!), item)"
                    >
                      查看对话
                    </a-button>
                  </div>
                </div>
              </template>
              <!-- 应用信息区域：左右结构 -->
              <div class="app-info-row">
                <!-- 左侧：用户头像 -->
                <div class="app-info-left">
                  <a-avatar :size="40" :src="item.user?.userAvatar" shape="circle" />
                </div>
                <!-- 右侧：上下结构 -->
                <div class="app-info-right">
                  <!-- 上方：应用标题 -->
                  <div class="app-title">{{ item.appName || '未命名应用' }}</div>
                  <!-- 下方：用户昵称 -->
                  <div class="app-creator">{{ item.user?.userName ?? '用户' }}</div>
                </div>
              </div>
            </a-card>
          </div>
          <div v-else-if="!goodLoading" class="empty-block">
            <a-empty description="暂无精选应用" />
          </div>
          <div v-if="goodLoadError" class="error-block">
            <a-result status="warning" title="精选加载失败">
              <template #extra>
                <a-button type="primary" @click="fetchGoodApps">重试</a-button>
              </template>
            </a-result>
          </div>
        </a-spin>
        <div v-if="goodTotal > 0" class="pager">
          <a-pagination
            v-model:current="goodSearch.pageNum"
            v-model:page-size="goodSearch.pageSize"
            :total="goodTotal"
            :page-size-options="['12', '24']"
            :show-total="formatPageTotal"
            @change="onGoodPageChange"
          />
        </div>
      </div>
    </section>

    <div class="home-footer-note">
      <a href="https://www.codefather.cn" target="_blank" rel="noopener noreferrer">
        编程导航 codefather.cn
      </a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowUpOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { addApp, listGoodAppVoByPage, listMyAppVoByPage } from '@/api/appController.ts'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const initPrompt = ref('')
const creating = ref(false)
const myLoading = ref(false)
const goodLoading = ref(false)
const myLoadError = ref(false)
const goodLoadError = ref(false)
const myApps = ref<API.AppVO[]>([])
const goodApps = ref<API.AppVO[]>([])
const myTotal = ref(0)
const goodTotal = ref(0)

const placeholderCover = 'https://gw.alipayobjects.com/zos/rmsportal/JiqGstEfoWAOHiTxclqi.png'

const suggestTags = [
  '帮我创建一个响应式电商网站首页，包含商品轮播图、分类导航栏、热销商品展示区域、购物车图标和页脚信息栏，风格简约现代',
  '帮我创建个人技术博客网站，包含文章列表页和文章详情页，支持代码高亮显示、标签分类、文章搜索功能和关于我页面',
  '帮我创建一个企业官网展示网站，包含首页Banner大图、公司介绍、服务项目展示、成功案例轮播、团队介绍和在线联系表单',
  '帮我创建一个在线任务管理看板应用，包含待办/进行中/已完成三列看板、任务增删改查、优先级标签、截止日期和搜索筛选功能',
]

const mySearch = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 6, // 2行×3个
  appName: undefined,
  sortField: 'create_time',
  sortOrder: 'desc',
})

const goodSearch = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 12,
  appName: undefined,
})

function clampPageSize(body: API.AppQueryRequest) {
  const size = Math.min(body.pageSize ?? 20, 20)
  body.pageSize = size
}

function quickCreate(e: KeyboardEvent) {
  if (!e.shiftKey) {
    e.preventDefault()
    onCreateApp()
  }
}

async function onCreateApp() {
  const text = initPrompt.value.trim()
  if (!text) {
    message.warning('请输入创建应用的描述')
    return
  }
  if (!loginUserStore.loginUser?.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }
  creating.value = true
  try {
    const res = await addApp({ initPrompt: text })
    if (res.data.code === 0 && res.data.data != null) {
      const id = res.data.data
      message.success('应用已创建')
      initPrompt.value = ''
      await router.push({ path: `/app/chat/${id}`, query: { auto: '1' } })
    } else {
      message.error(res.data.message ?? '创建失败')
    }
  } catch {
    message.error('创建失败，请检查网络或登录状态')
  } finally {
    creating.value = false
  }
}

async function fetchMyApps() {
  if (!loginUserStore.loginUser?.id) return
  myLoading.value = true
  myLoadError.value = false
  try {
    clampPageSize(mySearch)
    const res = await listMyAppVoByPage({ ...mySearch })
    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records ?? []
      myTotal.value = res.data.data.totalRow ?? 0
    } else {
      myApps.value = []
      myTotal.value = 0
      message.error(res.data.message ?? '加载我的应用失败')
    }
  } catch {
    myApps.value = []
    myTotal.value = 0
    myLoadError.value = true
    message.error('加载我的应用失败')
  } finally {
    myLoading.value = false
  }
}

async function fetchGoodApps() {
  goodLoading.value = true
  goodLoadError.value = false
  try {
    clampPageSize(goodSearch)
    const res = await listGoodAppVoByPage({ ...goodSearch })
    if (res.data.code === 0 && res.data.data) {
      goodApps.value = res.data.data.records ?? []
      goodTotal.value = res.data.data.totalRow ?? 0
    } else {
      goodApps.value = []
      goodTotal.value = 0
      message.error(res.data.message ?? '加载精选应用失败')
    }
  } catch {
    goodApps.value = []
    goodTotal.value = 0
    goodLoadError.value = true
    message.error('加载精选应用失败')
  } finally {
    goodLoading.value = false
  }
}

function reloadMyApps() {
  mySearch.pageNum = 1
  fetchMyApps()
}

function reloadGoodApps() {
  goodSearch.pageNum = 1
  fetchGoodApps()
}

const formatPageTotal = (total: number) => `共 ${total} 条`

function onMyPageChange(page: number, pageSize: number) {
  mySearch.pageNum = page
  mySearch.pageSize = pageSize
  fetchMyApps()
}

function onGoodPageChange(page: number, pageSize: number) {
  goodSearch.pageNum = page
  goodSearch.pageSize = pageSize
  fetchGoodApps()
}

function goChat(id: string, item?: API.AppVO) {
  if (!loginUserStore.loginUser?.id) {
    message.warning('请先登录后查看应用')
    router.push('/user/login')
    return
  }

  // 判断是否是自己的作品
  const isOwner = item?.userId === loginUserStore.loginUser.id
  const isAdmin = loginUserStore.loginUser.userRole === 'admin'

  // 如果不是自己的作品且不是管理员，添加 view=1 参数
  if (!isOwner && !isAdmin) {
    router.push({ path: `/app/chat/${id}`, query: { view: '1' } })
  } else {
    router.push(`/app/chat/${id}`)
  }
}

function openDeployedApp(deployKey: string) {
  // 部署地址格式：localhost/{deployKey}
  window.open(`/${deployKey}`, '_blank')
}

watch(
  () => loginUserStore.loginUser?.id,
  (id) => {
    if (id) {
      mySearch.pageNum = 1
      fetchMyApps()
    } else {
      myApps.value = []
      myTotal.value = 0
    }
  },
)

onMounted(() => {
  fetchGoodApps()
  if (loginUserStore.loginUser?.id) {
    fetchMyApps()
  }
})
</script>

<style scoped>
.home {
  min-height: 100vh;
  height: 100vh;
  display: flex;
  flex-direction: column;
  font-family:
    system-ui,
    -apple-system,
    'Segoe UI',
    Roboto,
    'PingFang SC',
    'Microsoft YaHei',
    sans-serif;
  overflow-y: auto; /* 允许垂直滚动 */
  overflow-x: hidden; /* 隐藏横向滚动 */
}

/* 隐藏滚动条但保留滚动功能 */
.home::-webkit-scrollbar {
  width: 0;
  height: 0;
  display: none;
}

.home {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE/Edge */
}

.home-hero {
  flex: 1;
  padding: 80px 24px 60px;
  /* 科技感深色渐变背景 */
  background: linear-gradient(
    135deg,
    #0a0e1a 0%,
    #0f1830 25%,
    #151b45 50%,
    #0c1f30 75%,
    #080d1a 100%
  );
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-inner {
  width: 100%;
  max-width: none;
  margin: 0 auto;
  text-align: center;
}

.hero-title {
  margin: 0 0 16px;
  font-size: clamp(36px, 5.5vw, 56px);
  font-weight: 800;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #7dd3fc 0%, #38bdf8 40%, #818cf8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-sub {
  margin: 0 0 36px;
  color: #8b9bb5;
  font-size: 18px;
  font-weight: 300;
  letter-spacing: 1px;
}

.prompt-card {
  /* 毛玻璃效果卡片 */
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 18px 20px 12px;
  text-align: left;
  transition: all 0.3s ease;
  max-width: 720px;
  margin: 0 auto;
}

.prompt-card:hover {
  border-color: rgba(56, 189, 248, 0.3);
  box-shadow: 0 0 30px rgba(56, 189, 248, 0.08);
}

.prompt-input {
  font-size: 16px;
  resize: none !important;
  line-height: 1.6;
}

.prompt-input :deep(textarea) {
  color: #e2e8f0 !important;
  background: transparent !important;
}

.prompt-input :deep(textarea::placeholder) {
  color: #64748b !important;
}

.prompt-toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-top: 8px;
}

.send-btn {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #38bdf8, #818cf8) !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(56, 189, 248, 0.35);
  transition: all 0.3s ease;
}

.send-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(56, 189, 248, 0.5);
}

.send-btn:active {
  transform: translateY(0);
}

.suggest-tags {
  margin-top: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.tag-btn {
  border-radius: 999px !important;
  background: rgba(255, 255, 255, 0.06) !important;
  border: 1px solid rgba(255, 255, 255, 0.12) !important;
  color: #94a3b8 !important;
  font-size: 13px;
  padding: 6px 18px;
  transition: all 0.25s ease;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 260px;
}

.tag-btn:hover {
  background: rgba(56, 189, 248, 0.12) !important;
  border-color: rgba(56, 189, 248, 0.35) !important;
  color: #38bdf8 !important;
}

.home-panel {
  padding: 0 0 16px;
  background: linear-gradient(180deg, #080d1a 0%, #0f1830 40%, #0a0e1a 100%);
}

.home-panel--last {
  padding-bottom: 48px;
}

.panel-inner {
  width: 100%;
  max-width: none;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 28px 28px 0 0;
  padding: 36px 48px 28px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.section-title {
  margin: 0 0 24px;
  font-size: 26px;
  font-weight: 700;
  color: #e2e8f0;
  letter-spacing: -0.3px;
}

.search-row {
  margin-bottom: 16px;
}

.search-row :deep(.ant-form-item-label > label) {
  color: #94a3b8;
}

.search-row :deep(.ant-input) {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.1);
  color: #e2e8f0;
}

.search-row :deep(.ant-input:hover),
.search-row :deep(.ant-input:focus) {
  border-color: rgba(56, 189, 248, 0.4);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 28px;
}

@media (max-width: 1200px) {
  .card-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .card-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }
}

@media (max-width: 576px) {
  .card-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}

.app-card {
  border-radius: 18px !important;
  overflow: hidden;
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
  cursor: pointer;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.03) !important;
}

.app-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.4);
  border-color: rgba(56, 189, 248, 0.25);
}

.thumb-wrap {
  position: relative;
  height: 200px;
  background: linear-gradient(135deg, #0f1830 0%, #151b45 100%);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 悬浮操作按钮 */
.card-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: rgba(0, 0, 0, 0.6);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 10;
}

.app-card:hover .card-overlay {
  opacity: 1;
}

.overlay-btn {
  min-width: 160px !important;
  border-radius: 999px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  padding: 12px 24px !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2) !important;
  transition: all 0.3s ease !important;
  border: 2px solid #fff !important;
}

.overlay-btn:hover {
  transform: scale(1.05) !important;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.3) !important;
}

/* 查看作品按钮 - 黑色 */
.overlay-btn--view {
  background: #1a1a1a !important;
  color: #fff !important;
  border-color: #1a1a1a !important;
}

.overlay-btn--view:hover {
  background: #333 !important;
  border-color: #333 !important;
}

/* 查看对话按钮 - 白色 */
.overlay-btn--chat {
  background: #fff !important;
  color: #1a1a1a !important;
}

.overlay-btn--chat:hover {
  background: #f5f5f5 !important;
}

.thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 应用信息区域：左右结构 */
.app-info-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
}

/* 左侧：用户头像 */
.app-info-left {
  flex-shrink: 0;
}

/* 右侧：上下结构 */
.app-info-right {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.app-title {
  font-size: 16px;
  font-weight: 600;
  color: #e2e8f0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

/* 下方：用户昵称 */
.app-creator {
  font-size: 14px;
  color: #7c8aa0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.empty-block {
  padding: 32px 0;
}

.error-block {
  padding: 16px 0;
}

.pager {
  margin-top: 32px;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 分页组件样式优化 */
.pager :deep(.ant-pagination) {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pager :deep(.ant-pagination-item) {
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.04);
  transition: all 0.3s ease;
}

.pager :deep(.ant-pagination-item a) {
  color: #94a3b8;
}

.pager :deep(.ant-pagination-item:hover) {
  border-color: #38bdf8;
}

.pager :deep(.ant-pagination-item:hover a) {
  color: #38bdf8;
}

.pager :deep(.ant-pagination-item-active) {
  border-color: #38bdf8;
  background: #38bdf8;
}

.pager :deep(.ant-pagination-item-active a) {
  color: #fff;
  font-weight: 600;
}

.pager :deep(.ant-pagination-prev),
.pager :deep(.ant-pagination-next) {
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.04);
  transition: all 0.3s ease;
}

.pager :deep(.ant-pagination-prev .ant-pagination-item-link),
.pager :deep(.ant-pagination-next .ant-pagination-item-link) {
  color: #94a3b8;
}

.pager :deep(.ant-pagination-prev:hover),
.pager :deep(.ant-pagination-next:hover) {
  border-color: #38bdf8;
}

.pager :deep(.ant-pagination-prev:hover .ant-pagination-item-link),
.pager :deep(.ant-pagination-next:hover .ant-pagination-item-link) {
  color: #38bdf8;
}

.pager :deep(.ant-pagination-prev .ant-pagination-item-link),
.pager :deep(.ant-pagination-next .ant-pagination-item-link) {
  border-radius: 8px;
}

.pager :deep(.ant-pagination-options) {
  margin-left: 16px;
}

.pager :deep(.ant-pagination-options-quick-jumper) {
  margin-left: 8px;
}

.pager :deep(.ant-pagination-options-quick-jumper input) {
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.04);
  color: #e2e8f0;
  transition: all 0.3s ease;
}

.pager :deep(.ant-pagination-options-quick-jumper input:hover),
.pager :deep(.ant-pagination-options-quick-jumper input:focus) {
  border-color: #38bdf8;
  box-shadow: 0 0 0 2px rgba(56, 189, 248, 0.15);
}

.pager :deep(.ant-pagination-total-text) {
  color: #4a5568;
  font-size: 14px;
  margin-right: 16px;
}

.pager :deep(.ant-select-selector) {
  background: rgba(255, 255, 255, 0.04) !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
  color: #94a3b8 !important;
}

.need-login {
  padding: 24px 0;
}

.home-footer-note {
  text-align: center;
  padding: 16px 24px;
  font-size: 13px;
  color: #4a5568;
  background: #080d1a;
  flex-shrink: 0;
}

.home-footer-note a {
  color: #4a5568;
}
</style>
