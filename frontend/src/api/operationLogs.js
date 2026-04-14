import request from './request'

export function listOperationLogs(page, limit, username, operation, dateFrom, dateTo) {
  return request.get('/api/operation-logs', {
    params: { page, limit, username, operation, dateFrom, dateTo }
  })
}

export function exportOperationLogs(username, operation, dateFrom, dateTo) {
  return request.get('/api/operation-logs/export', {
    params: { username, operation, dateFrom, dateTo },
    responseType: 'blob'
  })
}
