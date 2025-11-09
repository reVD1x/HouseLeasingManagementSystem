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
    window.location.reload()
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
    window.location.reload()
    throw new Error('Token已过期，请重新登录')
  }

  return response
}

/**
 * GET请求
 */
export async function get(url) {
  const response = await fetchWithAuth(url)
  return response.json()
}

/**
 * POST请求
 */
export async function post(url, data) {
  const response = await fetchWithAuth(url, {
    method: 'POST',
    body: JSON.stringify(data),
  })
  return response.json()
}

/**
 * PUT请求
 */
export async function put(url, data) {
  const response = await fetchWithAuth(url, {
    method: 'PUT',
    body: JSON.stringify(data),
  })
  return response.json()
}

/**
 * DELETE请求
 */
export async function del(url) {
  const response = await fetchWithAuth(url, {
    method: 'DELETE',
  })
  return response.json()
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

