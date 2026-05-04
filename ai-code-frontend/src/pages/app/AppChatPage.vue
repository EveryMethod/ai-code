<template>
  <div class="chat-page">
    <header class="chat-top">
      <div class="chat-top-left">
        <img class="mini-logo" src="@/assets/logo.png" alt="" />
        <span class="app-name">{{ app?.appName || '加载中…' }}</span>
        <a-tag v-if="app?.codeGenType" color="blue" size="small"
          >{{ app.codeGenType }} 项目模式</a-tag
        >
      </div>
      <div class="chat-top-right">
        <a-popover
          v-model:open="detailVisible"
          trigger="click"
          placement="bottomRight"
          overlay-class-name="app-detail-popover"
        >
          <template #content>
            <div class="app-detail-content">
              <!-- 应用基础信息 -->
              <div class="detail-section">
                <div class="detail-item">
                  <span class="detail-label">创建者</span>
                  <div class="detail-value">
                    <a-avatar :size="32" :src="app?.user?.userAvatar" />
                    <span class="creator-name">{{ app?.user?.userName ?? '未知用户' }}</span>
                  </div>
                </div>
                <div class="detail-item">
                  <span class="detail-label">创建时间</span>
                  <span class="detail-value">{{ formatTime(app?.createTime) }}</span>
                </div>
              </div>
              <!-- 操作栏（仅本人或管理员可见） -->
              <div v-if="canOperate" class="detail-actions">
                <a-button size="small" @click="goEditFromDetail">
                  <template #icon><EditOutlined /></template>
                  修改
                </a-button>
                <a-button size="small" danger @click="onDeleteApp">
                  <template #icon><DeleteOutlined /></template>
                  删除
                </a-button>
              </div>
            </div>
          </template>
          <a-button size="small">
            <template #icon><InfoCircleOutlined /></template>
            应用详情
          </a-button>
        </a-popover>
        <a-button size="small" @click="downloadCode">
          <template #icon><DownloadOutlined /></template>
          下载代码
        </a-button>
        <a-button type="primary" :loading="deploying" @click="onDeploy">
          <template #icon><CloudUploadOutlined /></template>
          部署
        </a-button>
      </div>
    </header>

    <div class="chat-body">
      <div class="chat-col chat-col--left">
        <!-- History loading -->
        <div v-if="historyLoading" class="history-loading">
          <a-spin tip="加载对话历史…" />
        </div>

        <!-- Welcome screen when no messages and history loaded -->
        <div v-else-if="messages.length === 0 && !streaming" class="chat-welcome">
          <div class="welcome-icon">
            <img src="@/assets/logo.png" alt="" />
          </div>
          <h3>{{ app?.appName || 'AI 应用' }}</h3>
          <p>描述你想要生成的网站,越详细效果越好哦</p>
          <div class="welcome-hints">
            <a-button
              v-for="hint in welcomeHints"
              :key="hint"
              size="small"
              @click="draft = hint; textareaRef?.focus()"
            >
              {{ hint }}
            </a-button>
          </div>
        </div>

        <!-- Messages area -->
        <div v-else ref="scrollRef" class="messages" @click="onMsgClick">
          <!-- Load more history -->
          <div v-if="hasMoreHistory" class="load-more-row">
            <a-button size="small" :loading="loadingMore" @click="loadMoreHistory">
              加载更多历史消息
            </a-button>
          </div>
          <div
            v-for="(m, idx) in messages"
            :key="idx"
            :class="['msg-row', m.role === 'user' ? 'msg-row--user' : 'msg-row--ai']"
          >
            <div class="msg-body">
              <div v-if="m.role === 'user'" class="msg-text msg-text--user">{{ m.content }}</div>
              <div
                v-else
                class="msg-text msg-text--markdown"
                v-html="renderMarkdown(m.content)"
              ></div>
              <div
                v-if="m.role === 'assistant' && streaming && idx === messages.length - 1"
                class="msg-typing"
              >
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
              </div>
            </div>
          </div>
        </div>

        <!-- Composer -->
        <div class="composer">
          <a-tooltip :title="canOperate ? '' : '无法在别人的作品下对话哦~'" placement="top">
            <a-textarea
              ref="textareaRef"
              v-model:value="draft"
              :rows="2"
              :disabled="streaming || !canOperate"
              :placeholder="
                canOperate ? '请描述你想生成的网站，越详细效果越好哦' : '无法在别人的作品下对话哦~'
              "
              @pressEnter="onEnterPress"
            />
          </a-tooltip>
          <a-tooltip :title="canOperate ? '' : '无法在别人的作品下对话哦~'" placement="top">
            <a-button
              v-if="!streaming"
              type="primary"
              shape="circle"
              class="send-btn"
              :disabled="!draft.trim() || !canOperate"
              @click="sendManual"
            >
              <template #icon><SendOutlined /></template>
            </a-button>
          </a-tooltip>
          <a-button v-if="streaming" danger size="small" @click="stopStream">
            <template #icon><PauseCircleOutlined /></template>
            停止生成
          </a-button>
        </div>
      </div>

      <!-- Preview column -->
      <div class="chat-col chat-col--right">
        <div class="preview-head">
          <span class="preview-head-title">生成后的网页展示</span>
          <div class="preview-actions">
            <a-button size="small" @click="toggleEditMode">
              <template #icon><EditOutlined /></template>
              {{ editMode ? '预览模式' : '编辑模式' }}
            </a-button>
            <a-button size="small" @click="openInNewWindow">
              <template #icon><ExportOutlined /></template>
              新窗口打开
            </a-button>
          </div>
        </div>
        <div class="preview-frame-wrap">
          <a-spin v-if="streaming" class="preview-spin" tip="AI 正在生成页面,请稍候…">
            <template #indicator>
              <LoadingOutlined style="font-size: 36px" spin />
            </template>
          </a-spin>
          <iframe
            v-if="previewUrl && !streaming"
            :key="previewKey"
            class="preview-frame"
            :src="previewUrl"
            title="preview"
            sandbox="allow-scripts allow-same-origin"
          />
          <a-empty
            v-if="!previewUrl && !streaming"
            class="preview-empty"
            description="在左侧发送消息,AI 将为你生成页面"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  CloudUploadOutlined,
  InfoCircleOutlined,
  DownloadOutlined,
  SendOutlined,
  EditOutlined,
  ExportOutlined,
  LoadingOutlined,
  PauseCircleOutlined,
  DeleteOutlined,
} from '@ant-design/icons-vue'
import { deployApp, getAppVoById, deleteApp } from '@/api/appController.ts'
import { listAppChatHistory } from '@/api/chatHistoryController.ts'
import { streamChatGenCode, staticPreviewUrl } from '@/utils/chatSse.ts'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import dayjs from 'dayjs'
import hljs from 'highlight.js'
import 'highlight.js/styles/vs2015.css' // VS Code 风格主题

