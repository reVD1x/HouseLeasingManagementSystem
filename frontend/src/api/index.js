import { get, post, put, del, patch } from '@/utils/api'

function buildQs(params = {}) {
  const p = new URLSearchParams()
  Object.entries(params || {}).forEach(([k, v]) => {
    if (v === undefined || v === null) return
    p.append(k, String(v))
  })
  const s = p.toString()
  return s ? `?${s}` : ''
}

export const HousesAPI = {
  search(params = {}) {
    const qs = buildQs(params)
    return get(`/houses/search${qs}`)
  },
  get(id) { return get(`/houses/${id}`) },
  create(data) { return post(`/houses`, data) },
  update(id, data) { return put(`/houses/${id}`, data) },
  delete(id) { return del(`/houses/${id}`) }
}

export const UsersAPI = {
  list(params = {}) { const qs = buildQs(params); return get(`/users${qs}`) },
  get(id) { return get(`/users/${id}`) },
  create(data) { return post(`/users`, data) },
  update(id, data) { return put(`/users/${id}`, data) },
  delete(id) { return del(`/users/${id}`) },
  byRealName(name){ return get(`/users/by-realname/${encodeURIComponent(name)}`) },
  byIdCard(idcard){ return get(`/users/by-idcard/${encodeURIComponent(idcard)}`) }
}

export const ContractsAPI = {
  search(params = {}){ const qs = buildQs(params); return get(`/contracts/search${qs}`) },
  get(id){ return get(`/contracts/${id}`) },
  create(data){ return post(`/contracts`, data) },
  update(id,data){ return put(`/contracts/${id}`, data) },
  delete(id){ return del(`/contracts/${id}`) },
  templateData(houseId, landlordId, tenantId){ return get(`/contracts/template-data?houseId=${houseId}&landlordId=${landlordId}&tenantId=${tenantId}`) }
}

export const RentPaymentsAPI = {
  search(params = {}){ const qs = buildQs(params); return get(`/rent-payments/search${qs}`) },
  get(id){ return get(`/rent-payments/${id}`) },
  create(data){ return post(`/rent-payments`, data) },
  update(id,data){ return put(`/rent-payments/${id}`, data) },
  delete(id){ return del(`/rent-payments/${id}`) },
  markPaid(id){ return post(`/rent-payments/${id}/pay`) },
  remind(id){ return post(`/rent-payments/${id}/remind`) },
  overdueProcess(id, penalty){ const qs = penalty != null ? `?penalty=${penalty}` : ''; return post(`/rent-payments/${id}/overdue-process${qs}`) },
  report(from,to){ return get(`/rent-payments/report?from=${from}&to=${to}`) },
  overdue(beforeDate){ return get(`/rent-payments/overdue${beforeDate?`?beforeDate=${beforeDate}`:''}`) }
}

// Maintenance (维修) API
export const MaintenanceAPI = {
  search(params = {}) { const qs = buildQs(params); return get(`/maintenance-requests/search${qs}`) },
  get(id) { return get(`/maintenance-requests/${id}`) },
  create(data) { return post(`/maintenance-requests`, data) },
  update(id, data) { return put(`/maintenance-requests/${id}`, data) },
  // status/cost/complete/paid use PATCH
  updateStatus(id, status) { return patch(`/maintenance-requests/${id}/status`, { status }) },
  updateCost(id, cost) { return patch(`/maintenance-requests/${id}/cost`, { cost }) },
  complete(id) { return patch(`/maintenance-requests/${id}/complete`, {}) },
  markPaid(id) { return patch(`/maintenance-requests/${id}/paid`, {}) },
  delete(id) { return del(`/maintenance-requests/${id}`) }
}

// Reviews (评价) API
export const ReviewsAPI = {
  list(params = {}) { const qs = buildQs(params); return get(`/reviews${qs}`) },
  get(id) { return get(`/reviews/${id}`) },
  create(data) { return post(`/reviews`, data) },
  update(id, data) { return put(`/reviews/${id}`, data) },
  delete(id) { return del(`/reviews/${id}`) }
}

// Statistics API
export const StatsAPI = {
  overview() { return get(`/stats/overview`) },
  rentalRates() { return get(`/stats/rental-rates`) },
  income(from, to) { return get(`/stats/income?from=${from}&to=${to}`) },
  tenants() { return get(`/stats/tenants`) }
}

// Admin / System management API
export const AdminAPI = {
  roles() { return get(`/admin/roles`) },
  setRole(userId, role) { return post(`/admin/users/${userId}/role`, { role }) },
  backup() { return post(`/admin/backup`) },
  restore(data) { return post(`/admin/restore`, data) },
  settings() { return get(`/admin/settings`) },
  updateSettings(data) { return post(`/admin/settings`, data) }
}

export default { HousesAPI, UsersAPI, ContractsAPI, RentPaymentsAPI, MaintenanceAPI, ReviewsAPI, StatsAPI, AdminAPI }
