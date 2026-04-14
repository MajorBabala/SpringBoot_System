<template>
  <div class="user-page app-card">
    <div class="user-header">
      <div>
        <div class="page-title">用户管理</div>
        <div class="page-subtitle">管理系统用户、角色绑定和账号状态。</div>
      </div>
      <el-button type="primary" @click="openCreateDialog">新增用户</el-button>
    </div>

    <el-form :inline="true" :model="searchForm" class="filters-form">
      <el-form-item label="用户名">
        <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
          <el-option :value="1" label="启用" />
          <el-option :value="0" label="禁用" />
        </el-select>
      </el-form-item>
      <el-form-item label="部门">
        <el-select v-model="searchForm.deptId" placeholder="全部" clearable style="width: 180px">
          <el-option v-for="dept in deptOptions" :key="dept.id" :value="dept.id" :label="dept.deptName" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="userList" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="130" />
      <el-table-column prop="nickname" label="昵称" min-width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="mobile" label="手机号" min-width="140" />
      <el-table-column label="角色" min-width="180">
        <template #default="{ row }">
          <el-tag
            v-for="role in row.roleCodes || []"
            :key="role"
            size="small"
            class="role-tag"
          >
            {{ role }}
          </el-tag>
          <span v-if="!row.roleCodes || row.roleCodes.length === 0">-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="270" fixed="right">
        <template #default="{ row }">
          <el-space>
            <el-button size="small" @click="openEditDialog(row.id)">编辑</el-button>
            <el-button size="small" @click="openResetPwdDialog(row)">重置密码</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="editDialogVisible" :title="isEditMode ? '编辑用户' : '新增用户'" width="640px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="用户名" v-if="!isEditMode">
          <el-input v-model="editForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="初始密码" v-if="!isEditMode">
          <el-input v-model="editForm.password" placeholder="请输入初始密码" show-password />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱（选填）" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.mobile" placeholder="请输入手机号（选填）" />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="editForm.deptId" placeholder="请选择部门" clearable style="width: 100%">
            <el-option v-for="dept in deptOptions" :key="dept.id" :value="dept.id" :label="dept.deptName" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" style="width: 100%">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="role in roleOptions" :key="role.id" :value="role.id" :label="role.roleName" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeEditDialog">取消</el-button>
        <el-button type="primary" @click="handleSaveUser">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="pwdDialogVisible" title="重置密码" width="420px">
      <el-form :model="pwdForm" label-width="90px">
        <el-form-item label="用户名">
          <el-input :model-value="pwdForm.username" disabled />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.password" show-password placeholder="请输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closePwdDialog">取消</el-button>
        <el-button type="primary" @click="handleResetPwd">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listRoles } from '../api/roles'
import { listDepts } from '../api/depts'
import { createUser, deleteUser, getUserDetail, listUsers, resetUserPassword, updateUser } from '../api/users'

const loading = ref(false)
const userList = ref([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)

const roleOptions = ref([])
const deptOptions = ref([])

const searchForm = reactive({
  username: '',
  status: null,
  deptId: null
})

const editDialogVisible = ref(false)
const isEditMode = ref(false)
const currentEditId = ref(null)
const editForm = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
  mobile: '',
  deptId: null,
  status: 1,
  roleIds: []
})

const pwdDialogVisible = ref(false)
const pwdForm = reactive({
  userId: null,
  username: '',
  password: ''
})

onMounted(async () => {
  await Promise.all([fetchRoleOptions(), fetchDeptOptions()])
  await fetchUsers()
})

const fetchRoleOptions = async () => {
  try {
    const resp = await listRoles(1, 200, null, 1)
    if (resp.code === 200) {
      roleOptions.value = resp.data.records || []
    }
  } catch {
    roleOptions.value = []
  }
}

