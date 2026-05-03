<template>
  <a-layout-header class="header">
    <a-row :wrap="false">
      <!-- 左侧：Logo和标题 -->
      <a-col flex="200px">
        <RouterLink to="/">
          <div class="header-left">
            <img class="logo" src="@/assets/logo.png" alt="Logo" />
            <h1 class="site-title">应用生成</h1>
          </div>
        </RouterLink>
      </a-col>
      <!-- 中间：导航菜单 -->
      <a-col flex="auto" class="nav-col">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          @click="handleMenuClick"
        />
      </a-col>
      <!-- 右侧：用户操作区域 -->
      <a-col class="user-col">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space class="user-trigger">
                <a-avatar :size="32" :src="loginUserStore.loginUser.userAvatar">
                  {{ loginUserStore.loginUser.userName?.charAt(0) || 'U' }}
                </a-avatar>
                <span class="user-name">{{ loginUserStore.loginUser.userName ?? '无名' }}</span>
                <DownOutlined class="arrow-icon" />
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login" size="small">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogout } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
const loginUserStore = useLoginUserStore()
import { LogoutOutlined, DownOutlined } from '@ant-design/icons-vue'

const router = useRouter()
// 当前选中菜单
const selectedKeys = ref<string[]>(['/'])
// 监听路由变化，更新当前选中菜单
router.afterEach((to) => {
  selectedKeys.value = [to.path]
})

// 菜单配置项
const originItems = [
  {
    key: '/',
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/appManage',
    label: '应用管理',
    title: '应用管理',
  },
  {
    key: 'others',
    label: h('a', { href: 'https://www.codefather.cn', target: '_blank' }, '编程导航'),
    title: '编程导航',
  },
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  // 跳转到对应页面
  if (key.startsWith('/')) {
    router.push(key)
  }
}

// 用户注销
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
.header {
  background: #fff;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.04);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  height: 40px;
  width: 40px;
}

.site-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
  white-space: nowrap;
}

.nav-col {
  overflow: hidden;
}

.user-col {
  flex-shrink: 0;
}

.user-trigger {
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-trigger:hover {
  background: #f1f5f9;
}

.user-name {
  font-size: 14px;
  color: #334155;
  font-weight: 500;
}

.arrow-icon {
  font-size: 10px;
  color: #94a3b8;
}

.ant-menu-horizontal {
  border-bottom: none !important;
}

@media (max-width: 768px) {
  .header {
    padding: 0 12px;
  }
  .site-title {
    display: none;
  }
  .user-name {
    display: none;
  }
}
</style>
