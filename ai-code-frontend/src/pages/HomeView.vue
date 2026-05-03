<template>
  <div class="home">
    <section class="home-hero">
      <div class="hero-inner">
        <h1 class="hero-title">
          一句话
          <span class="hero-logo-wrap">
            <img class="hero-logo" src="@/assets/logo.png" alt="" />
          </span>
          呈所想
        </h1>
        <p class="hero-sub">与 AI 对话轻松创建应用和网站</p>

        <div class="prompt-card">
          <a-textarea
            v-model:value="initPrompt"
            :rows="3"
            :maxlength="2000"
            class="prompt-input"
            placeholder="使用 NoCode 创建一个高效的小工具，帮我计算……"
            :bordered="false"
            @pressEnter="quickCreate"
          />
          <div class="prompt-toolbar">
            <div class="prompt-toolbar-left">
              <a-button type="text" class="tool-btn" @click="onUploadClick">
                <template #icon><PaperClipOutlined /></template>
                上传
              </a-button>
              <a-button type="text" class="tool-btn" @click="onOptimizeClick">
                <template #icon><ThunderboltOutlined /></template>
                优化
              </a-button>
            </div>
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
                @click="goChat(item.id!)"
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
                        @click.stop="goChat(item.id!)"
                      >
                        查看对话
                      </a-button>
                    </div>
                  </div>
                </template>
                <a-card-meta :title="item.appName || '未命名应用'">
                  <template #description>
                    <span class="card-time">{{ relativeCreated(item.createTime) }}</span>
                  </template>
                </a-card-meta>
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
              :page-size-options="['10', '20']"
              show-size-changer
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
              @click="goChat(item.id!)"
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
                      @click.stop="goChat(item.id!)"
                    >
                      查看对话
                    </a-button>
                  </div>
                </div>
              </template>
              <a-card-meta :title="item.appName || '未命名应用'">
                <template #description>
                  <div class="featured-meta">
                    <span class="badges">
                      <a-tag v-if="(item.priority ?? 0) >= 99" color="purple">精选</a-tag>
                      <a-tag v-if="item.codeGenType" color="blue">{{ item.codeGenType }}</a-tag>
                      <a-tag color="orange">应用</a-tag>
                    </span>
                    <div class="creator">
                      <a-avatar :size="24" :src="item.user?.userAvatar" />
                      <span class="creator-name">{{ item.user?.userName ?? '用户' }}</span>
                    </div>
                  </div>
                </template>
              </a-card-meta>
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
            :page-size-options="['10', '20']"
            show-size-changer
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
import { PaperClipOutlined, ThunderboltOutlined, ArrowUpOutlined } from '@ant-design/icons-vue'
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

const suggestTags = ['波普风电商页面', '企业网站', '电商运营后台', '暗黑话题社区']

const mySearch = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 12,
  appName: undefined,
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

function relativeCreated(iso?: string) {
  if (!iso) return ''
  const t = new Date(iso).getTime()
  if (Number.isNaN(t)) return ''
  const diff = Date.now() - t
  const m = Math.floor(diff / 60000)
  if (m < 60) return `创建于 ${Math.max(1, m)} 分钟前`
  const h = Math.floor(m / 60)
  if (h < 24) return `创建于 ${h} 小时前`
  const d = Math.floor(h / 24)
  if (d < 30) return `创建于 ${d} 天前`
  return `创建于 ${new Date(iso).toLocaleDateString()}`
}

function onUploadClick() {
  message.info('上传能力敬请期待')
}

function onOptimizeClick() {
  message.info('提示词优化敬请期待')
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

function goChat(id: string) {
  if (!loginUserStore.loginUser?.id) {
    message.warning('请先登录后查看应用')
    router.push('/user/login')
    return
  }
  router.push(`/app/chat/${id}`)
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
  min-height: calc(100vh - 64px - 80px);
  font-family:
    system-ui,
    -apple-system,
    'Segoe UI',
    Roboto,
    'PingFang SC',
    'Microsoft YaHei',
    sans-serif;
}

.home-hero {
  padding: 64px 24px 48px;
  background: linear-gradient(135deg, #e0f7fa 0%, #b2ebf2 30%, #80deea 60%, #4dd0e1 100%);
}

.hero-inner {
  max-width: 880px;
  margin: 0 auto;
  text-align: center;
}

.hero-title {
  margin: 0 0 16px;
  font-size: clamp(32px, 5vw, 48px);
  font-weight: 800;
  color: #0f172a;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
  letter-spacing: -0.5px;
}

.hero-logo-wrap {
  display: inline-flex;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #22c55e;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(34, 197, 94, 0.35);
  transition: transform 0.3s ease;
}

.hero-logo-wrap:hover {
  transform: scale(1.05);
}

.hero-logo {
  width: 38px;
  height: 38px;
  object-fit: contain;
}

.hero-sub {
  margin: 0 0 36px;
  color: #64748b;
  font-size: 17px;
  font-weight: 400;
}

.prompt-card {
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 16px 48px rgba(15, 23, 42, 0.1);
  padding: 20px 20px 16px;
  text-align: left;
  transition: box-shadow 0.3s ease;
}

.prompt-card:hover {
  box-shadow: 0 20px 56px rgba(15, 23, 42, 0.15);
}

.prompt-input {
  font-size: 16px;
  resize: none !important;
  line-height: 1.6;
}

.prompt-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px solid #f1f5f9;
}

.prompt-toolbar-left {
  display: flex;
  gap: 4px;
}

.tool-btn {
  color: #64748b;
  font-size: 14px;
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.tool-btn:hover {
  background: #f1f5f9;
  color: #0f172a;
}

.send-btn {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #334155, #1e293b) !important;
  border: none !important;
  box-shadow: 0 4px 12px rgba(51, 65, 85, 0.3);
  transition: all 0.3s ease;
}

.send-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(51, 65, 85, 0.4);
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
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid #e2e8f0 !important;
}

.home-panel {
  padding: 48px 24px 16px;
  background: linear-gradient(180deg, #b2ebf2 0%, #e0f2fe 20%, #f8fafc 50%);
}

.home-panel--last {
  padding-bottom: 48px;
}

.panel-inner {
  max-width: 1280px;
  margin: 0 auto;
  background: #fff;
  border-radius: 28px;
  padding: 36px 32px 28px;
  box-shadow: 0 12px 40px rgba(15, 23, 42, 0.08);
}

.section-title {
  margin: 0 0 24px;
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.3px;
}

.search-row {
  margin-bottom: 16px;
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
  border: 1px solid #e2e8f0;
}

.app-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.12);
}

.thumb-wrap {
  position: relative;
  height: 200px;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
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

.card-time {
  color: #94a3b8;
  font-size: 13px;
  font-weight: 500;
}

.empty-block {
  padding: 32px 0;
}

.error-block {
  padding: 16px 0;
}

.featured-meta {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.creator {
  display: flex;
  align-items: center;
  gap: 8px;
}

.creator-name {
  color: #64748b;
  font-size: 13px;
}

.pager {
  margin-top: 28px;
  display: flex;
  justify-content: flex-end;
}

.need-login {
  padding: 24px 0;
}

.home-footer-note {
  text-align: right;
  padding: 0 24px 16px;
  font-size: 13px;
  color: #94a3b8;
}

.home-footer-note a {
  color: #94a3b8;
}
</style>
