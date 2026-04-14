<template>
  <div class="business-page">
    <div class="module-hero">
      <div>
        <div class="module-kicker">Basic Data</div>
        <div class="module-title">账套期间管理</div>
        <div class="module-desc">维护会计期间的起止日期和开关状态，支持开账、结账和期间资料维护。</div>
      </div>
      <div class="module-badges">
        <span>草稿</span>
        <span>已开账</span>
        <span>已结账</span>
      </div>
    </div>

    <el-card class="app-card module-card">
      <template #header>
        <div class="card-header">
          <span>账套期间列表</span>
          <el-button type="primary" @click="openCreate">新增期间</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="filters-form">
        <el-form-item label="期间">
          <el-input v-model="query.keyword" placeholder="YYYYMM" style="width: 180px" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 160px">
            <el-option :value="0" label="草稿" />
            <el-option :value="1" label="已开账" />
            <el-option :value="2" label="已结账" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="fetchList">查询</el-button>
        </el-form-item>
      </el-form>

      <div class="table-shell">
        <el-table :data="tableData" border>
          <el-table-column prop="period" label="会计期间" width="120" />
          <el-table-column prop="startDate" label="开始日期" width="130" />
          <el-table-column prop="endDate" label="结束日期" width="130" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">{{ statusText(row.status) }}</template>
          </el-table-column>
          <el-table-column prop="closeTime" label="结账时间" min-width="180" />
          <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
          <el-table-column label="操作" min-width="260">
            <template #default="{ row }">
              <el-space wrap>
                <el-button size="small" @click="openEdit(row)" :disabled="row.status === 2">编辑</el-button>
                <el-button size="small" type="success" @click="openPeriod(row.id)" v-if="row.status !== 1">开账</el-button>
                <el-button size="small" type="warning" @click="closePeriod(row.id)" v-if="row.status === 1">结账</el-button>
                <el-button size="small" type="danger" @click="remove(row.id)" v-if="row.status === 0">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑会计期间' : '新增会计期间'" width="760px">
      <el-form :model="form" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="会计期间">
              <el-input v-model="form.period" placeholder="YYYYMM" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始日期">
              <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="结束日期">
              <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="form.remark" />
            </el-form-item>
          </el-col>
        </el-row>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
        </div>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  closeAccountingPeriod,
  createAccountingPeriod,
  deleteAccountingPeriod,
  listAccountingPeriods,
  openAccountingPeriod,
  updateAccountingPeriod
} from '../../api/accountingPeriods'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: null
})

const form = reactive({
  id: null,
  period: '',
  startDate: '',
  endDate: '',
  remark: ''
})

const statusText = (value) => {
  if (value === 0) return '草稿'
  if (value === 1) return '已开账'
  if (value === 2) return '已结账'
  return '-'
}

const resetForm = () => {
  form.id = null
  form.period = ''
  form.startDate = ''
  form.endDate = ''
  form.remark = ''
}

const fetchList = async () => {
  loading.value = true
  try {
    const resp = await listAccountingPeriods(query)
    if (resp.code === 200) {
      tableData.value = resp.data.records || []
      total.value = resp.data.total || 0
      return
    }
    ElMessage.error(resp.message || '账套期间加载失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '账套期间加载失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  query.pageNum = page
  fetchList()
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const submit = async () => {
  if (!form.period || !form.startDate || !form.endDate) {
    ElMessage.error('请填写完整期间信息')
    return
  }
  saving.value = true
  try {
    const payload = { ...form }
    const resp = form.id
      ? await updateAccountingPeriod(form.id, payload)
      : await createAccountingPeriod(payload)
    if (resp.code === 200) {
      ElMessage.success(form.id ? '修改成功' : '新增成功')
      dialogVisible.value = false
      fetchList()
      return
    }
    ElMessage.error(resp.message || '保存失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const openPeriod = async (id) => {
  try {
    const resp = await openAccountingPeriod(id)
    if (resp.code === 200) {
      ElMessage.success('开账成功')
      fetchList()
      return
    }
    ElMessage.error(resp.message || '开账失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '开账失败')
  }
}

const closePeriod = async (id) => {
  try {
    const resp = await closeAccountingPeriod(id)
    if (resp.code === 200) {
      ElMessage.success('结账成功')
      fetchList()
      return
    }
    ElMessage.error(resp.message || '结账失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '结账失败')
  }
}

const remove = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该会计期间吗？', '提示', { type: 'warning' })
    const resp = await deleteAccountingPeriod(id)
    if (resp.code === 200) {
      ElMessage.success('删除成功')
      fetchList()
      return
    }
    ElMessage.error(resp.message || '删除失败')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '删除失败')
    }
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.business-page { display: flex; flex-direction: column; gap: 18px; }
.module-hero { display: flex; align-items: flex-start; justify-content: space-between; gap: 18px; padding: 22px 24px; border-radius: 24px; background: radial-gradient(circle at top left, rgba(37, 99, 235, 0.16), transparent 32%), radial-gradient(circle at bottom right, rgba(15, 118, 110, 0.12), transparent 28%), linear-gradient(135deg, #f8fbff, #eef7f5); border: 1px solid rgba(203, 213, 225, 0.8); box-shadow: 0 18px 42px rgba(15, 23, 42, 0.05); }
.module-kicker { display: inline-flex; padding: 5px 10px; border-radius: 999px; background: rgba(37, 99, 235, 0.12); color: #1d4ed8; font-size: 12px; font-weight: 700; }
.module-title { margin-top: 12px; font-size: 28px; font-weight: 800; color: #0f172a; }
.module-desc { margin-top: 8px; max-width: 560px; color: #64748b; line-height: 1.8; font-size: 14px; }
.module-badges { display: flex; flex-wrap: wrap; gap: 10px; }
.module-badges span { padding: 8px 14px; border-radius: 999px; background: rgba(255, 255, 255, 0.88); color: #0f172a; font-size: 13px; font-weight: 700; border: 1px solid rgba(226, 232, 240, 0.9); }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.filters-form { display: flex; gap: 12px; flex-wrap: wrap; margin-bottom: 16px; padding: 16px 18px 2px; border-radius: 18px; background: linear-gradient(145deg, rgba(248, 250, 252, 0.95), rgba(255, 255, 255, 0.95)); border: 1px solid rgba(226, 232, 240, 0.9); }
.table-shell { padding: 12px; border-radius: 22px; background: rgba(255, 255, 255, 0.92); border: 1px solid rgba(226, 232, 240, 0.9); box-shadow: 0 20px 36px rgba(15, 23, 42, 0.05); }
.pagination { margin-top: 12px; display: flex; justify-content: flex-end; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 12px; margin-top: 14px; }
:deep(.el-table) { --el-table-border-color: rgba(226, 232, 240, 0.9); --el-table-header-bg-color: #f8fafc; border-radius: 16px; overflow: hidden; }
:deep(.el-table th.el-table__cell) { color: #334155; font-weight: 700; }
:deep(.el-table .el-table__row:hover > td.el-table__cell) { background: #f8fbff; }
@media (max-width: 960px) { .module-hero { flex-direction: column; } }
</style>
