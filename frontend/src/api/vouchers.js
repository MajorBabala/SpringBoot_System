import request from './request'

export function listVouchers({ pageNum = 1, pageSize = 10, status, period } = {}) {
  return request.get('/api/vouchers', {
    params: { pageNum, pageSize, status, period }
  })
}

export function createVoucher(payload) {
  return request.post('/api/vouchers', payload)
}

export function auditVoucher(id) {
  return request.put(`/api/vouchers/${id}/audit`)
}

export function bookkeepVoucher(id) {
  return request.put(`/api/vouchers/${id}/bookkeep`)
}

export function getVoucherDetail(id) {
  return request.get(`/api/vouchers/${id}`)
}

