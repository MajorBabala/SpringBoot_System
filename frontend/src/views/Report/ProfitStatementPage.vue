<template>
  <div>
    <el-card class="app-card">
      <template #header>
        <div class="card-header">
          <div>
            <div class="page-title">财务报表查询</div>
            <div class="page-subtitle">在同一页面集中查看利润表、资产负债表和科目余额表</div>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="report-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="利润表" name="profit">
          <el-form :inline="true" :model="profitQuery" class="filters-form">
            <el-form-item label="期间">
              <el-input v-model="profitQuery.period" placeholder="YYYYMM" style="width: 160px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="profitLoading" @click="searchProfit">查询</el-button>
            </el-form-item>
          </el-form>

          <div v-if="profitData" class="profit-panel">
            <div class="profit-label">净利润</div>
            <div class="profit-value">{{ formatAmount(profitData.netProfit) }}</div>
            <div class="profit-tip">当前页面支持在同一入口下快速切换各类核心财务报表。</div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="资产负债表" name="balance">
          <div class="balance-shell">
            <div class="balance-hero">
              <div>
                <div class="balance-hero-label">资产结构视图</div>
                <div class="balance-hero-title">资产、负债与权益一屏总览</div>
                <div class="balance-hero-desc">
                  通过分区卡片和分栏表格查看当前期间的财务结构，更适合展示型页面。
                </div>
              </div>
              <el-form :inline="true" :model="balanceQuery" class="filters-form balance-filters">
                <el-form-item label="期间">
                  <el-input v-model="balanceQuery.period" placeholder="YYYYMM" style="width: 160px" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="balanceLoading" @click="searchBalance">查询</el-button>
                </el-form-item>
              </el-form>
            </div>

            <div v-if="balanceData" class="summary-grid balance-summary-grid">
              <div class="summary-card asset-card">
                <span>资产合计</span>
                <strong>{{ formatAmount(balanceData.totalAsset) }}</strong>
                <small>本期可配置资产项目汇总</small>
              </div>
              <div class="summary-card liability-card">
                <span>负债合计</span>
                <strong>{{ formatAmount(balanceData.totalLiability) }}</strong>
                <small>本期负债项目汇总</small>
              </div>
              <div class="summary-card equity-card">
                <span>权益合计</span>
                <strong>{{ formatAmount(balanceData.totalEquity) }}</strong>
                <small>所有者权益汇总</small>
              </div>
              <div class="summary-card accent total-card">
                <span>负债和权益合计</span>
                <strong>{{ formatAmount(balanceData.totalLiabilityAndEquity) }}</strong>
                <small>用于与资产总额对照</small>
              </div>
            </div>

            <div v-if="balanceData" class="tables balance-tables">
              <el-row :gutter="18">
                <el-col :span="8">
                  <div class="table-panel asset-panel">
                    <div class="table-headline">
                      <div class="table-badge">资产</div>
                      <div class="table-caption">流动及非流动资产项目</div>
                    </div>
                    <el-table :data="balanceData.assets" border height="420">
                      <el-table-column prop="subjectName" label="科目" min-width="160" />
                      <el-table-column label="金额" width="140">
                        <template #default="{ row }">
                          {{ formatAmount(row.amount) }}
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="table-panel liability-panel">
                    <div class="table-headline">
                      <div class="table-badge">负债</div>
                      <div class="table-caption">短期及长期负债项目</div>
                    </div>
                    <el-table :data="balanceData.liabilities" border height="420">
                      <el-table-column prop="subjectName" label="科目" min-width="160" />
                      <el-table-column label="金额" width="140">
                        <template #default="{ row }">
                          {{ formatAmount(row.amount) }}
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="table-panel equity-panel">
                    <div class="table-headline">
                      <div class="table-badge">权益</div>
                      <div class="table-caption">资本及留存收益项目</div>
                    </div>
                    <el-table :data="balanceData.equities" border height="420">
                      <el-table-column prop="subjectName" label="科目" min-width="160" />
                      <el-table-column label="金额" width="140">
                        <template #default="{ row }">
                          {{ formatAmount(row.amount) }}
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </el-col>
              </el-row>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="科目余额表" name="subject">
          <el-form :inline="true" :model="subjectQuery" class="filters-form">
            <el-form-item label="期间">
              <el-input v-model="subjectQuery.period" placeholder="YYYYMM" style="width: 160px" clearable />
            </el-form-item>
            <el-form-item label="科目">
              <el-input v-model="subjectQuery.keyword" placeholder="科目编码/名称" style="width: 220px" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="subjectLoading" @click="searchSubject">查询</el-button>
              <el-button @click="resetSubject">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="subjectRows" border stripe v-loading="subjectLoading">
            <el-table-column prop="period" label="期间" width="110" />
            <el-table-column prop="subjectCode" label="科目编码" width="140" />
            <el-table-column prop="subjectName" label="科目名称" min-width="180" />
            <el-table-column label="期初借方" width="140" align="right">
              <template #default="{ row }">{{ formatAmount(row.openingDebit) }}</template>
            </el-table-column>
            <el-table-column label="期初贷方" width="140" align="right">
              <template #default="{ row }">{{ formatAmount(row.openingCredit) }}</template>
            </el-table-column>
            <el-table-column label="本期借方" width="140" align="right">
              <template #default="{ row }">{{ formatAmount(row.debitAmount) }}</template>
            </el-table-column>
            <el-table-column label="本期贷方" width="140" align="right">
              <template #default="{ row }">{{ formatAmount(row.creditAmount) }}</template>
            </el-table-column>
            <el-table-column label="期末借方" width="140" align="right">
              <template #default="{ row }">{{ formatAmount(row.endingDebit) }}</template>
            </el-table-column>
            <el-table-column label="期末贷方" width="140" align="right">
              <template #default="{ row }">{{ formatAmount(row.endingCredit) }}</template>
            </el-table-column>
          </el-table>

          <div class="pager-wrap">
            <el-pagination
              v-model:current-page="subjectQuery.page"
              v-model:page-size="subjectQuery.limit"
              :total="subjectTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchSubjectData"
              @current-change="fetchSubjectData"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listSubjectBalances } from '../../api/ledgers'
