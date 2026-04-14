<template>
  <div>
    <div class="hero-panel">
      <div>
        <div class="hero-title">财务控制台</div>
        <div class="hero-desc">
          关键业务数据以饼图方式展示，便于快速查看结构占比和处理优先级。
        </div>
      </div>
      <div class="hero-tags">
        <span>凭证</span>
        <span>报销</span>
        <span>审批</span>
        <span>利润</span>
      </div>
    </div>

    <div class="pie-grid">
      <el-card v-for="card in pieCards" :key="card.title" class="app-card pie-card">
        <template #header>
          <div class="panel-header">
            <span>{{ card.title }}</span>
          </div>
        </template>

        <div class="pie-wrap">
          <div class="pie" :style="{ backgroundImage: card.gradient }">
            <div class="pie-center">{{ card.total }}</div>
          </div>
        </div>

        <div class="legend-list">
          <div v-for="item in card.items" :key="item.name" class="legend-item">
            <div class="legend-left">
              <span class="dot" :style="{ backgroundColor: item.color }"></span>
              <span>{{ item.name }}</span>
            </div>
            <div class="legend-right">{{ item.value }} / {{ item.percent }}%</div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { listExpenseClaims } from '../api/expenseClaims'
import { getProfitStatement } from '../api/reports'
import { listVouchers } from '../api/vouchers'

const COLORS = ['#2563eb', '#0f766e', '#f59e0b', '#dc2626', '#8b5cf6', '#14b8a6']

const dashboard = reactive({
  voucherTotal: 0,
  voucherStatus: [0, 0, 0, 0],
  claimTotal: 0,
  claimStatus: [0, 0, 0, 0, 0],
  netProfit: 0
})

const currentPeriod = computed(() => {
  const date = new Date()
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  return `${year}${month}`
})

const toNumber = (value) => {
  const num = Number(value || 0)
  return Number.isNaN(num) ? 0 : num
}

const toPercent = (value, total) => {
  if (!total) return 0
  return Math.round((value / total) * 100)
}

const buildGradient = (items) => {
  const total = items.reduce((sum, item) => sum + item.value, 0)
  if (total <= 0) {
    return 'conic-gradient(#e2e8f0 0deg 360deg)'
  }

  let start = 0
  const parts = items.map((item) => {
    const ratio = item.value / total
    const end = start + ratio * 360
    const segment = `${item.color} ${start}deg ${end}deg`
    start = end
    return segment
  })
  return `conic-gradient(${parts.join(', ')})`
}

const buildPieCard = (title, sourceItems) => {
  const total = sourceItems.reduce((sum, item) => sum + item.value, 0)
  const items = sourceItems.map((item) => ({
    ...item,
    percent: toPercent(item.value, total)
  }))
  return {
    title,
    total,
    items,
    gradient: buildGradient(items)
  }
}

const pieCards = computed(() => {
  const voucherItems = [
    { name: '未审核', value: dashboard.voucherStatus[0], color: COLORS[0] },
    { name: '已审核', value: dashboard.voucherStatus[1], color: COLORS[1] },
    { name: '已记账', value: dashboard.voucherStatus[2], color: COLORS[2] },
    { name: '已作废', value: dashboard.voucherStatus[3], color: COLORS[3] }
  ]

  const claimItems = [
    { name: '草稿', value: dashboard.claimStatus[0], color: COLORS[0] },
    { name: '待审批', value: dashboard.claimStatus[1], color: COLORS[1] },
    { name: '已审批', value: dashboard.claimStatus[2], color: COLORS[2] },
    { name: '已付款', value: dashboard.claimStatus[3], color: COLORS[3] },
    { name: '已驳回', value: dashboard.claimStatus[4], color: COLORS[4] }
  ]

  const workloadItems = [
    { name: '待审核凭证', value: dashboard.voucherStatus[0], color: COLORS[0] },
    { name: '待审批报销', value: dashboard.claimStatus[1], color: COLORS[3] },
    { name: '已处理事项', value: Math.max(dashboard.voucherTotal + dashboard.claimTotal - dashboard.voucherStatus[0] - dashboard.claimStatus[1], 0), color: COLORS[1] }
  ]

  const profitValue = Math.abs(toNumber(dashboard.netProfit))
  const profitItems = [
    { name: dashboard.netProfit >= 0 ? '净利润' : '净亏损', value: profitValue, color: dashboard.netProfit >= 0 ? COLORS[1] : COLORS[3] },
    { name: '基准值', value: profitValue > 0 ? profitValue * 0.4 : 1, color: '#cbd5e1' }
  ]

  return [
    buildPieCard(`凭证状态（${currentPeriod.value}）`, voucherItems),
    buildPieCard('报销状态', claimItems),
    buildPieCard('待办与已办占比', workloadItems),
    buildPieCard('利润概览', profitItems)
  ]
})

