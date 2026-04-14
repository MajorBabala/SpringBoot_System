import request from './request'

export function listAccountingPeriods({ pageNum = 1, pageSize = 10, keyword, status } = {}) {
  return request.get('/api/accounting-periods', {
    params: { pageNum, pageSize, keyword, status }
  })
}

export function createAccountingPeriod(data) {
  return request.post('/api/accounting-periods', data)
}

export function updateAccountingPeriod(id, data) {
  return request.put(`/api/accounting-periods/${id}`, data)
}

export function deleteAccountingPeriod(id) {
  return request.delete(`/api/accounting-periods/${id}`)
}

export function getAccountingPeriodDetail(id) {
  return request.get(`/api/accounting-periods/${id}`)
}

export function openAccountingPeriod(id) {
  return request.put(`/api/accounting-periods/${id}/open`)
}

export function closeAccountingPeriod(id) {
  return request.put(`/api/accounting-periods/${id}/close`)
}
