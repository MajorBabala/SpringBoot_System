import request from './request'

export function listSubjectBalances({ page = 1, limit = 10, period, keyword } = {}) {
  return request.get('/api/ledgers/subject-balances', {
    params: { page, limit, period, keyword }
  })
}

export function listGeneralLedgers({ page = 1, limit = 10, period, keyword } = {}) {
  return request.get('/api/ledgers/general', {
    params: { page, limit, period, keyword }
  })
}

export function listDetailLedgers({ page = 1, limit = 10, period, subjectId } = {}) {
  return request.get('/api/ledgers/detail', {
    params: { page, limit, period, subjectId }
  })
}

export function listJournalEntries({ page = 1, limit = 10, period, keyword } = {}) {
  return request.get('/api/ledgers/journal', {
    params: { page, limit, period, keyword }
  })
}
