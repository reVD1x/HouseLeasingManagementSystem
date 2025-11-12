/**
 * API请求工具
 * 自动添加token到请求头
 */

import router from '@/router'

const API_BASE_URL = 'http://localhost:8080/api'

/**
 * 发送带token的请求
 */
export async function fetchWithAuth(url, options = {}) {
  const token = localStorage.getItem('access-token')

  // If there's no token, allow the request to proceed as unauthenticated.
  // We will handle 401 responses after the request and redirect to login then.

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

  // 如果返回401，说明token无效，移除token并导航到登录（避免 reload 导致的循环）
  if (response.status === 401) {
    localStorage.removeItem('access-token')
    try { router.push('/login') } catch (e) { window.location.href = '/login' }
    throw new Error('Token已过期，请重新登录')
  }

  return response
}

async function parseResponse(response) {
  // handle empty body (e.g. 204 No Content) and non-JSON responses
  const text = await response.text()
  if (!text) return null
  try {
    return JSON.parse(text)
  } catch (e) {
    // not JSON, return raw text
    return text
  }
}

function makeErrorMessage(response, parsed) {
  const status = response.status
  let body = ''
  try {
    if (parsed === null || parsed === undefined) body = ''
    else if (typeof parsed === 'string') body = parsed
    else body = JSON.stringify(parsed)
  } catch (e) {
    body = String(parsed)
  }
  return `HTTP ${status}: ${body || response.statusText}`
}

/**
 * GET请求
 */
export async function get(url) {
  const response = await fetchWithAuth(url)
  const data = await parseResponse(response)
  if (!response.ok) {
    // if backend returned structured error, include it
    const msg = makeErrorMessage(response, data)
    throw new Error(msg)
  }
  return data
}

/**
 * POST请求
 */
export async function post(url, data) {
  const response = await fetchWithAuth(url, {
    method: 'POST',
    body: JSON.stringify(data),
  })
  const parsed = await parseResponse(response)
  if (!response.ok) {
    const msg = makeErrorMessage(response, parsed)
    throw new Error(msg)
  }
  return parsed
}

/**
 * PUT请求
 */
export async function put(url, data) {
  const response = await fetchWithAuth(url, {
    method: 'PUT',
    body: JSON.stringify(data),
  })
  const parsed = await parseResponse(response)
  if (!response.ok) {
    const msg = makeErrorMessage(response, parsed)
    throw new Error(msg)
  }
  return parsed
}

/**
 * PATCH请求
 */
export async function patch(url, data) {
  const response = await fetchWithAuth(url, {
    method: 'PATCH',
    body: JSON.stringify(data),
  })
  const parsed = await parseResponse(response)
  if (!response.ok) {
    const msg = makeErrorMessage(response, parsed)
    throw new Error(msg)
  }
  return parsed
}

/**
 * DELETE请求
 */
export async function del(url) {
  const response = await fetchWithAuth(url, {
    method: 'DELETE',
  })
  const parsed = await parseResponse(response)
  if (!response.ok) {
    const msg = makeErrorMessage(response, parsed)
    throw new Error(msg)
  }
  return parsed
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
  // 使用路由导航到登录页替代刷新
  try { router.push('/login') } catch (e) { window.location.href = '/login' }
}
