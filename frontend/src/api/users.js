import request from './request'

export function listUsers(page, limit, username, status, deptId) {
  return request.get('/api/users', {
    params: { page, limit, username, status, deptId }
  })
}

export function getUserDetail(id) {
  return request.get(`/api/users/${id}`)
}

export function createUser(data) {
  return request.post('/api/users', data)
}

export function updateUser(id, data) {
  return request.put(`/api/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/api/users/${id}`)
}

export function resetUserPassword(id, password) {
  return request.put(`/api/users/${id}/resetPwd`, { password })
}
