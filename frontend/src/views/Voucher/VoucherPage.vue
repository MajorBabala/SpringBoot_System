<template>
  <div class="business-page">
    <div class="module-hero">
      <div>
        <div class="module-kicker">Voucher Workspace</div>
        <div class="module-title">凭证业务中心</div>
        <div class="module-desc">统一查看凭证状态、筛选记录并完成审核与记账处理。</div>
      </div>
      <div class="module-badges">
        <span>录入</span>
        <span>审核</span>
        <span>记账</span>
      </div>
    </div>

    <el-card class="app-card module-card">
      <template #header>
        <div class="card-header">
          <span>凭证管理</span>
          <el-button type="primary" @click="openCreate">新增凭证</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="filters-form">
        <el-form-item label="期间">
          <el-input v-model="query.period" placeholder="YYYYMM" style="width: 160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 160px">
            <el-option :value="0" label="未审核" />
            <el-option :value="1" label="已审核" />
            <el-option :value="2" label="已记账" />
            <el-option :value="3" label="已作废" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
        </el-form-item>
      </el-form>

      <div class="table-shell">
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="voucherNo" label="凭证号" min-width="150" />
        <el-table-column prop="period" label="期间" width="120" />
        <el-table-column prop="voucherDate" label="日期" width="140" />
        <el-table-column prop="summary" label="摘要" min-width="180" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            {{ statusText(row.status) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-space>
              <el-button size="small" @click="openDetail(row.id)">
                详情
              </el-button>
              <el-button v-if="row.status === 0" type="primary" size="small" @click="audit(row.id)">
                审核
              </el-button>
              <el-button v-if="row.status === 1" type="success" size="small" @click="bookkeep(row.id)">
                记账
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
      </div>

      <div class="pagination">
        <el-pagination
          background
          layout="prev, pager, next, jumper, total"
          :total="total"
          :current-page="query.pageNum"
          :page-size="query.pageSize"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="createDialogVisible" width="980px" title="新增凭证">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="凭证号" prop="voucherNo">
              <el-input v-model="createForm.voucherNo" placeholder="202604-0001" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="期间" prop="period">
              <el-input v-model="createForm.period" placeholder="YYYYMM" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="日期" prop="voucherDate">
              <el-date-picker
                v-model="createForm.voucherDate"
                type="date"
                placeholder="请选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="摘要">
          <el-input v-model="createForm.summary" placeholder="选填" />
        </el-form-item>

        <div class="entries-title">分录</div>
        <div v-for="(item, idx) in createForm.entries" :key="idx" class="entry-row">
          <el-row :gutter="10">
            <el-col :span="8">
              <el-form-item :label="`科目 ${idx + 1}`" :prop="`entries.${idx}.subjectId`">
                <el-select
                  v-model="item.subjectId"
                  filterable
                  placeholder="请选择科目"
                  style="width: 100%"
                >
                  <el-option
                    v-for="subject in subjectOptions"
                    :key="subject.id"
                    :label="`${subject.subjectCode} - ${subject.subjectName}`"
                    :value="subject.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="5">
              <el-form-item :label="`借方 ${idx + 1}`">
                <el-input
                  v-model="item.debitAmount"
                  placeholder="0.00"
                  inputmode="decimal"
                  @input="item.debitAmount = normalizeAmount(item.debitAmount)"
                />
              </el-form-item>
            </el-col>
            <el-col :span="5">
              <el-form-item :label="`贷方 ${idx + 1}`">
                <el-input
                  v-model="item.creditAmount"
                  placeholder="0.00"
                  inputmode="decimal"
                  @input="item.creditAmount = normalizeAmount(item.creditAmount)"
                />
              </el-form-item>
            </el-col>
            <el-col :span="5">
              <el-form-item label="分录摘要">
                <el-input v-model="item.summary" placeholder="选填" />
              </el-form-item>
            </el-col>
            <el-col :span="1" class="action-col">
              <el-button type="danger" link :disabled="createForm.entries.length <= 1" @click="removeEntry(idx)">
                删除
              </el-button>
            </el-col>
          </el-row>
        </div>

        <div class="entry-actions">
          <el-button @click="addEntry">添加分录</el-button>
        </div>

        <div class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="createLoading" @click="submitCreate">提交</el-button>
        </div>
      </el-form>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" width="900px" title="凭证详情">
      <div v-if="detailLoading" class="detail-loading">加载中...</div>
      <div v-else-if="detailData.voucher">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="凭证号">
            {{ detailData.voucher.voucherNo }}
          </el-descriptions-item>
          <el-descriptions-item label="期间">
            {{ detailData.voucher.period }}
          </el-descriptions-item>
          <el-descriptions-item label="日期">
            {{ detailData.voucher.voucherDate || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            {{ statusText(detailData.voucher.status) }}
          </el-descriptions-item>
          <el-descriptions-item label="制单人 ID">
            {{ detailData.voucher.makerId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="摘要">
            {{ detailData.voucher.summary || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="entries-title">分录详情</div>
        <el-table :data="detailData.entries" border style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="科目" min-width="220">
            <template #default="{ row }">
              {{ subjectLabel(row.subjectId) }}
            </template>
          </el-table-column>
          <el-table-column prop="summary" label="摘要" min-width="180" />
          <el-table-column prop="debitAmount" label="借方" width="140" />
          <el-table-column prop="creditAmount" label="贷方" width="140" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listAccountSubjects } from '../../api/accountSubjects'
import { auditVoucher, bookkeepVoucher, createVoucher, getVoucherDetail, listVouchers } from '../../api/vouchers'

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  status: null,
  period: ''
})

const tableData = ref([])
const total = ref(0)
const createDialogVisible = ref(false)
const createLoading = ref(false)
const createFormRef = ref(null)
const subjectOptions = ref([])
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailData = reactive({
  voucher: null,
  entries: []
})

const createEmptyEntry = () => ({
  subjectId: null,
  summary: '',
  debitAmount: '',
  creditAmount: ''
})

const createForm = reactive({
  voucherNo: '',
  period: '',
  voucherDate: '',
  summary: '',
  entries: [createEmptyEntry()]
})

const createRules = {
  voucherNo: [{ required: true, message: '请输入凭证号', trigger: 'blur' }],
  period: [{ required: true, message: '请输入期间', trigger: 'blur' }],
  voucherDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

const statusText = (status) => {
  if (status === 0) return '未审核'
  if (status === 1) return '已审核'
  if (status === 2) return '已记账'
  if (status === 3) return '已作废'
  return '-'
}

const subjectLabel = (subjectId) => {
  const subject = subjectOptions.value.find((item) => item.id === subjectId)
  if (!subject) return subjectId ?? '-'
  return `${subject.subjectCode} - ${subject.subjectName}`
}

const fetchSubjects = async () => {
  const resp = await listAccountSubjects()
  if (resp.code === 200) {
    subjectOptions.value = resp.data || []
    return
  }
  ElMessage.error(resp.message || '会计科目加载失败')
}

const fetchList = async () => {
  const resp = await listVouchers({
    pageNum: query.pageNum,
    pageSize: query.pageSize,
    status: query.status,
    period: query.period || undefined
  })

  if (resp.code !== 200) {
    ElMessage.error(resp.message || '凭证列表加载失败')
    return
  }

  tableData.value = resp.data.records || []
  total.value = resp.data.total || 0
}

const handlePageChange = (page) => {
  query.pageNum = page
  fetchList()
}

const resetCreateForm = () => {
  createForm.voucherNo = ''
  createForm.period = query.period || ''
  createForm.voucherDate = ''
  createForm.summary = ''
  createForm.entries = [createEmptyEntry()]
  createFormRef.value?.clearValidate()
}

const openCreate = async () => {
  if (subjectOptions.value.length === 0) {
    await fetchSubjects()
  }
  resetCreateForm()
  createDialogVisible.value = true
}

const openDetail = async (id) => {
  detailDialogVisible.value = true
  detailLoading.value = true
  detailData.voucher = null
  detailData.entries = []
  try {
    const resp = await getVoucherDetail(id)
    if (resp.code === 200) {
      detailData.voucher = resp.data?.voucher || null
      detailData.entries = resp.data?.entries || []
      return
    }
    ElMessage.error(resp.message || '凭证详情加载失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '凭证详情加载失败')
  } finally {
    detailLoading.value = false
  }
}

const addEntry = () => {
  createForm.entries.push(createEmptyEntry())
}

const removeEntry = (idx) => {
  createForm.entries.splice(idx, 1)
}

const normalizeAmount = (value) => {
  if (value == null) return ''
  let normalized = String(value).replace(/[^\d.]/g, '')

  const dotIndex = normalized.indexOf('.')
  if (dotIndex !== -1) {
    normalized = normalized.slice(0, dotIndex + 1) + normalized.slice(dotIndex + 1).replace(/\./g, '')
  }

  const [intPartRaw, decimalPart] = normalized.split('.')
  const intPart = intPartRaw ? intPartRaw.replace(/^0+(\d)/, '$1') : '0'
  if (decimalPart === undefined) {
    return intPart
  }
  return `${intPart}.${decimalPart.slice(0, 2)}`
}

const submitCreate = async () => {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return

  const entries = createForm.entries.filter((entry) => entry.subjectId)
  if (entries.length === 0) {
    ElMessage.error('请至少选择一个科目')
    return
  }

  createLoading.value = true
  try {
    const payload = {
      voucherNo: createForm.voucherNo,
      period: createForm.period,
      voucherDate: createForm.voucherDate,
      summary: createForm.summary,
      entries: entries.map((entry) => ({
        subjectId: entry.subjectId,
        summary: entry.summary,
        debitAmount: Number(entry.debitAmount || 0),
        creditAmount: Number(entry.creditAmount || 0)
      }))
    }

    const resp = await createVoucher(payload)
    if (resp.code === 200) {
      ElMessage.success('凭证创建成功')
      createDialogVisible.value = false
      await fetchList()
      return
    }
    ElMessage.error(resp.message || '凭证创建失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '凭证创建失败')
  } finally {
    createLoading.value = false
  }
}

const audit = async (id) => {
  try {
    const resp = await auditVoucher(id)
    if (resp.code === 200) {
      ElMessage.success('凭证审核成功')
      await fetchList()
      return
    }
    ElMessage.error(resp.message || '凭证审核失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '凭证审核失败')
  }
}

const bookkeep = async (id) => {
  try {
    const resp = await bookkeepVoucher(id)
    if (resp.code === 200) {
      ElMessage.success('凭证记账成功')
      await fetchList()
      return
    }
    ElMessage.error(resp.message || '凭证记账失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '凭证记账失败')
  }
}

onMounted(async () => {
  await Promise.all([fetchSubjects(), fetchList()])
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
  max-width: 540px;
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

.filters-form {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
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

.pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.entries-title {
  margin: 16px 0 8px;
  font-weight: 700;
  color: #111827;
}

.entry-row {
  padding: 10px 0;
  border-bottom: 1px dashed var(--el-border-color-base);
}

.entry-actions {
  margin-top: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 14px;
}

.action-col {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.detail-loading {
  padding: 32px 0;
  text-align: center;
  color: #6b7280;
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
