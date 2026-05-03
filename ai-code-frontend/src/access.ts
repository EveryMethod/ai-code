import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser'
import { message } from 'ant-design-vue'
import router from '@/router'

// 是否为首次获取登录用户
let firstFetchLoginUser = true

/**
 * 全局权限校验
 */
const guard = async (
  to: RouteLocationNormalized,
  _from: RouteLocationNormalized,
  next: NavigationGuardNext,
) => {
  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser
  // 确保页面刷新，首次加载时，能够等后端返回用户信息后再校验权限
  if (firstFetchLoginUser) {
    await loginUserStore.fetchLoginUser()
    loginUser = loginUserStore.loginUser
    firstFetchLoginUser = false
  }
  const toUrl = to.fullPath
  // 已登录用户访问登录/注册页，直接跳转首页
  if (loginUser?.id && (toUrl.startsWith('/user/login') || toUrl.startsWith('/user/register'))) {
    next('/')
    return
  }
  if (to.meta.requiresAuth) {
    if (!loginUser?.id) {
      message.warning('请先登录')
      next(`/user/login?redirect=${encodeURIComponent(to.fullPath)}`)
      return
    }
  }
  if (toUrl.startsWith('/admin')) {
    if (!loginUser || loginUser.userRole !== 'admin') {
      message.error('没有权限')
      next(`/user/login?redirect=${encodeURIComponent(to.fullPath)}`)
      return
    }
  }
  next()
}

router.beforeEach(guard)
