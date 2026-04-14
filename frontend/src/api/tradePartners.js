import request from './request'

export function listTradePartners({ pageNum = 1, pageSize = 10, keyword, partnerType, status } = {}) {
  return request.get('/api/trade-partners', {
    params: { pageNum, pageSize, keyword, partnerType, status }
  })
}

export function createTradePartner(data) {
  return request.post('/api/trade-partners', data)
}

export function updateTradePartner(id, data) {
  return request.put(`/api/trade-partners/${id}`, data)
}

export function deleteTradePartner(id) {
  return request.delete(`/api/trade-partners/${id}`)
}

export function getTradePartnerDetail(id) {
  return request.get(`/api/trade-partners/${id}`)
}
