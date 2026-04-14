import request from './request'

export function listSettlementMethods({ pageNum = 1, pageSize = 10, keyword, status } = {}) {
  return request.get('/api/settlement-methods', {
    params: { pageNum, pageSize, keyword, status }
  })
}

export function createSettlementMethod(data) {
  return request.post('/api/settlement-methods', data)
}

export function updateSettlementMethod(id, data) {
  return request.put(`/api/settlement-methods/${id}`, data)
}

export function deleteSettlementMethod(id) {
  return request.delete(`/api/settlement-methods/${id}`)
}

export function getSettlementMethodDetail(id) {
  return request.get(`/api/settlement-methods/${id}`)
}
