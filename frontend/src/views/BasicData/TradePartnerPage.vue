<template>
  <div class="business-page">
    <div class="module-hero">
      <div>
        <div class="module-kicker">Basic Data</div>
        <div class="module-title">往来单位管理</div>
        <div class="module-desc">维护客户、供应商及双向往来单位资料，作为业务单据和辅助核算的基础数据。</div>
      </div>
      <div class="module-badges">
        <span>客户</span>
        <span>供应商</span>
        <span>双向</span>
      </div>
    </div>

    <el-card class="app-card module-card">
      <template #header>
        <div class="card-header">
          <span>往来单位列表</span>
          <el-button type="primary" @click="openCreate">新增单位</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="filters-form">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="编码/名称" style="width: 200px" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.partnerType" clearable placeholder="全部" style="width: 160px">
            <el-option :value="1" label="客户" />
            <el-option :value="2" label="供应商" />
            <el-option :value="3" label="双向往来" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 160px">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="停用" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="fetchList">查询</el-button>
        </el-form-item>
      </el-form>

      <div class="table-shell">
        <el-table :data="tableData" border>
          <el-table-column prop="partnerCode" label="单位编码" width="140" />
          <el-table-column prop="partnerName" label="单位名称" min-width="180" />
          <el-table-column label="类型" width="120">
            <template #default="{ row }">{{ typeText(row.partnerType) }}</template>
          </el-table-column>
          <el-table-column prop="contactName" label="联系人" width="120" />
          <el-table-column prop="phone" label="联系电话" width="150" />
          <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ row.status === 1 ? '启用' : '停用' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-space>
                <el-button size="small" @click="openEdit(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑往来单位' : '新增往来单位'" width="760px">
      <el-form :model="form" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="单位编码">
              <el-input v-model="form.partnerCode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位名称">
              <el-input v-model="form.partnerName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="单位类型">
              <el-select v-model="form.partnerType" style="width: 100%">
                <el-option :value="1" label="客户" />
                <el-option :value="2" label="供应商" />
                <el-option :value="3" label="双向往来" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option :value="1" label="启用" />
                <el-option :value="0" label="停用" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="联系人">
              <el-input v-model="form.contactName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
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
import { createTradePartner, deleteTradePartner, listTradePartners, updateTradePartner } from '../../api/tradePartners'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  partnerType: null,
  status: null
})

const form = reactive({
  id: null,
  partnerCode: '',
  partnerName: '',
  partnerType: 1,
  contactName: '',
  phone: '',
  address: '',
  status: 1,
  remark: ''
})

const typeText = (value) => {
  if (value === 1) return '客户'
  if (value === 2) return '供应商'
  if (value === 3) return '双向往来'
  return '-'
}

const resetForm = () => {
  form.id = null
  form.partnerCode = ''
  form.partnerName = ''
  form.partnerType = 1
  form.contactName = ''
  form.phone = ''
  form.address = ''
  form.status = 1
  form.remark = ''
}

const fetchList = async () => {
  loading.value = true
  try {
    const resp = await listTradePartners(query)
    if (resp.code === 200) {
      tableData.value = resp.data.records || []
      total.value = resp.data.total || 0
      return
    }
    ElMessage.error(resp.message || '往来单位加载失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '往来单位加载失败')
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
  if (!form.partnerCode || !form.partnerName) {
    ElMessage.error('请填写单位编码和单位名称')
    return
  }
  saving.value = true
  try {
    const payload = { ...form }
    const resp = form.id
      ? await updateTradePartner(form.id, payload)
      : await createTradePartner(payload)
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

const remove = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该往来单位吗？', '提示', { type: 'warning' })
    const resp = await deleteTradePartner(id)
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
