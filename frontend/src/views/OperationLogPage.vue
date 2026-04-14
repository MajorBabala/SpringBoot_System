<template>
  <div class="log-page app-card">
    <div class="log-header">
      <div>
        <div class="page-title">操作日志</div>
        <div class="page-subtitle">查看系统接口操作轨迹，支持筛选与导出。</div>
      </div>
      <el-button type="primary" @click="handleExport">导出日志</el-button>
    </div>

    <el-form :inline="true" :model="searchForm" class="filters-form">
      <el-form-item label="用户名">
        <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
      </el-form-item>
      <el-form-item label="操作">
        <el-input v-model="searchForm.operation" placeholder="请输入操作关键字" clearable />
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="searchForm.timeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DDTHH:mm:ss"
          style="width: 360px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="logs" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="130" />
      <el-table-column prop="operation" label="操作" min-width="220" />
      <el-table-column prop="method" label="方法" min-width="180" />
      <el-table-column prop="ip" label="IP" width="140" />
      <el-table-column prop="duration" label="耗时(ms)" width="100" />
      <el-table-column prop="createTime" label="时间" min-width="170" />
      <el-table-column prop="params" label="参数" min-width="220" show-overflow-tooltip />
    </el-table>

    <div class="pagination">
      <el-pagination
        background
        layout="prev, pager, next, jumper, total"
        :total="total"
        :current-page="page"
        :page-size="limit"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { exportOperationLogs, listOperationLogs } from '../api/operationLogs'

const loading = ref(false)
const logs = ref([])
const page = ref(1)
const limit = ref(10)
const total = ref(0)

const searchForm = reactive({
  username: '',
  operation: '',
  timeRange: []
})

const extractDateRange = () => {
  const range = searchForm.timeRange || []
  return {
    dateFrom: range[0] || null,
    dateTo: range[1] || null
  }
}

const fetchLogs = async () => {
  loading.value = true
  try {
    const { dateFrom, dateTo } = extractDateRange()
    const resp = await listOperationLogs(
      page.value,
      limit.value,
      searchForm.username || null,
      searchForm.operation || null,
      dateFrom,
      dateTo
    )
    if (resp.code === 200) {
      logs.value = resp.data.records || []
      total.value = resp.data.total || 0
      return
    }
    ElMessage.error(resp.message || '日志列表加载失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '日志列表加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  fetchLogs()
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.operation = ''
  searchForm.timeRange = []
  page.value = 1
  fetchLogs()
}

const handlePageChange = (value) => {
  page.value = value
  fetchLogs()
}

const handleExport = async () => {
  try {
    const { dateFrom, dateTo } = extractDateRange()
    const resp = await exportOperationLogs(
      searchForm.username || null,
      searchForm.operation || null,
      dateFrom,
      dateTo
    )
    const blob = new Blob([resp], { type: 'text/csv;charset=utf-8;' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `operation-logs-${new Date().toISOString().slice(0, 10)}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('日志导出成功')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '日志导出失败')
  }
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.log-page {
  padding: 20px;
}

.log-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
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

.filters-form {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.pagination {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}
</style>