type Role = 'user' | 'assistant'
type ChatMessage = { role: Role; content: string; versionNote?: string; time?: string }

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = ref<string>('')
const app = ref<API.AppVO>()
const messages = ref<ChatMessage[]>([])
const draft = ref('')
const streaming = ref(false)
const previewUrl = ref('')
const previewKey = ref(0)
const scrollRef = ref<HTMLElement | null>(null)
const textareaRef = ref<InstanceType<typeof HTMLTextAreaElement> | null>(null)
const deploying = ref(false)
const editMode = ref(false)
const detailVisible = ref(false)

// 历史消息
const historyLoading = ref(false)
const loadingMore = ref(false)
const hasMoreHistory = ref(false)
const historyTotal = ref(0)
const lastCreateTime = ref<string | undefined>(undefined)

const welcomeHints = [
  '创建一个电商首页',
  '做一个个人博客',
  '生成一个后台管理面板',
  '帮我做一个待办事项应用',
]

let sseAbort: AbortController | null = null

// 配置 marked 的渲染器，添加语法高亮
const renderer = new marked.Renderer()

// 重写 code 渲染器
renderer.code = ({ text, lang }: { text: string; lang?: string }) => {
  // 使用 highlight.js 进行语法高亮
  let highlighted = text
  if (lang && hljs.getLanguage(lang)) {
    try {
      highlighted = hljs.highlight(text, { language: lang }).value
    } catch {
      // 高亮失败，使用自动检测
      highlighted = hljs.highlightAuto(text).value
    }
  } else {
    highlighted = hljs.highlightAuto(text).value
  }

  // 返回带样式的代码块
  return `<pre><code class="language-${lang || ''} hljs">${highlighted}</code></pre>`
}

