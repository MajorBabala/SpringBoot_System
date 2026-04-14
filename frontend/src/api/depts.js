import request from './request'

export function listDepts() {
  return request.get('/api/depts')
}