const fetchDeptOptions = async () => {
  try {
    const resp = await listDepts()
    if (resp.code === 200) {
      deptOptions.value = resp.data || []
    }
  } catch {
    deptOptions.value = []
  }
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const resp = await listUsers(
      page.value,
      limit.value,
      searchForm.username || null,
      searchForm.status,
      searchForm.deptId
    )
    if (resp.code === 200) {
      userList.value = resp.data.records || []
      total.value = resp.data.total || 0
      return
    }
    ElMessage.error(resp.message || '用户列表加载失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '用户列表加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  fetchUsers()
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.status = null
  searchForm.deptId = null
  page.value = 1
  fetchUsers()
}

const handlePageChange = (value) => {
  page.value = value
  fetchUsers()
}

const resetEditForm = () => {
  editForm.username = ''
  editForm.password = ''
  editForm.nickname = ''
  editForm.email = ''
  editForm.mobile = ''
  editForm.deptId = null
  editForm.status = 1
  editForm.roleIds = []
}

const openCreateDialog = () => {
  isEditMode.value = false
  currentEditId.value = null
  resetEditForm()
  editDialogVisible.value = true
}

const openEditDialog = async (id) => {
  isEditMode.value = true
  currentEditId.value = id
  resetEditForm()
  try {
    const resp = await getUserDetail(id)
    if (resp.code === 200) {
      editForm.username = resp.data.username || ''
      editForm.nickname = resp.data.nickname || ''
      editForm.email = resp.data.email || ''
      editForm.mobile = resp.data.mobile || ''
      editForm.deptId = resp.data.deptId ?? null
      editForm.status = resp.data.status ?? 1
      editForm.roleIds = resp.data.roleIds || []
      editDialogVisible.value = true
      return
    }
    ElMessage.error(resp.message || '用户详情加载失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '用户详情加载失败')
  }
}

const closeEditDialog = () => {
  editDialogVisible.value = false
}

const validateEditForm = () => {
  if (!isEditMode.value) {
    if (!editForm.username.trim()) return '用户名不能为空'
    if (!editForm.password) return '初始密码不能为空'
    if (editForm.password.length < 6 || editForm.password.length > 20) {
      return '密码长度需在 6 到 20 位之间'
    }
  }
  if (!editForm.nickname.trim()) return '昵称不能为空'
  if (editForm.mobile && !/^\d{11}$/.test(editForm.mobile)) return '手机号必须为 11 位数字'
  if (editForm.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(editForm.email)) return '邮箱格式不正确'
  return ''
}

const handleSaveUser = async () => {
  const validationMessage = validateEditForm()
  if (validationMessage) {
    ElMessage.error(validationMessage)
    return
  }

  const payload = {
    nickname: editForm.nickname.trim(),
    email: (editForm.email || '').trim(),
    mobile: (editForm.mobile || '').trim(),
    deptId: editForm.deptId,
    status: editForm.status,
    roleIds: editForm.roleIds
  }

  try {
    const resp = isEditMode.value
      ? await updateUser(currentEditId.value, payload)
      : await createUser({
          ...payload,
          username: editForm.username.trim(),
          password: editForm.password
        })

    if (resp.code === 200) {
      ElMessage.success(isEditMode.value ? '用户更新成功' : '用户创建成功')
      closeEditDialog()
      fetchUsers()
      return
    }
    ElMessage.error(resp.message || '用户保存失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '用户保存失败')
  }
}

const openResetPwdDialog = (row) => {
  pwdForm.userId = row.id
  pwdForm.username = row.username
  pwdForm.password = ''
  pwdDialogVisible.value = true
}

const closePwdDialog = () => {
  pwdDialogVisible.value = false
  pwdForm.userId = null
  pwdForm.username = ''
  pwdForm.password = ''
}

const handleResetPwd = async () => {
  if (!pwdForm.password) {
    ElMessage.error('请输入新密码')
    return
  }
  if (pwdForm.password.length < 6 || pwdForm.password.length > 20) {
    ElMessage.error('密码长度需在 6 到 20 位之间')
    return
  }
  try {
    const resp = await resetUserPassword(pwdForm.userId, pwdForm.password)
    if (resp.code === 200) {
      ElMessage.success('密码重置成功')
      closePwdDialog()
      return
    }
    ElMessage.error(resp.message || '密码重置失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '密码重置失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除用户【${row.username}】吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }

  try {
    const resp = await deleteUser(row.id)
    if (resp.code === 200) {
      ElMessage.success('用户删除成功')
      if (userList.value.length === 1 && page.value > 1) {
        page.value -= 1
      }
      fetchUsers()
      return
    }
    ElMessage.error(resp.message || '用户删除失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '用户删除失败')
  }
}
</script>

<style scoped>
.user-page {
  padding: 20px;
}

.user-header {
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

.role-tag {
  margin-right: 6px;
  margin-bottom: 4px;
}
</style>