marked.setOptions({
  renderer,
  breaks: true, // 支持换行
  gfm: true, // 启用 GitHub Flavored Markdown
})

const renderMarkdown = (content: string): string => {
  if (!content) return ''

  // 使用 marked 解析 Markdown（已包含语法高亮）
  const raw = marked(content) as unknown as string

  // DOMPurify 清理 HTML，防止 XSS
  const sanitized = DOMPurify.sanitize(raw) as unknown as string

  // 为代码块添加复制按钮
  const html = sanitized.replace(
    /<pre><code class="language-(\w*) hljs">([\s\S]*?)<\/code><\/pre>/g,
    (_match, lang, code) => {
      const langLabel = lang || 'code'
      return `<div class="code-block">
        <div class="code-block-head">
          <span class="code-lang">${langLabel}</span>
          <button class="code-copy-btn" data-code="${encodeURIComponent(code)}">复制</button>
        </div>
        <pre><code class="language-${lang} hljs">${code}</code></pre>
      </div>`
    },
  )

  return html
}

function nowTime() {
  const d = new Date()
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// 立即滚动到底部（同步操作，用于流式输出时）
function scrollBottomSync() {
  const el = scrollRef.value
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

async function scrollBottom() {
  await nextTick()
  const el = scrollRef.value
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

function onEnterPress(e: KeyboardEvent) {
  if (!e.shiftKey) {
    e.preventDefault()
    sendManual()
  }
}

function toggleEditMode() {
  editMode.value = !editMode.value
  message.info(editMode.value ? '已切换到编辑模式' : '已切换到预览模式')
}

function openInNewWindow() {
  if (previewUrl.value) {
    window.open(previewUrl.value, '_blank')
  } else {
    message.warning('请先生成页面')
  }
}

function downloadCode() {
  message.info('下载代码功能敬请期待')
}

function goEdit() {
  if (appId.value) {
    router.push(`/app/edit/${appId.value}`)
  }
}

function goEditFromDetail() {
  detailVisible.value = false
  goEdit()
}

// 判断是否有操作权限（本人或管理员）
const canOperate = computed(() => {
  const currentUser = loginUserStore.loginUser
  if (!currentUser?.id) return false
  // 管理员
  if (currentUser.userRole === 'admin') return true
  // 本人
  return app.value?.userId === currentUser.id
})

// 格式化时间
function formatTime(time?: string): string {
  if (!time) return '未知'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

// 删除应用
async function onDeleteApp() {
  if (!appId.value) return
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除该应用吗？此操作不可恢复。',
    okText: '确定删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await deleteApp({ id: appId.value })
        if (res.data.code === 0) {
          message.success('删除成功')
          detailVisible.value = false
          await router.push('/')
        } else {
          message.error(res.data.message ?? '删除失败')
        }
      } catch {
        message.error('删除失败，请检查网络或登录状态')
      }
    },
  })
}

function onMsgClick(e: MouseEvent) {
  const target = e.target as HTMLElement
  const btn = target.closest?.('.code-copy-btn') as HTMLElement | null
  if (btn) {
    const code = btn.dataset.code
    if (code) {
      navigator.clipboard
        .writeText(decodeURIComponent(code))
        .then(() => {
          btn.textContent = '已复制'
          btn.classList.add('copied')
          setTimeout(() => {
            btn.textContent = '复制'
            btn.classList.remove('copied')
          }, 2000)
        })
        .catch(() => {
          message.warning('复制失败,请手动复制')
        })
    }
  }
}

async function loadApp() {
  const id = String(route.params.id)
  if (!id || id === 'undefined' || id === 'null') {
    message.error('无效的应用 id')
    await router.push('/')
    return
  }
  appId.value = id
  const res = await getAppVoById({ id })
  if (res.data.code === 0 && res.data.data) {
    app.value = res.data.data
    return
  }
  message.error(res.data.message ?? '获取应用失败')
  await router.push('/')
}

