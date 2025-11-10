/**
 * API请求工具
 * 自动添加token到请求头
 */

const API_BASE_URL = 'http://localhost:8080/api'

/**
 * 发送带token的请求
 */
export async function fetchWithAuth(url, options = {}) {
  const token = localStorage.getItem('access-token')

  if (!token && !url.includes('/auth/')) {
    // 如果没有token且不是登录请求，刷新页面回到登录
    localStorage.removeItem('access-token')
    // 不要直接 reload（会导致无限刷新循环），改为抛错并由上层处理或导航到登录
    try { if (typeof location !== 'undefined') location.hash = '#/login' } catch (e) {}
    throw new Error('未登录')
  }

  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  }

  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const response = await fetch(`${API_BASE_URL}${url}`, {
    ...options,
    headers,
  })

  // 如果返回401，说明token无效，刷新页面回到登录
  if (response.status === 401) {
    localStorage.removeItem('access-token')
    // 不要直接 reload（会导致无限刷新循环），改为导航到登录页 hash，并抛错
    try { if (typeof location !== 'undefined') location.hash = '#/login' } catch (e) {}
    throw new Error('Token已过期，请重新登录')
  }

  return response
}

// parse response body safely: return null for empty body, or parsed JSON if possible
async function parseJsonSafe(response) {
  const text = await response.text()
  if (!text) return null
  try {
    return JSON.parse(text)
  } catch (e) {
    // not JSON, return raw text
    return text
  }
}

async function handleResponse(response) {
  const body = await parseJsonSafe(response)
  if (response.ok) return body
  // construct error message
  let msg = ''
  if (body === null) msg = `HTTP ${response.status} ${response.statusText}`
  else if (typeof body === 'string') msg = body
  else msg = JSON.stringify(body)
  const err = new Error(msg)
  err.status = response.status
  err.body = body
  throw err
}

/**
 * GET请求
 */
export async function get(url) {
  const response = await fetchWithAuth(url)
  return handleResponse(response)
}

/**
 * POST请求
 */
export async function post(url, data) {
  const response = await fetchWithAuth(url, {
    method: 'POST',
    body: JSON.stringify(data),
  })
  return handleResponse(response)
}

/**
 * PUT请求
 */
export async function put(url, data) {
  const response = await fetchWithAuth(url, {
    method: 'PUT',
    body: JSON.stringify(data),
  })
  return handleResponse(response)
}

/**
 * DELETE请求
 */
export async function del(url) {
  const response = await fetchWithAuth(url, {
    method: 'DELETE',
  })
  return handleResponse(response)
}

/**
 * 检查是否已登录
 */
export function isAuthenticated() {
  return !!localStorage.getItem('access-token')
}

/**
 * 退出登录
 */
export function logout() {
  localStorage.removeItem('access-token')
  window.location.reload()
}

