import request from './request'

export function listRoles(page, limit, roleName, status) {
  return request.get('/api/roles', {
    params: { page, limit, roleName, status }
  })
}

export function createRole(data) {
  return request.post('/api/roles', data)
}

export function updateRole(id, data) {
  return request.put(`/api/roles/${id}`, data)
}

export function deleteRole(id) {
  return request.delete(`/api/roles/${id}`)
}

export function getRoleDetail(id) {
  return request.get(`/api/roles/${id}`)
}

export function getRoleMenus(id) {
  return request.get(`/api/roles/${id}/menus`)
}

export function assignRoleMenus(id, menuIds) {
  return request.put(`/api/roles/${id}/menus`, { menuIds })
}

export function getCurrentRoleMenus() {
  return request.get('/api/roles/current/menus')
}

export function checkRoleCode(roleCode) {
  return request.get(`/api/roles/check-code/${roleCode}`)
}