import { getBalanceSheet, getProfitStatement } from '../../api/reports'

const route = useRoute()
const router = useRouter()

const activeTab = ref('profit')

const profitLoading = ref(false)
const profitData = ref(null)
const balanceLoading = ref(false)
const balanceData = ref(null)
const subjectLoading = ref(false)
const subjectRows = ref([])
const subjectTotal = ref(0)

const profitQuery = reactive({
  period: ''
})

const balanceQuery = reactive({
  period: ''
})

const subjectQuery = reactive({
  page: 1,
  limit: 10,
  period: '',
  keyword: ''
})

const todayPeriod = () => {
  const date = new Date()
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  return `${year}${month}`
}

const formatAmount = (value) => {
  const num = Number(value || 0)
  return Number.isNaN(num) ? '0.00' : num.toFixed(2)
}

const normalizeTab = (value) => {
  if (value === 'balance' || value === 'subject') return value
  return 'profit'
}

const fetchProfitData = async () => {
  if (!profitQuery.period) {
    ElMessage.warning('请输入会计期间')
    return
  }
  profitLoading.value = true
  try {
    const resp = await getProfitStatement(profitQuery.period)
    if (resp.code === 200) {
      profitData.value = resp.data
      return
    }
    ElMessage.error(resp.message || '利润表查询失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '利润表查询失败')
  } finally {
    profitLoading.value = false
  }
}

const fetchBalanceData = async () => {
  if (!balanceQuery.period) {
    ElMessage.warning('请输入会计期间')
    return
  }
  balanceLoading.value = true
  try {
    const resp = await getBalanceSheet(balanceQuery.period)
    if (resp.code === 200) {
      balanceData.value = resp.data
      return
    }
    ElMessage.error(resp.message || '资产负债表查询失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '资产负债表查询失败')
  } finally {
    balanceLoading.value = false
  }
}

const fetchSubjectData = async () => {
  subjectLoading.value = true
  try {
    const resp = await listSubjectBalances(subjectQuery)
    if (resp.code === 200) {
      subjectRows.value = resp.data?.records || []
      subjectTotal.value = Number(resp.data?.total || 0)
      return
    }
    ElMessage.error(resp.message || '科目余额表查询失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '科目余额表查询失败')
  } finally {
    subjectLoading.value = false
  }
}

const fetchByTab = async (tab) => {
  if (tab === 'balance') {
    await fetchBalanceData()
    return
  }
  if (tab === 'subject') {
    await fetchSubjectData()
    return
  }
  await fetchProfitData()
}

const searchProfit = () => {
  fetchProfitData()
}

const searchBalance = () => {
  fetchBalanceData()
}

const searchSubject = () => {
  subjectQuery.page = 1
  fetchSubjectData()
}

const resetSubject = () => {
  subjectQuery.page = 1
  subjectQuery.limit = 10
  subjectQuery.period = todayPeriod()
  subjectQuery.keyword = ''
  fetchSubjectData()
}

const handleTabChange = async (name) => {
  const tab = normalizeTab(name)
  if (route.query.tab !== tab) {
    await router.replace({
      path: '/home/reports/profit-statement',
      query: tab === 'profit' ? {} : { tab }
    })
    return
  }
  activeTab.value = tab
  fetchByTab(tab)
}

watch(
  () => route.query.tab,
  (value) => {
    const tab = normalizeTab(value)
    activeTab.value = tab
    fetchByTab(tab)
  }
)

onMounted(() => {
  const period = todayPeriod()
  profitQuery.period = period
  balanceQuery.period = period
  subjectQuery.period = period
  const tab = normalizeTab(route.query.tab)
  activeTab.value = tab
  fetchByTab(tab)
})
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-size: 20px;
  font-weight: 800;
  color: #111827;
}

.page-subtitle {
  margin-top: 6px;
  color: #64748b;
  font-size: 13px;
}

.report-tabs {
  margin-top: -8px;
}

.filters-form {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.balance-shell {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.balance-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding: 22px 24px;
  border-radius: 24px;
  background:
    radial-gradient(circle at top left, rgba(37, 99, 235, 0.16), transparent 34%),
    radial-gradient(circle at bottom right, rgba(15, 118, 110, 0.12), transparent 28%),
    linear-gradient(135deg, #f8fbff, #eef7f5);
  border: 1px solid rgba(203, 213, 225, 0.8);
}

.balance-hero-label {
  display: inline-flex;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.12);
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 700;
}

.balance-hero-title {
  margin-top: 12px;
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
}

.balance-hero-desc {
  margin-top: 8px;
  max-width: 560px;
  color: #64748b;
  line-height: 1.8;
  font-size: 14px;
}

.balance-filters {
  margin-bottom: 0;
  padding: 16px 18px 2px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.05);
}

.profit-panel {
  padding: 34px 24px;
  border-radius: 18px;
  background: linear-gradient(135deg, #0f172a, #2563eb);
  color: #fff;
}

.profit-label {
  font-size: 14px;
  opacity: 0.78;
}

.profit-value {
  margin-top: 8px;
  font-size: 42px;
  font-weight: 800;
}

.profit-tip {
  margin-top: 14px;
  max-width: 560px;
  line-height: 1.7;
  font-size: 13px;
  opacity: 0.86;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.summary-card {
  position: relative;
  overflow: hidden;
  padding: 18px 20px;
  border-radius: 22px;
  border: 1px solid rgba(219, 231, 245, 0.95);
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(241, 245, 249, 0.96));
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.06);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.summary-card::before {
  content: '';
  position: absolute;
  top: -42px;
  right: -28px;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  opacity: 0.2;
  background: currentColor;
}

.summary-card span {
  color: #64748b;
  font-size: 13px;
}

.summary-card strong {
  color: #0f172a;
  font-size: 26px;
}

.summary-card small {
  color: #94a3b8;
  font-size: 12px;
}

.summary-card.accent {
  background: linear-gradient(135deg, #dbeafe, #ccfbf1);
}

.asset-card {
  color: #2563eb;
}

.liability-card {
  color: #dc2626;
}

.equity-card {
  color: #0f766e;
}

.total-card {
  color: #7c3aed;
}

.tables {
  margin-top: 8px;
}

.table-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 20px 36px rgba(15, 23, 42, 0.05);
}

.table-title {
  margin-bottom: 8px;
  font-weight: 700;
  color: #111827;
}

.table-headline {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.table-badge {
  display: inline-flex;
  width: fit-content;
  padding: 5px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.table-caption {
  color: #64748b;
  font-size: 13px;
}

.asset-panel .table-badge {
  background: rgba(37, 99, 235, 0.12);
  color: #1d4ed8;
}

.liability-panel .table-badge {
  background: rgba(220, 38, 38, 0.12);
  color: #b91c1c;
}

.equity-panel .table-badge {
  background: rgba(15, 118, 110, 0.12);
  color: #0f766e;
}

:deep(.balance-tables .el-table) {
  --el-table-border-color: rgba(226, 232, 240, 0.9);
  --el-table-header-bg-color: #f8fafc;
  border-radius: 16px;
  overflow: hidden;
}

:deep(.balance-tables .el-table th.el-table__cell) {
  color: #334155;
  font-weight: 700;
}

:deep(.balance-tables .el-table .el-table__row:hover > td.el-table__cell) {
  background: #f8fbff;
}

.pager-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1200px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 960px) {
  .balance-hero {
    flex-direction: column;
  }

  .balance-filters {
    width: 100%;
  }
}
</style>
