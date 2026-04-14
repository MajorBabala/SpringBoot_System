<template>
  <div class="menu-page app-card">
    <div class="menu-header">
      <div>
        <div class="page-title">菜单管理</div>
        <div class="page-subtitle">维护系统菜单目录、权限标识和层级关系。</div>
      </div>
      <el-button type="primary" @click="openCreateDialog">新增菜单</el-button>
    </div>

    <el-table v-loading="loading" :data="menus" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="menuName" label="菜单名称" min-width="160" />
      <el-table-column label="父级" min-width="120">
        <template #default="{ row }">
          {{ getParentName(row.parentId) }}
        </template>
      </el-table-column>
      <el-table-column prop="path" label="路径" min-width="200" />
      <el-table-column prop="permission" label="权限标识" min-width="180" />
      <el-table-column prop="icon" label="图标" min-width="120" />
      <el-table-column prop="sort" label="排序" width="90" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ typeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-space>
            <el-button size="small" @click="openEditDialog(row.id)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </el-space>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEditMode ? '编辑菜单' : '新增菜单'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="菜单名称">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="父级菜单">
          <el-select v-model="form.parentId" placeholder="请选择父级菜单" clearable style="width: 100%">
            <el-option :value="0" label="根节点" />
            <el-option
              v-for="menu in parentCandidates"
              :key="menu.id"
              :value="menu.id"
              :label="`${menu.menuName} (#${menu.id})`"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model="form.path" placeholder="请输入路由路径（选填）" />
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model="form.permission" placeholder="请输入权限标识（选填）" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="请输入图标（选填）" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" style="width: 100%">
            <el-option :value="0" label="目录" />
            <el-option :value="1" label="菜单" />
            <el-option :value="2" label="按钮" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="handleSave">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createMenu, deleteMenu, getMenuDetail, listMenus, updateMenu } from '../api/menus'

const loading = ref(false)
const menus = ref([])
const parentNameMap = ref({})

const dialogVisible = ref(false)
const isEditMode = ref(false)
const currentEditId = ref(null)

const form = reactive({
  menuName: '',
  parentId: 0,
  path: '',
  permission: '',
  icon: '',
  sort: 0,
  type: 1
})

const parentCandidates = computed(() =>
  menus.value.filter((menu) => menu.id !== currentEditId.value)
)

const typeLabel = (type) => {
  if (type === 0) return '目录'
  if (type === 1) return '菜单'
  if (type === 2) return '按钮'
  return '-'
}

const resetForm = () => {
  form.menuName = ''
  form.parentId = 0
  form.path = ''
  form.permission = ''
  form.icon = ''
  form.sort = 0
  form.type = 1
}

const buildParentMap = () => {
  const map = { 0: '根节点' }
  for (const menu of menus.value) {
    map[menu.id] = menu.menuName
  }
  parentNameMap.value = map
}

const getParentName = (parentId) => {
  const id = parentId ?? 0
  return parentNameMap.value[id] || `#${id}`
}

const fetchMenus = async () => {
  loading.value = true
  try {
    const resp = await listMenus()
    if (resp.code === 200) {
      menus.value = resp.data || []
      buildParentMap()
      return
    }
    ElMessage.error(resp.message || '菜单列表加载失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '菜单列表加载失败')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  isEditMode.value = false
  currentEditId.value = null
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = async (id) => {
  isEditMode.value = true
  currentEditId.value = id
  resetForm()
  try {
    const resp = await getMenuDetail(id)
    if (resp.code === 200) {
      form.menuName = resp.data.menuName || ''
      form.parentId = resp.data.parentId ?? 0
      form.path = resp.data.path || ''
      form.permission = resp.data.permission || ''
      form.icon = resp.data.icon || ''
      form.sort = resp.data.sort ?? 0
      form.type = resp.data.type ?? 1
      dialogVisible.value = true
      return
    }
    ElMessage.error(resp.message || '菜单详情加载失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '菜单详情加载失败')
  }
}

const closeDialog = () => {
  dialogVisible.value = false
}

const handleSave = async () => {
  if (!form.menuName.trim()) {
    ElMessage.error('菜单名称不能为空')
    return
  }

  const payload = {
    menuName: form.menuName.trim(),
    parentId: form.parentId ?? 0,
    path: (form.path || '').trim(),
    permission: (form.permission || '').trim(),
    icon: (form.icon || '').trim(),
    sort: form.sort ?? 0,
    type: form.type
  }

  try {
    const resp = isEditMode.value
      ? await updateMenu(currentEditId.value, payload)
      : await createMenu(payload)
    if (resp.code === 200) {
      ElMessage.success(isEditMode.value ? '菜单更新成功' : '菜单创建成功')
      closeDialog()
      fetchMenus()
      return
    }
    ElMessage.error(resp.message || '菜单保存失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '菜单保存失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除菜单【${row.menuName}】吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }

  try {
    const resp = await deleteMenu(row.id)
    if (resp.code === 200) {
      ElMessage.success('菜单删除成功')
      fetchMenus()
      return
    }
    ElMessage.error(resp.message || '菜单删除失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '菜单删除失败')
  }
}

onMounted(() => {
  fetchMenus()
})
</script>

<style scoped>
.menu-page {
  padding: 20px;
}

.menu-header {
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
</style>
