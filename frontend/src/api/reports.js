import request from './request'

export function getBalanceSheet(period) {
  return request.get('/api/reports/balance-sheet', { params: { period } })
}

export function getProfitStatement(period) {
  return request.get('/api/reports/profit-statement', { params: { period } })
}

