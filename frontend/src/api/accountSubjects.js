import request from './request'

export function listAccountSubjects() {
  return request.get('/api/account-subjects')
}
