import request from './request'

export function listExpenseClaims({ pageNum = 1, pageSize = 10, status } = {}) {
  return request.get('/api/expense-claims', { params: { pageNum, pageSize, status } })
}

export function createExpenseClaim(payload) {
  return request.post('/api/expense-claims', payload)
}

export function submitExpenseClaim(id) {
  return request.put(`/api/expense-claims/${id}/submit`)
}

export function approveExpenseClaim(id) {
  return request.put(`/api/expense-claims/${id}/approve`)
}

export function payExpenseClaim(id) {
  return request.put(`/api/expense-claims/${id}/pay`)
}

export function getExpenseClaimDetail(id) {
  return request.get(`/api/expense-claims/${id}`)
}

export function generateExpenseClaimVoucher(id) {
  return request.put(`/api/expense-claims/${id}/generate-voucher`)
}

export function listExpenseClaimAttachments(id) {
  return request.get(`/api/expense-claims/${id}/attachments`)
}

export function uploadExpenseClaimAttachment(id, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/api/expense-claims/${id}/attachments`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function deleteExpenseClaimAttachment(id, attachmentId) {
  return request.delete(`/api/expense-claims/${id}/attachments/${attachmentId}`)
}