const fetchDashboard = async () => {
  try {
    const tasks = [
      listVouchers({ pageNum: 1, pageSize: 1, period: currentPeriod.value }),
      listVouchers({ pageNum: 1, pageSize: 1, status: 0, period: currentPeriod.value }),
      listVouchers({ pageNum: 1, pageSize: 1, status: 1, period: currentPeriod.value }),
      listVouchers({ pageNum: 1, pageSize: 1, status: 2, period: currentPeriod.value }),
      listVouchers({ pageNum: 1, pageSize: 1, status: 3, period: currentPeriod.value }),
      listExpenseClaims({ pageNum: 1, pageSize: 1 }),
      listExpenseClaims({ pageNum: 1, pageSize: 1, status: 0 }),
      listExpenseClaims({ pageNum: 1, pageSize: 1, status: 1 }),
      listExpenseClaims({ pageNum: 1, pageSize: 1, status: 2 }),
      listExpenseClaims({ pageNum: 1, pageSize: 1, status: 3 }),
      listExpenseClaims({ pageNum: 1, pageSize: 1, status: 4 }),
      getProfitStatement(currentPeriod.value)
    ]

    const [
      voucherTotalResp,
      v0,
      v1,
      v2,
      v3,
      claimTotalResp,
      c0,
      c1,
      c2,
      c3,
      c4,
      profitResp
    ] = await Promise.all(tasks)

    dashboard.voucherTotal = toNumber(voucherTotalResp?.data?.total)
    dashboard.voucherStatus = [
      toNumber(v0?.data?.total),
      toNumber(v1?.data?.total),
      toNumber(v2?.data?.total),
      toNumber(v3?.data?.total)
    ]

    dashboard.claimTotal = toNumber(claimTotalResp?.data?.total)
    dashboard.claimStatus = [
      toNumber(c0?.data?.total),
      toNumber(c1?.data?.total),
      toNumber(c2?.data?.total),
      toNumber(c3?.data?.total),
      toNumber(c4?.data?.total)
    ]

    dashboard.netProfit = toNumber(profitResp?.data?.netProfit)
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '首页数据加载失败')
  }
}

onMounted(() => {
  fetchDashboard()
})
</script>

<style scoped>
.hero-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 16px;
  padding: 20px 24px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.06);
}

.hero-title {
  font-size: 20px;
  font-weight: 800;
  color: #111827;
}

.hero-desc {
  margin-top: 8px;
  color: #64748b;
  line-height: 1.7;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-tags span {
  padding: 8px 14px;
  border-radius: 999px;
  background: linear-gradient(135deg, #dbeafe, #ccfbf1);
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
}

.pie-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.pie-card {
  min-width: 0;
}

.panel-header {
  font-weight: 700;
  color: #111827;
}

.pie-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 14px;
}

.pie {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  position: relative;
  box-shadow: inset 0 0 0 1px rgba(148, 163, 184, 0.25);
}

.pie::after {
  content: '';
  position: absolute;
  inset: 38px;
  border-radius: 50%;
  background: #fff;
}

.pie-center {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
}

.legend-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  border-radius: 10px;
  background: #f8fafc;
}

.legend-left {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #334155;
}

.legend-right {
  color: #0f172a;
  font-weight: 700;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

@media (max-width: 1100px) {
  .pie-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 960px) {
  .hero-panel {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