/** 加载最近一页对话历史（游标查询，按创建时间升序展示） */
async function loadHistory() {
  historyLoading.value = true
  try {
    const res = await listAppChatHistory({
      appId: appId.value as unknown as number,
      pageSize: 10,
    })
    if (res.data.code === 0 && res.data.data) {
      const records = res.data.data.records ?? []
      historyTotal.value = res.data.data.totalRow ?? 0
      // 按创建时间升序排列
      const sorted = [...records].sort(
        (a, b) => dayjs(a.createTime).valueOf() - dayjs(b.createTime).valueOf(),
      )
      // 转换为 ChatMessage 格式
      const historyMsgs: ChatMessage[] = sorted.map((r) => ({
        role: (r.messageType === 'user' ? 'user' : 'assistant') as Role,
        content: r.message || '',
        time: r.createTime ? dayjs(r.createTime).format('HH:mm') : '',
      }))
      messages.value = historyMsgs
      // 判断是否有更多历史
      hasMoreHistory.value = historyTotal.value > records.length
      // 记录最早一条的 createTime 作为游标
      if (sorted.length > 0) {
        lastCreateTime.value = sorted[0].createTime
      }
      // 若有 >= 2 条记录则展示网站预览
      if (historyTotal.value >= 2) {
        previewUrl.value = staticPreviewUrl(app.value, appId.value)
      }
    } else {
      messages.value = []
      hasMoreHistory.value = false
    }
  } catch {
    message.error('加载对话历史失败')
    messages.value = []
    hasMoreHistory.value = false
  } finally {
    historyLoading.value = false
  }
}

/** 加载更早一页历史消息 */
async function loadMoreHistory() {
  if (!lastCreateTime.value) return
  loadingMore.value = true
  try {
    const res = await listAppChatHistory({
      appId: appId.value as unknown as number,
      pageSize: 10,
      lastCreateTime: lastCreateTime.value,
    })
    if (res.data.code === 0 && res.data.data) {
      const records = res.data.data.records ?? []
      // 按创建时间升序排列
      const sorted = [...records].sort(
        (a, b) => dayjs(a.createTime).valueOf() - dayjs(b.createTime).valueOf(),
      )
      const moreMsgs: ChatMessage[] = sorted.map((r) => ({
        role: (r.messageType === 'user' ? 'user' : 'assistant') as Role,
        content: r.message || '',
        time: r.createTime ? dayjs(r.createTime).format('HH:mm') : '',
      }))
      // 前置插入更早的消息
      messages.value = [...moreMsgs, ...messages.value]
      // 更新游标与更多标记
      if (sorted.length > 0) {
        lastCreateTime.value = sorted[0].createTime
      }
      hasMoreHistory.value = historyTotal.value > messages.value.length
    }
  } catch {
    message.error('加载更多历史消息失败')
  } finally {
    loadingMore.value = false
  }
}

async function runStream(userText: string) {
  streaming.value = true
  previewUrl.value = ''
  sseAbort?.abort()
  sseAbort = new AbortController()

  messages.value.push({ role: 'user', content: userText, time: nowTime() })
  messages.value.push({ role: 'assistant', content: '', time: nowTime() })
  await scrollBottom()

  const assistantIndex = messages.value.length - 1
  try {
    await streamChatGenCode(
      { appId: appId.value, message: userText },
      (chunk) => {
        const cur = messages.value[assistantIndex]
        if (cur) {
          cur.content += chunk
          // 每次接收数据后立即滚动到底部（同步操作，性能更好）
          scrollBottomSync()
        }
      },
      { signal: sseAbort.signal },
    )
    const doneMsg = messages.value[assistantIndex]
    if (doneMsg) {
      doneMsg.versionNote = '生成已完成'
    }
    const detail = await getAppVoById({ id: appId.value })
    if (detail.data.code === 0 && detail.data.data) {
      app.value = detail.data.data
    }
    previewUrl.value = staticPreviewUrl(app.value, appId.value)
  } catch (e: unknown) {
    if (e instanceof Error && e.name === 'AbortError') {
      const cur = messages.value[assistantIndex]
      if (cur && !cur.content.trim()) {
        cur.content = '(已停止生成)'
        cur.versionNote = '生成已中止'
      } else if (cur) {
        cur.versionNote = '生成已中止'
      }
      return
    }
    const msg = e instanceof Error ? e.message : '对话失败'
    message.error(msg)
    messages.value.pop()
    messages.value.pop()
  } finally {
    streaming.value = false
    await scrollBottom()
  }
}

function stopStream() {
  sseAbort?.abort()
  streaming.value = false
}

async function sendManual() {
  const text = draft.value.trim()
  if (!text || streaming.value) return
  draft.value = ''
  await runStream(text)
}

