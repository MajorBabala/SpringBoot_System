<template>
  <div class="business-page">
    <div class="module-hero">
      <div>
        <div class="module-kicker">Ledger Workspace</div>
        <div class="module-title">账簿查询中心</div>
        <div class="module-desc">通过统一入口查看总账、明细账与序时账，并保持一致的业务分析体验。</div>
      </div>
      <div class="module-badges">
        <span>总账</span>
        <span>明细账</span>
        <span>序时账</span>
      </div>
    </div>

    <el-card class="app-card module-card">
      <template #header>
        <div class="card-header">
          <div>
            <div class="page-title">账簿查询</div>
            <div class="page-subtitle">在同一页面集中查看总账、明细账和序时账</div>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="ledger-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="总账查询" name="general">
          <el-form :inline="true" :model="generalQuery" class="filters-form">
            <el-form-item label="期间">
              <el-input v-model="generalQuery.period" placeholder="YYYYMM" style="width: 160px" clearable />
            </el-form-item>
            <el-form-item label="科目">
              <el-input v-model="generalQuery.keyword" placeholder="科目编码/名称" style="width: 220px" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="generalLoading" @click="searchGeneral">查询</el-button>
            </el-form-item>
          </el-form>

          <div class="table-shell">
          <el-table :data="generalRows" border stripe v-loading="generalLoading">
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
          </div>

          <div class="pager-wrap">
            <el-pagination
              v-model:current-page="generalQuery.page"
              v-model:page-size="generalQuery.limit"
              :total="generalTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchGeneralData"
              @current-change="fetchGeneralData"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="明细账查询" name="detail">
          <el-form :inline="true" :model="detailQuery" class="filters-form">
            <el-form-item label="期间">
              <el-input v-model="detailQuery.period" placeholder="YYYYMM" style="width: 160px" clearable />
            </el-form-item>
            <el-form-item label="科目">
              <el-select
                v-model="detailQuery.subjectId"
                filterable
                clearable
                placeholder="请选择科目"
                style="width: 260px"
              >
                <el-option
                  v-for="subject in subjectOptions"
                  :key="subject.id"
                  :label="`${subject.subjectCode} - ${subject.subjectName}`"
                  :value="subject.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="detailLoading" @click="searchDetail">查询</el-button>
            </el-form-item>
          </el-form>

          <div class="table-shell">
          <el-table :data="detailRows" border stripe v-loading="detailLoading">
            <el-table-column prop="voucherDate" label="凭证日期" width="120" />
            <el-table-column prop="voucherNo" label="凭证号" width="140" />
            <el-table-column prop="summary" label="摘要" min-width="220" show-overflow-tooltip />
            <el-table-column label="借方" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.debitAmount) }}</template>
            </el-table-column>
            <el-table-column label="贷方" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.creditAmount) }}</template>
            </el-table-column>
            <el-table-column label="累计借方" width="130" align="right">
              <template #default="{ row }">{{ formatAmount(row.runningDebit) }}</template>
            </el-table-column>
            <el-table-column label="累计贷方" width="130" align="right">
              <template #default="{ row }">{{ formatAmount(row.runningCredit) }}</template>
            </el-table-column>
          </el-table>
          </div>

          <div class="pager-wrap">
            <el-pagination
              v-model:current-page="detailQuery.page"
              v-model:page-size="detailQuery.limit"
              :total="detailTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchDetailData"
              @current-change="fetchDetailData"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="序时账查询" name="journal">
          <el-form :inline="true" :model="journalQuery" class="filters-form">
            <el-form-item label="期间">
              <el-input v-model="journalQuery.period" placeholder="YYYYMM" style="width: 160px" clearable />
            </el-form-item>
            <el-form-item label="科目">
              <el-input v-model="journalQuery.keyword" placeholder="科目编码/名称" style="width: 220px" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="journalLoading" @click="searchJournal">查询</el-button>
            </el-form-item>
          </el-form>

          <div class="table-shell">
          <el-table :data="journalRows" border stripe v-loading="journalLoading">
            <el-table-column prop="voucherDate" label="凭证日期" width="120" />
            <el-table-column prop="voucherNo" label="凭证号" width="140" />
            <el-table-column prop="subjectCode" label="科目编码" width="140" />
            <el-table-column prop="subjectName" label="科目名称" min-width="160" />
            <el-table-column prop="summary" label="摘要" min-width="220" show-overflow-tooltip />
            <el-table-column label="借方" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.debitAmount) }}</template>
            </el-table-column>
            <el-table-column label="贷方" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.creditAmount) }}</template>
            </el-table-column>
          </el-table>
          </div>

          <div class="pager-wrap">
            <el-pagination
              v-model:current-page="journalQuery.page"
              v-model:page-size="journalQuery.limit"
              :total="journalTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchJournalData"
              @current-change="fetchJournalData"
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
import { listAccountSubjects } from '../../api/accountSubjects'
import { listDetailLedgers, listGeneralLedgers, listJournalEntries } from '../../api/ledgers'

const route = useRoute()
const router = useRouter()

const activeTab = ref('general')
const subjectOptions = ref([])

const generalLoading = ref(false)
const generalRows = ref([])
const generalTotal = ref(0)
const detailLoading = ref(false)
const detailRows = ref([])
const detailTotal = ref(0)
const journalLoading = ref(false)
const journalRows = ref([])
const journalTotal = ref(0)

