import 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    /** 主页等全宽渐变背景，不占 BasicLayout 白底卡片 */
    fullBleed?: boolean
    /** 需登录 */
    requiresAuth?: boolean
  }
}
