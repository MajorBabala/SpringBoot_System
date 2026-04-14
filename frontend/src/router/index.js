import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import DashboardPage from '../views/DashboardPage.vue'
import VoucherPage from '../views/Voucher/VoucherPage.vue'
import ProfitStatementPage from '../views/Report/ProfitStatementPage.vue'
import ExpenseClaimPage from '../views/Expense/ExpenseClaimPage.vue'
import GeneralLedgerPage from '../views/Ledger/GeneralLedgerPage.vue'
import AccountingPeriodPage from '../views/BasicData/AccountingPeriodPage.vue'
import RoleManagementPage from '../views/RoleManagementPage.vue'
import UserManagementPage from '../views/UserManagementPage.vue'
import OperationLogPage from '../views/OperationLogPage.vue'

function hasToken() {
  return !!localStorage.getItem('token')
}

function hasAdminRole() {
  try {
    const raw = localStorage.getItem('roles')
    const roles = raw ? JSON.parse(raw) : []
    return Array.isArray(roles) && roles.includes('ADMIN')
  } catch {
    return false
  }
}

const adminPaths = new Set([
  '/home/roles',
  '/home/users',
  '/home/operation-logs',
  '/home/basic-data/accounting-periods'
])

const routes = [
  { path: '/login', name: 'login', component: Login },
  {
    path: '/',
    redirect: '/home/dashboard'
  },
  {
    path: '/home',
    name: 'home',
    component: Home,
    children: [
      { path: 'dashboard', name: 'dashboard', component: DashboardPage },
      { path: 'vouchers', name: 'vouchers', component: VoucherPage },
      {
        path: 'reports/balance-sheet',
        redirect: () => ({ path: '/home/reports/profit-statement', query: { tab: 'balance' } })
      },
      { path: 'reports/profit-statement', name: 'profit-statement', component: ProfitStatementPage },
      {
        path: 'ledgers/subject-balances',
        redirect: () => ({ path: '/home/reports/profit-statement', query: { tab: 'subject' } })
      },
      { path: 'ledgers/general', name: 'general-ledger', component: GeneralLedgerPage },
      { path: 'basic-data/accounting-periods', name: 'accounting-periods', component: AccountingPeriodPage },
      {
        path: 'ledgers/detail',
        redirect: () => ({ path: '/home/ledgers/general', query: { tab: 'detail' } })
      },
      {
        path: 'ledgers/journal',
        redirect: () => ({ path: '/home/ledgers/general', query: { tab: 'journal' } })
      },
      { path: 'expense-claims', name: 'expense-claims', component: ExpenseClaimPage },
      { path: 'roles', name: 'roles', component: RoleManagementPage },
      { path: 'users', name: 'users', component: UserManagementPage },
      { path: 'operation-logs', name: 'operation-logs', component: OperationLogPage }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.path === '/login') return next()
  if (!hasToken()) return next('/login')
  if (adminPaths.has(to.path) && !hasAdminRole()) return next('/home/dashboard')
  return next()
})

export default router