const generalQuery = reactive({
  page: 1,
  limit: 10,
  period: '',
  keyword: ''
})

const detailQuery = reactive({
  page: 1,
  limit: 10,
  period: '',
  subjectId: null
})

const journalQuery = reactive({
  page: 1,
  limit: 10,
  period: '',
  keyword: ''
})

const todayPeriod = () => {
  const date = new Date()
  return `${date.getFullYear()}${String(date.getMonth() + 1).padStart(2, '0')}`
}

const formatAmount = (value) => {
  const num = Number(value || 0)
  return Number.isNaN(num) ? '0.00' : num.toFixed(2)
}

const normalizeTab = (value) => {
  if (value === 'detail' || value === 'journal') return value
  return 'general'
}

const fetchSubjects = async () => {
  const resp = await listAccountSubjects()
  if (resp.code === 200) {
    subjectOptions.value = resp.data || []
    if (!detailQuery.subjectId && subjectOptions.value.length > 0) {
      detailQuery.subjectId = subjectOptions.value[0].id
    }
    return
  }
  ElMessage.error(resp.message || '会计科目加载失败')
}

const fetchGeneralData = async () => {
  generalLoading.value = true
  try {
    const resp = await listGeneralLedgers(generalQuery)
    if (resp.code === 200) {
      generalRows.value = resp.data?.records || []
      generalTotal.value = Number(resp.data?.total || 0)
      return
    }
    ElMessage.error(resp.message || '总账查询失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '总账查询失败')
  } finally {
    generalLoading.value = false
  }
}

const fetchDetailData = async () => {
  if (!detailQuery.subjectId) {
    detailRows.value = []
    detailTotal.value = 0
    return
  }
  detailLoading.value = true
  try {
    const resp = await listDetailLedgers(detailQuery)
    if (resp.code === 200) {
      detailRows.value = resp.data?.records || []
      detailTotal.value = Number(resp.data?.total || 0)
      return
    }
    ElMessage.error(resp.message || '明细账查询失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '明细账查询失败')
  } finally {
    detailLoading.value = false
  }
}

const fetchJournalData = async () => {
  journalLoading.value = true
  try {
    const resp = await listJournalEntries(journalQuery)
    if (resp.code === 200) {
      journalRows.value = resp.data?.records || []
      journalTotal.value = Number(resp.data?.total || 0)
      return
    }
    ElMessage.error(resp.message || '序时账查询失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '序时账查询失败')
  } finally {
    journalLoading.value = false
  }
}

const fetchByTab = async (tab) => {
  if (tab === 'detail') {
    await fetchDetailData()
    return
  }
  if (tab === 'journal') {
    await fetchJournalData()
    return
  }
  await fetchGeneralData()
}

const searchGeneral = () => {
  generalQuery.page = 1
  fetchGeneralData()
}

const searchDetail = () => {
  detailQuery.page = 1
  fetchDetailData()
}

const searchJournal = () => {
  journalQuery.page = 1
  fetchJournalData()
}

const handleTabChange = async (name) => {
  const tab = normalizeTab(name)
  if (route.query.tab !== tab) {
    await router.replace({
      path: '/home/ledgers/general',
      query: tab === 'general' ? {} : { tab }
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

onMounted(async () => {
  const period = todayPeriod()
  generalQuery.period = period
  detailQuery.period = period
  journalQuery.period = period
  await fetchSubjects()
  const tab = normalizeTab(route.query.tab)
  activeTab.value = tab
  fetchByTab(tab)
})
</script>

<style scoped>
.business-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.module-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding: 22px 24px;
  border-radius: 24px;
  background:
    radial-gradient(circle at top left, rgba(37, 99, 235, 0.16), transparent 32%),
    radial-gradient(circle at bottom right, rgba(15, 118, 110, 0.12), transparent 28%),
    linear-gradient(135deg, #f8fbff, #eef7f5);
  border: 1px solid rgba(203, 213, 225, 0.8);
  box-shadow: 0 18px 42px rgba(15, 23, 42, 0.05);
}

.module-kicker {
  display: inline-flex;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.12);
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 700;
}

.module-title {
  margin-top: 12px;
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
}

.module-desc {
  margin-top: 8px;
  max-width: 560px;
  color: #64748b;
  line-height: 1.8;
  font-size: 14px;
}

.module-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.module-badges span {
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.88);
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
  border: 1px solid rgba(226, 232, 240, 0.9);
}

.module-card {
  overflow: hidden;
}

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

.ledger-tabs {
  margin-top: -8px;
}

.filters-form {
  margin-bottom: 16px;
  padding: 16px 18px 2px;
  border-radius: 18px;
  background: linear-gradient(145deg, rgba(248, 250, 252, 0.95), rgba(255, 255, 255, 0.95));
  border: 1px solid rgba(226, 232, 240, 0.9);
}

.table-shell {
  padding: 12px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 20px 36px rgba(15, 23, 42, 0.05);
}

.pager-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table) {
  --el-table-border-color: rgba(226, 232, 240, 0.9);
  --el-table-header-bg-color: #f8fafc;
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-table th.el-table__cell) {
  color: #334155;
  font-weight: 700;
}

:deep(.el-table .el-table__row:hover > td.el-table__cell) {
  background: #f8fbff;
}

@media (max-width: 960px) {
  .module-hero {
    flex-direction: column;
  }
}
</style>
