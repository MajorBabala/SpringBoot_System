<template>
  <div class="role-page app-card">
    <div class="role-header">
      <div>
        <div class="page-title">角色管理</div>
        <div class="page-subtitle">维护角色基础信息，并可分配可见菜单。</div>
      </div>
      <el-button type="primary" @click="openAddDialog">新增角色</el-button>
    </div>

    <el-form :inline="true" :model="searchForm" class="filters-form">
      <el-form-item label="角色名称">
        <el-input v-model="searchForm.roleName" placeholder="请输入角色名称" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 160px">
          <el-option :value="1" label="启用" />
          <el-option :value="0" label="禁用" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="roleList" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="roleName" label="角色名称" min-width="160" />
      <el-table-column prop="roleCode" label="角色编码" min-width="180" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-space>
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="primary" plain @click="openMenuDialog(row)">分配菜单</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </el-space>
        </template>
      </el-table-column>
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

    <el-dialog v-model="showDialog" :title="isEditMode ? '编辑角色' : '新增角色'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="角色名称">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="handleSave">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showMenuDialog" title="分配菜单" width="600px">
      <div class="menu-role-name">角色：{{ currentRoleName }}</div>
      <el-tree
        ref="menuTreeRef"
        :data="menuTreeData"
        node-key="id"
        show-checkbox
        default-expand-all
        :default-checked-keys="checkedMenuIds"
        :props="{ label: 'menuName', children: 'children' }"
      />
      <template #footer>
        <el-button @click="closeMenuDialog">取消</el-button>
        <el-button type="primary" @click="handleSaveMenus">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { assignRoleMenus, createRole, deleteRole, getRoleMenus, listRoles, updateRole } from '../api/roles'
import { listMenus } from '../api/menus'

const roleList = ref([])
const page = ref(1)
const limit = ref(10)
const total = ref(0)
const loading = ref(false)
const showDialog = ref(false)
const isEditMode = ref(false)
const currentEditId = ref(null)

const searchForm = reactive({
  roleName: '',
  status: null
})

const form = reactive({
  roleName: '',
  roleCode: '',
  status: 1
})

const showMenuDialog = ref(false)
const currentRoleId = ref(null)
const currentRoleName = ref('')
const checkedMenuIds = ref([])
const menuTreeData = ref([])
const menuTreeRef = ref()

onMounted(async () => {
  await fetchRoles()
  await fetchMenuTree()
})

const fetchRoles = async () => {
  loading.value = true
  try {
    const resp = await listRoles(page.value, limit.value, searchForm.roleName || null, searchForm.status)
    if (resp.code === 200) {
      roleList.value = resp.data.records || []
      total.value = resp.data.total || 0
      return
    }
    ElMessage.error(resp.message || '角色列表加载失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '角色列表加载失败')
  } finally {
    loading.value = false
  }
}

const fetchMenuTree = async () => {
  try {
    const resp = await listMenus()
    if (resp.code === 200) {
      menuTreeData.value = buildMenuTree(resp.data || [])
    } else {
      ElMessage.error(resp.message || '菜单列表加载失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '菜单列表加载失败')
  }
}

const buildMenuTree = (flatMenus) => {
  const map = new Map()
  flatMenus.forEach((item) => {
    map.set(item.id, { ...item, children: [] })
  })

  const roots = []
  map.forEach((item) => {
    const parentId = item.parentId ?? 0
    if (parentId === 0 || !map.has(parentId)) {
      roots.push(item)
    } else {
      map.get(parentId).children.push(item)
    }
  })

  const sortFn = (a, b) => (a.sort ?? 0) - (b.sort ?? 0) || (a.id ?? 0) - (b.id ?? 0)
  const dfsSort = (nodes) => {
    nodes.sort(sortFn)
    nodes.forEach((node) => dfsSort(node.children))
  }
  dfsSort(roots)
  return roots
}

const handleSearch = () => {
  page.value = 1
  fetchRoles()
}

const handleReset = () => {
  searchForm.roleName = ''
  searchForm.status = null
  page.value = 1
  fetchRoles()
}

const handlePageChange = (value) => {
  page.value = value
  fetchRoles()
}

const openAddDialog = () => {
  isEditMode.value = false
  currentEditId.value = null
  form.roleName = ''
  form.roleCode = ''
  form.status = 1
  showDialog.value = true
}

const handleEdit = (role) => {
  isEditMode.value = true
  currentEditId.value = role.id
  form.roleName = role.roleName
  form.roleCode = role.roleCode
  form.status = role.status
  showDialog.value = true
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该角色吗？', '提示', { type: 'warning' })
  } catch {
    return
  }

  try {
    const resp = await deleteRole(id)
    if (resp.code === 200) {
      ElMessage.success('角色删除成功')
      if (roleList.value.length === 1 && page.value > 1) {
        page.value -= 1
      }
      fetchRoles()
      return
    }
    ElMessage.error(resp.message || '角色删除失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '角色删除失败')
  }
}

const handleSave = async () => {
  if (!form.roleName.trim()) {
    ElMessage.error('角色名称不能为空')
    return
  }
  if (!form.roleCode.trim()) {
    ElMessage.error('角色编码不能为空')
    return
  }

  try {
    const payload = {
      roleName: form.roleName.trim(),
      roleCode: form.roleCode.trim(),
      status: form.status
    }

    const resp = isEditMode.value
      ? await updateRole(currentEditId.value, payload)
      : await createRole(payload)

    if (resp.code === 200) {
      ElMessage.success(isEditMode.value ? '角色更新成功' : '角色创建成功')
      closeDialog()
      fetchRoles()
      return
    }
    ElMessage.error(resp.message || '角色保存失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '角色保存失败')
  }
}

const closeDialog = () => {
  showDialog.value = false
  form.roleName = ''
  form.roleCode = ''
  form.status = 1
}

const openMenuDialog = async (role) => {
  currentRoleId.value = role.id
  currentRoleName.value = role.roleName
  checkedMenuIds.value = []
  showMenuDialog.value = true
  try {
    const resp = await getRoleMenus(role.id)
    if (resp.code === 200) {
      checkedMenuIds.value = resp.data || []
      await nextTick()
      menuTreeRef.value?.setCheckedKeys(checkedMenuIds.value)
      return
    }
    ElMessage.error(resp.message || '角色菜单加载失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '角色菜单加载失败')
  }
}

const handleSaveMenus = async () => {
  const checked = menuTreeRef.value?.getCheckedKeys(false) || []
  const halfChecked = menuTreeRef.value?.getHalfCheckedKeys() || []
  const finalIds = Array.from(new Set([...checked, ...halfChecked]))

  try {
    const resp = await assignRoleMenus(currentRoleId.value, finalIds)
    if (resp.code === 200) {
      ElMessage.success('角色菜单保存成功')
      closeMenuDialog()
      return
    }
    ElMessage.error(resp.message || '角色菜单保存失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '角色菜单保存失败')
  }
}

const closeMenuDialog = () => {
  showMenuDialog.value = false
  currentRoleId.value = null
  currentRoleName.value = ''
  checkedMenuIds.value = []
}
</script>

<style scoped>
.role-page {
  padding: 20px;
}

.role-header {
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

.menu-role-name {
  margin-bottom: 10px;
  color: #334155;
  font-weight: 600;
}
</style>
