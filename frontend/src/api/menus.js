import request from './request'

export function listMenus() {
  return request.get('/api/menus')
}

export function getMenuDetail(id) {
  return request.get(`/api/menus/${id}`)
}

export function createMenu(data) {
  return request.post('/api/menus', data)
}

export function updateMenu(id, data) {
  return request.put(`/api/menus/${id}`, data)
}

export function deleteMenu(id) {
  return request.delete(`/api/menus/${id}`)
}
