<template>
  <div class="home-shell">
    <el-container class="home-layout">
      <el-aside width="248px" class="sidebar">
        <div class="brand-panel">
          <div class="brand-badge">Finance OS</div>
          <div class="brand-title">企业财务管理系统</div>
          <div class="brand-subtitle">把凭证、报销和报表集中到统一工作台。</div>
        </div>

        <el-menu :router="true" :default-active="$route.path" class="nav-menu">
          <template v-for="item in visibleNavItems" :key="item.path || item.label">
            <el-menu-item v-if="!item.children?.length" :index="item.path">
              <span>{{ item.label }}</span>
            </el-menu-item>
            <el-sub-menu v-else :index="item.label">
              <template #title>
                <span>{{ item.label }}</span>
              </template>
              <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
                <span>{{ child.label }}</span>
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>

        <div class="sidebar-foot">
          <div class="foot-label">系统状态</div>
          <div class="foot-value">运行正常</div>
        </div>
      </el-aside>

      <el-container class="main-layout">
        <el-header class="topbar-wrap">
          <div class="topbar">
            <div>
              <div class="topbar-title">控制台</div>
              <div class="topbar-subtitle">统一处理企业财务核心业务流程。</div>
            </div>
            <div class="topbar-actions">
              <div class="user-card">
                <span class="user-label">当前用户</span>
                <strong>{{ userDisplay }}</strong>
              </div>
              <div class="today-card">
                <span class="today-label">今天</span>
                <strong>{{ todayText }}</strong>
              </div>
              <el-button plain type="danger" @click="logout">退出登录</el-button>
            </div>
          </div>
        </el-header>

        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCurrentUserInfo, logout as logoutApi } from '../api/auth'

const router = useRouter()
const currentUser = ref(null)
const cachedUsername = ref((localStorage.getItem('username') || '').trim())
const localRoles = ref(parseRoles())

function parseRoles() {
  try {
    const raw = localStorage.getItem('roles')
    const parsed = raw ? JSON.parse(raw) : []
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

const isAdmin = computed(() => localRoles.value.includes('ADMIN'))

const navItems = [
  { path: '/home/dashboard', label: '首页看板' },
  { path: '/home/vouchers', label: '凭证管理' },
  { path: '/home/ledgers/general', label: '总账查询' },
  { path: '/home/reports/profit-statement', label: '利润表' },
  { path: '/home/expense-claims', label: '费用报销' },
  { path: '/home/roles', label: '角色管理', adminOnly: true },
  { path: '/home/users', label: '用户管理', adminOnly: true },
  { path: '/home/operation-logs', label: '操作日志', adminOnly: true },
  {
    label: '系统设置',
    adminOnly: true,
    children: [{ path: '/home/basic-data/accounting-periods', label: '账套期间管理' }]
  }
]

const visibleNavItems = computed(() => {
  const includeAdmin = isAdmin.value
  return navItems
    .map((item) => {
      if (item.children?.length) {
        const children = item.children.filter((child) => !child.adminOnly || includeAdmin)
        return { ...item, children }
      }
      return item
    })
    .filter((item) => (!item.adminOnly || includeAdmin) && (!item.children || item.children.length > 0))
})

const todayText = computed(() =>
  new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  }).format(new Date())
)

const looksBrokenText = (value) => {
  if (!value) return true
  const text = value.trim()
  if (!text) return true
  return /[?�]/.test(text)
}

const userDisplay = computed(() => {
  const user = currentUser.value || {}
  const username = (user.username || '').trim()
  const nickname = (user.nickname || '').trim()
  const localName = cachedUsername.value

  const validUsername = !looksBrokenText(username)
    ? username
    : (!looksBrokenText(localName) ? localName : '')
  const validNickname = !looksBrokenText(nickname) ? nickname : ''

  if (validNickname && validUsername) return `${validNickname}（${validUsername}）`
  if (validUsername) return validUsername
  if (validNickname) return validNickname
  return '未知用户'
})

const fetchCurrentUser = async () => {
  try {
    const resp = await getCurrentUserInfo()
    if (resp.code === 200) {
      currentUser.value = resp.data || null
      const freshUsername = (resp.data?.username || '').trim()
      if (freshUsername) {
        cachedUsername.value = freshUsername
        localStorage.setItem('username', freshUsername)
      }
      const roles = Array.isArray(resp.data?.roles) ? resp.data.roles : []
      localRoles.value = roles
      localStorage.setItem('roles', JSON.stringify(roles))
      return
    }
    ElMessage.error(resp.message || '当前用户信息加载失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '当前用户信息加载失败')
  }
}

const logout = async () => {
  try {
    await logoutApi()
  } catch {
    // best-effort
  }
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('roles')
  router.push('/login')
}

onMounted(() => {
  fetchCurrentUser()
})
</script>

<style scoped>
.home-shell {
  min-height: 100vh;
  padding: 18px;
  background:
    radial-gradient(circle at top left, rgba(37, 99, 235, 0.16), transparent 28%),
    radial-gradient(circle at bottom right, rgba(15, 118, 110, 0.12), transparent 24%),
    linear-gradient(180deg, #f7fafc 0%, #eef4fb 100%);
}

.home-layout {
  min-height: calc(100vh - 36px);
  gap: 18px;
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.brand-panel,
.sidebar-foot,
.topbar {
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.06);
  backdrop-filter: blur(10px);
}

.brand-panel {
  padding: 20px 18px;
}

.brand-badge {
  display: inline-flex;
  padding: 4px 10px;
  border-radius: 999px;
  background: #dbeafe;
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 700;
}

.brand-title {
  margin-top: 14px;
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
}

.brand-subtitle {
  margin-top: 8px;
  line-height: 1.7;
  color: #64748b;
  font-size: 13px;
}

.nav-menu {
  border: none;
  border-radius: 20px;
  padding: 14px 10px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.06);
}

:deep(.nav-menu .el-menu-item) {
  height: 46px;
  margin: 6px 0;
  border-radius: 14px;
  color: #334155;
}

:deep(.nav-menu .el-menu-item.is-active) {
  background: linear-gradient(135deg, #2563eb, #0f766e);
  color: #fff;
}

:deep(.nav-menu .el-sub-menu__title) {
  height: 46px;
  margin: 6px 0;
  border-radius: 14px;
  color: #334155;
}

.sidebar-foot {
  margin-top: auto;
  padding: 16px 18px;
}

.foot-label {
  font-size: 12px;
  color: #64748b;
}

.foot-value {
  margin-top: 6px;
  font-size: 15px;
  font-weight: 700;
  color: #0f766e;
}

.main-layout {
  min-width: 0;
}

.topbar-wrap {
  padding: 0;
  height: auto;
  background: transparent;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 22px;
}

.topbar-title {
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
}

.topbar-subtitle {
  margin-top: 6px;
  color: #64748b;
  font-size: 14px;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-card,
.today-card {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  min-width: 150px;
}

.user-label,
.today-label {
  font-size: 12px;
  color: #64748b;
}

.main-content {
  padding: 16px 0 0;
}

@media (max-width: 960px) {
  .home-layout {
    flex-direction: column;
  }

  .topbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .topbar-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
