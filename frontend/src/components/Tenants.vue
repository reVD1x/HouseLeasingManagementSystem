<template>
  <div class="module-page">
    <div class="module-header">
      <div class="header-content">
        <div class="title">
          <span class="emoji">👥</span>
          <h2>租客管理</h2> <!-- 页面标题 -->
        </div>
        <!-- 跳转到新增租客页面的按钮 -->
        <router-link class="btn-add" to="/tenants/add">
          <span class="icon">+</span>
          <span>新增租客</span>
        </router-link>
      </div>
    </div>

    <div class="module-toolbar">
      <div class="search-bar">
        <!-- 搜索输入框，双向绑定 q -->
        <input 
          v-model="q" 
          type="text"
          class="search-input" 
          placeholder="搜索租客姓名..."
          @keyup.enter="fetchTenants" <!-- 按回车键触发查询 -->
        />
        <!-- 查询按钮 -->
        <button type="button" class="btn-search" @click="fetchTenants">
          <span class="icon">🔍</span>
          <span>查询</span>
        </button>
      </div>
    </div>

    <div class="module-content">
      <div class="table-container">
        <table class="data-table">
          <thead>
            <tr>
              <th>姓名</th>
              <th>身份证号</th>
              <th>电话</th>
              <th class="actions-column">操作</th> <!-- 操作列 -->
            </tr>
          </thead>
          <tbody>
            <!-- 遍历租客列表 -->
            <tr v-for="t in tenants" :key="t.id">
              <td>{{ t.realName }}</td> <!-- 租客姓名 -->
              <td>{{ t.idCard }}</td>   <!-- 身份证号 -->
              <td>{{ t.phone }}</td>    <!-- 电话号码 -->
              <td class="actions-cell">
                <!-- 编辑按钮 -->
                <router-link :to="`/tenants/edit/${t.id}`" class="btn-edit">编辑</router-link>
                <!-- 删除按钮 -->
                <button class="btn-delete" @click="deleteTenant(t.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
        <!-- 无数据时显示 -->
        <div v-if="tenants.length === 0" class="empty-state">
          <span class="icon">📭</span>
          <p>暂无数据</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue' // 导入 Vue 组合式 API
import { get, del } from '@/utils/api' // 导入封装的 GET 和 DELETE 请求方法

const tenants = ref([]) // 存储租客列表
const q = ref('') // 搜索关键字

// 统一格式化不同接口返回的数据结构
function normalizeList(res) {
  if (!res) return []
  if (Array.isArray(res)) return res // 直接是数组则直接返回
  if (res.content && Array.isArray(res.content)) return res.content // 支持分页接口格式
  if (res.data && Array.isArray(res.data)) return res.data // 支持 data 包裹格式
  return []
}

// 获取租客列表
async function fetchTenants() {
  try {
    // 当有搜索关键字时，使用模糊查询接口
    if (q.value) {
      const v = q.value.trim() // 去除前后空格
      try {
        // 模糊搜索接口
        const res = await get(`/users/search?realName=${encodeURIComponent(v)}&page=0&size=20`)
        console.debug('DEBUG: /users/search response:', res)
        const list = normalizeList(res)
        if (list.length > 0) {
          tenants.value = list
          return
        }
        // 如果主接口返回空，则尝试备用精确接口
        try {
          const list2 = await get(`/users/by-realname/${encodeURIComponent(v)}`)
          console.debug('DEBUG: /users/by-realname response:', list2)
          tenants.value = normalizeList(list2)
          return
        } catch (e2) {
          console.warn('DEBUG: fallback by-realname failed', e2)
        }
        tenants.value = [] // 搜索不到结果时清空
      } catch (err) {
        console.error('DEBUG: /users/search failed', err)
        tenants.value = [] // 请求出错时清空
      }
      return
    }
    // 默认查询第一页数据
    const res = await get(`/users?page=0&size=20`)
    tenants.value = normalizeList(res)
  } catch (e) {
    console.error('获取租客失败', e)
    tenants.value = [] // 异常时清空租客列表
    // 不在 UI 弹出错误，开发者可在控制台查看
  }
}

// 删除租客
async function deleteTenant(id) {
  if (!confirm('确认删除该租客？')) return // 用户确认删除
  try {
    await del(`/users/${id}`) // 调用后端删除接口
    await fetchTenants() // 删除后重新刷新列表
  } catch (e) {
    console.error('删除失败', e)
    try { await fetchTenants() } catch (_) {} // 即使失败也尝试刷新
    alert('删除失败：' + (e.message || '请检查控制台')) // 提示错误信息
  }
}

// 页面加载时自动获取租客列表
onMounted(fetchTenants)
</script>

<style scoped>
.me-2 { margin-right: .5rem } /* 添加右边距的通用样式 */
</style>
