<template>
  <div class="business-page">
    <div class="module-hero">
      <div>
        <div class="module-kicker">Expense Workspace</div>
        <div class="module-title">费用报销中心</div>
        <div class="module-desc">围绕报销申请、审批流转、附件管理和凭证生成的一体化处理界面。</div>
      </div>
      <div class="module-badges">
        <span>申请</span>
        <span>审批</span>
        <span>付款</span>
      </div>
    </div>

    <el-card class="app-card module-card">
      <template #header>
        <div class="card-header">
          <span>费用报销</span>
          <el-button type="primary" @click="openCreate">新增报销单</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="filters-form">
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 160px">
            <el-option :value="0" label="草稿" />
            <el-option :value="1" label="待审批" />
            <el-option :value="2" label="已审批" />
            <el-option :value="3" label="已付款" />
            <el-option :value="4" label="已驳回" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="fetchList">查询</el-button>
        </el-form-item>
      </el-form>

      <div class="table-shell">
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="claimNo" label="报销单号" min-width="150" />
        <el-table-column prop="period" label="期间" width="110" />
        <el-table-column prop="reason" label="事由" min-width="220" />
        <el-table-column label="总金额" width="140">
          <template #default="{ row }">
            {{ formatAmount(row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            {{ statusText(row.status) }}
          </template>
        </el-table-column>
        <el-table-column label="关联凭证" width="120">
          <template #default="{ row }">
            {{ row.voucherId || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="420">
          <template #default="{ row }">
            <el-space wrap>
              <el-button size="small" @click="openDetail(row.id)">详情</el-button>
              <el-button v-if="row.status === 0" type="primary" size="small" @click="submitClaim(row.id)">
                提交
              </el-button>
              <el-button v-if="row.status === 1" type="success" size="small" @click="approveClaim(row.id)">
                审批
              </el-button>
              <el-button
                v-if="(row.status === 2 || row.status === 3) && !row.voucherId"
                type="warning"
                size="small"
                @click="generateVoucher(row.id)"
              >
                生成凭证
              </el-button>
              <el-button v-if="row.status === 2" type="primary" size="small" @click="payClaim(row.id)">
                付款
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

    <el-dialog v-model="createDialogVisible" width="980px" title="新增费用报销单">
      <el-form :model="createForm" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="报销单号">
              <el-input v-model="createForm.claimNo" placeholder="EXP-0001" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="会计期间">
              <el-input v-model="createForm.period" placeholder="YYYYMM" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="部门">
              <el-select v-model="createForm.deptId" filterable placeholder="请选择部门" style="width: 100%">
                <el-option
                  v-for="dept in deptOptions"
                  :key="dept.id"
                  :label="dept.deptName"
                  :value="dept.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="费用类型">
              <el-input-number v-model="createForm.expenseType" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="报销事由">
          <el-input v-model="createForm.reason" placeholder="请输入报销事由" />
        </el-form-item>

        <div class="entries-title">报销明细</div>

        <div v-for="(item, idx) in createForm.details" :key="idx" class="entry-row">
          <el-row :gutter="10">
            <el-col :span="6">
              <el-form-item :label="`项目 ${idx + 1}`">
                <el-input v-model="item.itemName" placeholder="例如：差旅、餐饮、住宿" />
              </el-form-item>
            </el-col>
            <el-col :span="7">
              <el-form-item label="科目">
                <el-select v-model="item.subjectId" filterable placeholder="请选择科目" style="width: 100%">
                  <el-option
                    v-for="subject in subjectOptions"
                    :key="subject.id"
                    :label="`${subject.subjectCode} - ${subject.subjectName}`"
                    :value="subject.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="金额">
                <el-input-number v-model="item.amount" :min="0" :step="0.01" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="备注">
                <el-input v-model="item.remark" placeholder="选填" />
              </el-form-item>
            </el-col>
            <el-col :span="1" class="action-col">
              <el-button type="danger" link :disabled="createForm.details.length <= 1" @click="removeDetail(idx)">
                删除
              </el-button>
            </el-col>
          </el-row>
        </div>

        <div class="entry-actions">
          <el-button @click="addDetail">添加明细</el-button>
        </div>

        <div class="total-row">
          <span>合计：</span>
          <strong>{{ formatAmount(totalAmountComputed) }}</strong>
        </div>

        <div class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="createLoading" @click="submitCreate">保存</el-button>
        </div>
      </el-form>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" width="980px" title="报销单详情">
      <div v-if="detailLoading" class="detail-loading">加载中...</div>
      <div v-else-if="detailData.claim">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="报销单号">{{ detailData.claim.claimNo }}</el-descriptions-item>
          <el-descriptions-item label="期间">{{ detailData.claim.period || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusText(detailData.claim.status) }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ deptLabel(detailData.claim.deptId) }}</el-descriptions-item>
          <el-descriptions-item label="申请人 ID">{{ detailData.claim.applicantId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="事由">{{ detailData.claim.reason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="总金额">{{ formatAmount(detailData.claim.totalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="关联凭证 ID">{{ detailData.claim.voucherId || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="entries-title">明细列表</div>
        <el-table :data="detailData.details" border style="width: 100%">
          <el-table-column prop="lineNo" label="行号" width="80" />
          <el-table-column prop="itemName" label="项目" min-width="180" />
          <el-table-column label="科目" min-width="220">
            <template #default="{ row }">
              {{ subjectLabel(row.subjectId) }}
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="140" />
          <el-table-column prop="remark" label="备注" min-width="160" />
        </el-table>

        <div class="entries-title">附件</div>
        <div class="attachment-actions">
          <el-upload
            :show-file-list="false"
            :http-request="handleAttachmentUpload"
            accept="image/*,.pdf,.doc,.docx,.xls,.xlsx,.zip,.rar"
          >
            <el-button type="primary" plain>上传附件</el-button>
          </el-upload>
        </div>
        <el-table :data="detailData.attachments" border style="width: 100%">
          <el-table-column prop="fileName" label="文件名" min-width="260" />
          <el-table-column prop="contentType" label="类型" width="180" />
          <el-table-column label="大小(KB)" width="120">
            <template #default="{ row }">
              {{ formatFileSize(row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column prop="uploadTime" label="上传时间" min-width="180" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="danger" link @click="removeAttachment(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listAccountSubjects } from '../../api/accountSubjects'
import { listDepts } from '../../api/depts'
import {
  approveExpenseClaim,
  createExpenseClaim,
  deleteExpenseClaimAttachment,
  generateExpenseClaimVoucher,
  getExpenseClaimDetail,
  listExpenseClaimAttachments,
  listExpenseClaims,
  payExpenseClaim,
  submitExpenseClaim,
  uploadExpenseClaimAttachment
} from '../../api/expenseClaims'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const createDialogVisible = ref(false)
const createLoading = ref(false)
const subjectOptions = ref([])
const deptOptions = ref([])
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailData = reactive({
  claim: null,
  details: [],
  attachments: []
})

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  status: null
})

const createEmptyDetail = () => ({
  itemName: '',
  subjectId: null,
  amount: 0,
  remark: ''
})

const createForm = reactive({
  claimNo: '',
  period: '',
  reason: '',
  deptId: 1,
  expenseType: 0,
  details: [createEmptyDetail()]
})

const totalAmountComputed = computed(() =>
  createForm.details.reduce((sum, detail) => sum + (Number(detail.amount) || 0), 0)
)

const formatAmount = (value) => {
  const num = Number(value || 0)
  return Number.isNaN(num) ? '0.00' : num.toFixed(2)
}

const formatFileSize = (value) => {
  const size = Number(value || 0)
  if (Number.isNaN(size) || size <= 0) return '0.00'
  return (size / 1024).toFixed(2)
}

const todayPeriod = () => {
  const date = new Date()
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  return `${year}${month}`
}

const statusText = (status) => {
  if (status === 0) return '草稿'
  if (status === 1) return '待审批'
  if (status === 2) return '已审批'
  if (status === 3) return '已付款'
  if (status === 4) return '已驳回'
  return '-'
}

const subjectLabel = (subjectId) => {
  const subject = subjectOptions.value.find((item) => item.id === subjectId)
  if (!subject) return subjectId ?? '-'
  return `${subject.subjectCode} - ${subject.subjectName}`
}

const deptLabel = (deptId) => {
  const dept = deptOptions.value.find((item) => item.id === deptId)
  return dept ? dept.deptName : deptId ?? '-'
}

const fetchSubjects = async () => {
  const resp = await listAccountSubjects()
  if (resp.code === 200) {
    subjectOptions.value = resp.data || []
    return
  }
  ElMessage.error(resp.message || '会计科目加载失败')
}

const fetchDepts = async () => {
  const resp = await listDepts()
  if (resp.code === 200) {
    deptOptions.value = resp.data || []
    if (!createForm.deptId && deptOptions.value.length > 0) {
      createForm.deptId = deptOptions.value[0].id
    }
    return
  }
  ElMessage.error(resp.message || '部门列表加载失败')
}

const fetchList = async () => {
  loading.value = true
  try {
    const resp = await listExpenseClaims({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      status: query.status
    })
    if (resp.code === 200) {
      tableData.value = resp.data.records || []
      total.value = resp.data.total || 0
      return
    }
    ElMessage.error(resp.message || '报销单列表加载失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '报销单列表加载失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  query.pageNum = page
  fetchList()
}

const openCreate = async () => {
  if (subjectOptions.value.length === 0) {
    await fetchSubjects()
  }
  if (deptOptions.value.length === 0) {
    await fetchDepts()
  }
  createForm.claimNo = ''
  createForm.period = todayPeriod()
  createForm.reason = ''
  createForm.deptId = deptOptions.value[0]?.id || 1
  createForm.expenseType = 0
  createForm.details = [createEmptyDetail()]
  createDialogVisible.value = true
}

const refreshAttachments = async () => {
  if (!detailData.claim?.id) return
  try {
    const resp = await listExpenseClaimAttachments(detailData.claim.id)
    if (resp.code === 200) {
      detailData.attachments = resp.data || []
      return
    }
    ElMessage.error(resp.message || '附件列表加载失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '附件列表加载失败')
  }
}

const openDetail = async (id) => {
  detailDialogVisible.value = true
  detailLoading.value = true
  detailData.claim = null
  detailData.details = []
  detailData.attachments = []
  try {
    const resp = await getExpenseClaimDetail(id)
    if (resp.code === 200) {
      detailData.claim = resp.data?.claim || null
      detailData.details = resp.data?.details || []
      detailData.attachments = resp.data?.attachments || []
      return
    }
    ElMessage.error(resp.message || '报销单详情加载失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '报销单详情加载失败')
  } finally {
    detailLoading.value = false
  }
}

const addDetail = () => {
  createForm.details.push(createEmptyDetail())
}

const removeDetail = (idx) => {
  createForm.details.splice(idx, 1)
}

const submitCreate = async () => {
  if (!createForm.claimNo) {
    ElMessage.error('请输入报销单号')
    return
  }
  if (!createForm.period) {
    ElMessage.error('请输入会计期间')
    return
  }
  if (!createForm.reason) {
    ElMessage.error('请输入报销事由')
    return
  }

  const details = createForm.details
    .filter((detail) => detail.itemName && detail.subjectId)
    .map((detail) => ({
      itemName: detail.itemName,
      subjectId: detail.subjectId,
      amount: Number(detail.amount || 0),
      expenseType: createForm.expenseType,
      remark: detail.remark
    }))

  if (details.length === 0) {
    ElMessage.error('请至少添加一条有效明细')
    return
  }

  createLoading.value = true
  try {
    const payload = {
      claimNo: createForm.claimNo,
      period: createForm.period,
      reason: createForm.reason,
      deptId: Number(createForm.deptId),
      expenseType: Number(createForm.expenseType),
      totalAmount: totalAmountComputed.value,
      details
    }
    const resp = await createExpenseClaim(payload)
    if (resp.code === 200) {
      ElMessage.success('报销单创建成功')
      createDialogVisible.value = false
      await fetchList()
      return
    }
    ElMessage.error(resp.message || '报销单创建失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '报销单创建失败')
  } finally {
    createLoading.value = false
  }
}

const submitClaim = async (id) => {
  try {
    const resp = await submitExpenseClaim(id)
    if (resp.code === 200) {
      ElMessage.success('报销单提交成功')
      await fetchList()
      return
    }
    ElMessage.error(resp.message || '报销单提交失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '报销单提交失败')
  }
}

const approveClaim = async (id) => {
  try {
    const resp = await approveExpenseClaim(id)
    if (resp.code === 200) {
      ElMessage.success('报销单审批成功')
      await fetchList()
      return
    }
    ElMessage.error(resp.message || '报销单审批失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '报销单审批失败')
  }
}

const generateVoucher = async (id) => {
  try {
    const resp = await generateExpenseClaimVoucher(id)
    if (resp.code === 200) {
      ElMessage.success(`凭证生成成功，凭证ID：${resp.data}`)
      await fetchList()
      if (detailData.claim?.id === id) {
        await openDetail(id)
      }
      return
    }
    ElMessage.error(resp.message || '生成凭证失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '生成凭证失败')
  }
}

const payClaim = async (id) => {
  try {
    const resp = await payExpenseClaim(id)
    if (resp.code === 200) {
      ElMessage.success('报销单付款成功')
      await fetchList()
      return
    }
    ElMessage.error(resp.message || '报销单付款失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '报销单付款失败')
  }
}

const handleAttachmentUpload = async ({ file }) => {
  if (!detailData.claim?.id) {
    ElMessage.error('请先打开报销单详情')
    return
  }
  try {
    const resp = await uploadExpenseClaimAttachment(detailData.claim.id, file)
    if (resp.code === 200) {
      ElMessage.success('附件上传成功')
      await refreshAttachments()
      return
    }
    ElMessage.error(resp.message || '附件上传失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '附件上传失败')
  }
}

const removeAttachment = async (attachmentId) => {
  if (!detailData.claim?.id) return
  try {
    const resp = await deleteExpenseClaimAttachment(detailData.claim.id, attachmentId)
    if (resp.code === 200) {
      ElMessage.success('附件删除成功')
      await refreshAttachments()
      return
    }
    ElMessage.error(resp.message || '附件删除失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '附件删除失败')
  }
}

onMounted(async () => {
  await Promise.all([fetchSubjects(), fetchDepts(), fetchList()])
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

.total-row {
  margin-top: 12px;
  padding: 10px 12px;
  border: 1px solid var(--el-border-color-base);
  border-radius: 12px;
  background: #fff;
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: flex-end;
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

.attachment-actions {
  margin-bottom: 10px;
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
