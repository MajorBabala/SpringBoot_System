import request from './request'

export function login(username, password) {
  return request.post('/api/auth/login', { username, password })
}

export function register(payload) {
  return request.post('/api/auth/register', payload)
}

export function getCurrentUserInfo() {
  return request.get('/api/auth/info')
}

export function logout() {
  return request.post('/api/auth/logout')
}
