import { API_BASE_URL } from '@/request'

/**
 * 调用 GET /app/chat/gen/code，按 SSE 规范解析流式文本并回调。
 * 使用 fetch + credentials，便于携带与 axios 一致的 Cookie。
 * 后端返回的 JSON 格式数据如 {"d":"我来"}，需要解析并提取 d 字段的值
 */
export async function streamChatGenCode(
  params: { appId: string; message: string },
  onData: (chunk: string) => void,
  options?: { signal?: AbortSignal },
): Promise<void> {
  const qs = new URLSearchParams({
    appId: params.appId,
    message: params.message,
  })
  const res = await fetch(`${API_BASE_URL}/app/chat/gen/code?${qs.toString()}`, {
    method: 'GET',
    credentials: 'include',
    signal: options?.signal,
  })
  if (!res.ok) {
    throw new Error(res.statusText || `请求失败 ${res.status}`)
  }
  const reader = res.body?.getReader()
  if (!reader) {
    throw new Error('响应不支持流式读取')
  }
  const decoder = new TextDecoder()
  let carry = ''
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    carry += decoder.decode(value, { stream: true })
    const parts = carry.split(/\r?\n/)
    carry = parts.pop() ?? ''
    for (const line of parts) {
      if (line.startsWith('data:')) {
        const payload = line.slice(5).trimStart()
        if (payload === '[DONE]' || payload === '') continue

        // 解析 JSON 格式数据，提取 d 字段的值
        try {
          const jsonData = JSON.parse(payload)
          if (jsonData && jsonData.d !== undefined) {
            onData(jsonData.d)
          } else {
            // 如果不是 JSON 格式，直接使用原始数据
            onData(payload)
          }
        } catch {
          // 解析失败，直接使用原始数据
          onData(payload)
        }
      }
    }
  }
  if (carry.trim()) {
    for (const line of carry.split(/\r?\n/)) {
      if (line.startsWith('data:')) {
        const payload = line.slice(5).trimStart()
        if (payload && payload !== '[DONE]') {
          // 解析 JSON 格式数据
          try {
            const jsonData = JSON.parse(payload)
            if (jsonData && jsonData.d !== undefined) {
              onData(jsonData.d)
            } else {
              onData(payload)
            }
          } catch {
            onData(payload)
          }
        }
      }
    }
  }
}

/**
 * 与 `src/default.md` 中 static-resource-controller 一致：
 * `GET /api/static/{deployKey}/**`，预览 iframe 使用带尾斜杠的根路径即可。
 * 若尚无 `deployKey`，则按业务约定使用 `{codeGenType}_{appId}` 作为 deployKey。
 */
export function staticPreviewUrl(vo: API.AppVO | undefined, appId: string): string {
  const key = vo?.deployKey?.trim()
  if (key) {
    return `${API_BASE_URL}/static/${encodeURI(key)}/`
  }
  const type = vo?.codeGenType?.trim() || 'default'
  return `${API_BASE_URL}/static/${encodeURI(`${type}_${appId}`)}/`
}