async function maybeAutoStart() {
  // 没有权限操作，不自动发送消息
  if (!canOperate.value) {
    return
  }

  // 没有 auto=1 参数，不自动发送
  if (route.query.auto !== '1') return

  // 有历史消息，不自动发送
  if (historyTotal.value > 0) {
    await router.replace({ path: route.path, query: {} })
    return
  }

  const prompt = app.value?.initPrompt?.trim()
  if (!prompt) {
    await router.replace({ path: route.path })
    return
  }
  await router.replace({ path: route.path, query: {} })
  await runStream(prompt)
}

async function onDeploy() {
  deploying.value = true
  try {
    const res = await deployApp({ appId: appId.value })
    if (res.data.code === 0 && res.data.data) {
      Modal.success({
        title: '部署成功',
        content: res.data.data,
      })
    } else {
      message.error(res.data.message ?? '部署失败')
    }
  } catch {
    message.error('部署请求失败')
  } finally {
    deploying.value = false
  }
}

onMounted(async () => {
  await loadApp()
  await loadHistory()
  await maybeAutoStart()
})

onUnmounted(() => {
  sseAbort?.abort()
})
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
  background: #f5f7fa;
  font-family:
    system-ui,
    -apple-system,
    'Segoe UI',
    Roboto,
    'PingFang SC',
    'Microsoft YaHei',
    sans-serif;
}

.chat-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  z-index: 10;
  flex-shrink: 0;
}

.chat-top-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.chat-top-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.mini-logo {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  flex-shrink: 0;
}

.app-name {
  font-weight: 600;
  font-size: 16px;
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-body {
  flex: 1;
  display: flex;
  min-height: 0;
  overflow: hidden;
}

.chat-col {
  min-width: 0;
}

.chat-col--left {
  flex: 2;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  background: #fff;
  min-width: 0;
}

.chat-col--right {
  flex: 3;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  min-width: 0;
}

/* Welcome screen */
.chat-welcome {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 16px;
  text-align: center;
}

.welcome-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e0f7fa, #4dd0e1);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  box-shadow: 0 8px 24px rgba(77, 208, 225, 0.3);
}

.welcome-icon img {
  width: 38px;
  height: 38px;
}

.chat-welcome h3 {
  margin: 0 0 4px;
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.chat-welcome p {
  margin: 0 0 16px;
  color: #64748b;
  font-size: 14px;
  max-width: 320px;
}

.welcome-hints {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.welcome-hints .ant-btn {
  border-radius: 999px;
}

/* Messages */
.messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  scroll-behavior: smooth;
}

.load-more-row {
  text-align: center;
  padding: 0 0 16px;
}

.history-loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.messages::-webkit-scrollbar {
  width: 5px;
}

.messages::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 5px;
}

.msg-row {
  margin-bottom: 12px;
}

.msg-row--user {
  display: flex;
  justify-content: flex-end;
}

.msg-row--ai {
  display: flex;
  justify-content: flex-start;
}

.msg-body {
  min-width: 0;
  max-width: 90%;
}

.msg-row--user .msg-body {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.msg-text {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  font-size: 14px;
}

.msg-text--user {
  background: #1677ff;
  color: #fff;
  padding: 10px 16px;
  border-radius: 18px 18px 4px 18px;
}

.msg-text--markdown {
  white-space: normal;
  background: #f5f5f5;
  padding: 16px;
  border-radius: 18px 18px 18px 4px;
  border: 1px solid #e8e8e8;
}

.msg-text--markdown :deep(p) {
  margin: 0 0 8px;
  line-height: 1.65;
}

.msg-text--markdown :deep(p:last-child) {
  margin-bottom: 0;
}

.msg-text--markdown :deep(code) {
  background: rgba(0, 0, 0, 0.06);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  color: #e83e8c;
}

/* Code block with syntax highlighting */
.msg-text--markdown :deep(.code-block) {
  margin: 16px 0;
  border-radius: 10px;
  overflow: hidden;
  background: #1e1e1e;
  border: 1px solid #333;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.msg-text--markdown :deep(.code-block-head) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: linear-gradient(135deg, #2d2d2d 0%, #252526 100%);
  font-size: 13px;
  color: #858585;
  border-bottom: 1px solid #333;
}

.msg-text--markdown :deep(.code-lang) {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-weight: 600;
  color: #9cdcfe;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.msg-text--markdown :deep(.code-copy-btn) {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #ccc;
  padding: 4px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.msg-text--markdown :deep(.code-copy-btn:hover) {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.msg-text--markdown :deep(.code-copy-btn:active) {
  transform: translateY(0);
}

.msg-text--markdown :deep(.code-copy-btn.copied) {
  color: #4ade80;
  border-color: #4ade80;
  background: rgba(74, 222, 128, 0.1);
}

.msg-text--markdown :deep(pre) {
  margin: 0;
  padding: 20px 16px;
  overflow-x: auto;
  background: #1e1e1e;
}

.msg-text--markdown :deep(pre code) {
  background: transparent;
  padding: 0;
  color: #d4d4d4;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  tab-size: 2;
}

.msg-text--markdown :deep(ul),
.msg-text--markdown :deep(ol) {
  margin: 8px 0;
  padding-left: 24px;
}

.msg-text--markdown :deep(li) {
  margin: 4px 0;
}

.msg-text--markdown :deep(h1),
.msg-text--markdown :deep(h2),
.msg-text--markdown :deep(h3),
.msg-text--markdown :deep(h4),
.msg-text--markdown :deep(h5),
.msg-text--markdown :deep(h6) {
  margin: 16px 0 8px;
  font-weight: 600;
  line-height: 1.4;
}

.msg-text--markdown :deep(h1) {
  font-size: 1.4em;
}
.msg-text--markdown :deep(h2) {
  font-size: 1.25em;
}
.msg-text--markdown :deep(h3) {
  font-size: 1.15em;
}

.msg-text--markdown :deep(blockquote) {
  border-left: 3px solid #cbd5e1;
  padding-left: 12px;
  margin: 8px 0;
  color: #64748b;
}

.msg-text--markdown :deep(a) {
  color: #1677ff;
  text-decoration: none;
}

.msg-text--markdown :deep(a:hover) {
  text-decoration: underline;
}

.msg-text--markdown :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 8px 0;
  font-size: 13px;
}

.msg-text--markdown :deep(th),
.msg-text--markdown :deep(td) {
  border: 1px solid #e2e8f0;
  padding: 6px 10px;
  text-align: left;
}

.msg-text--markdown :deep(th) {
  background: #f1f5f9;
  font-weight: 600;
}

/* Typing indicator */
.msg-typing {
  display: flex;
  gap: 4px;
  padding: 6px 0 2px;
}

.typing-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #94a3b8;
  animation: typing-bounce 1.4s ease-in-out infinite;
}

.typing-dot:nth-child(2) {
  animation-delay: 0.2s;
}
.typing-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing-bounce {
  0%,
  80%,
  100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* Composer */
.composer {
  border-top: 1px solid #e8e8e8;
  padding: 8px 12px;
  background: #fff;
  flex-shrink: 0;
  display: flex;
  align-items: flex-end;
  gap: 10px;
}

.composer :deep(.ant-input) {
  flex: 1;
  border-radius: 8px;
}

.send-btn {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

/* Preview */
.preview-head {
  padding: 8px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.preview-head-title {
  font-weight: 600;
  font-size: 15px;
  color: #334155;
}

.preview-actions {
  display: flex;
  gap: 8px;
}

.preview-frame-wrap {
  position: relative;
  flex: 1;
  min-height: 0;
  background: #fff;
}

.preview-frame {
  width: 100%;
  height: 100%;
  border: 0;
  background: #fff;
}

.preview-empty,
.preview-spin {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

@media (max-width: 900px) {
  .chat-body {
    flex-direction: column;
  }
  .chat-col--left {
    flex: none;
    max-width: none;
    border-right: none;
    border-bottom: 1px solid #e8e8e8;
    min-height: 380px;
    max-height: 55vh;
  }
  .chat-col--right {
    min-height: 45vh;
  }
  .chat-top-right .ant-btn:first-child {
    display: none;
  }
  .msg-body {
    max-width: 95%;
  }
}

/* 应用详情悬浮窗 */
.app-detail-popover {
  .ant-popover-inner {
    border-radius: 12px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
}

.app-detail-content {
  min-width: 280px;
  padding: 4px 0;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-label {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.detail-value {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #334155;
  font-weight: 600;
}

.creator-name {
  font-size: 14px;
  color: #0f172a;
}

.detail-actions {
  display: flex;
  gap: 8px;
  padding-top: 12px;
}

.detail-actions .ant-btn {
  flex: 1;
}
</style>
