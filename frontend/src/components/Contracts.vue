<template>
  <div class="module-page">
    <div class="module-header">
      <div class="header-content">
        <div class="title">
          <span class="emoji">📄</span>
          <h2>合同管理</h2> <!-- 页面标题 -->
        </div>
        <!-- 新建合同按钮，点击跳转到合同新增页面 -->
        <router-link class="btn-add" to="/contracts/add">
          <span class="icon">+</span>
          <span>新建合同</span>
        </router-link>
      </div>
    </div>

    <!-- 工具栏：搜索功能 -->
    <div class="module-toolbar">
      <div class="search-bar">
        <input 
          v-model="q"  <!-- 双向绑定搜索输入 -->
          type="text"
          class="search-input" 
          placeholder="搜索合同编号..."
          @keyup.enter="fetchContracts" <!-- 回车触发搜索 -->
        />
        <button class="btn-search" @click="fetchContracts"> <!-- 点击按钮执行搜索 -->
          <span class="icon">🔍</span>
          <span>查询</span>
        </button>
      </div>
    </div>

    <!-- 合同数据表格展示 -->
    <div class="module-content">
      <div class="table-container">
        <table class="data-table">
          <thead>
            <tr>
              <th>合同编号</th>
              <th>房屋</th>
              <th>租客</th>
              <th>房东</th>
              <th>开始日期</th>
              <th>结束日期</th>
              <th>租金</th>
              <th class="actions-column">操作</th>
            </tr>
          </thead>
          <tbody>
            <!-- 遍历合同列表 -->
            <tr v-for="c in contracts" :key="c.id">
              <td>{{ c.contractNo }}</td> <!-- 合同编号 -->
              <td>{{ c.houseAddress }}</td> <!-- 房屋地址 -->
              <td>{{ c.tenantName }}</td> <!-- 租客姓名 -->
              <td>{{ c.landlordName }}</td> <!-- 房东姓名 -->
              <td>{{ formatDate(c.startDate) }}</td> <!-- 开始日期格式化显示 -->
              <td>{{ formatDate(c.endDate) }}</td> <!-- 结束日期格式化显示 -->
              <td>¥{{ c.rentAmount }}/月</td> <!-- 租金 -->
              <td class="actions-cell">
                <!-- 编辑按钮 -->
                <router-link :to="`/contracts/edit/${c.id}`" class="btn-edit">编辑</router-link>
                <!-- 删除按钮 -->
                <button class="btn-delete" @click="deleteContract(c.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- 当合同列表为空时显示 -->
        <div v-if="contracts.length === 0" class="empty-state">
          <span class="icon">📭</span>
          <p>暂无数据</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue' // 从 Vue 导入响应式和生命周期钩子
import { get, del } from '@/utils/api' // 导入封装好的 get、delete 请求方法

const contracts = ref([]) // 合同数据列表
const q = ref('') // 搜索关键字

// 统一处理不同格式的接口返回数据
function normalizeList(res) {
  if (!res) return []
  if (Array.isArray(res)) return res
  if (res.content && Array.isArray(res.content)) return res.content
  if (res.data && Array.isArray(res.data)) return res.data
  return []
}

// 获取合同列表（带搜索）
async function fetchContracts() {
  try {
    // 拼接查询参数，如果有搜索关键字则带上 contractNo 参数
    const query = q.value ? `?contractNo=${encodeURIComponent(q.value)}&page=0&size=20` : '?page=0&size=20'
    const res = await get(`/contracts/search${query}`) // 调用后端接口
    contracts.value = normalizeList(res) // 格式化接口返回并赋值
  } catch (e) {
    console.error('获取合同失败', e)
    contracts.value = [] // 出错时清空列表
  }
}

// 删除合同
async function deleteContract(id) {
  if (!confirm('确认删除该合同？')) return // 用户确认操作
  try {
    await del(`/contracts/${id}`) // 调用后端删除接口
    await fetchContracts() // 删除后刷新合同列表
  } catch (e) {
    console.error('删除失败', e)
    try { await fetchContracts() } catch (_) {} // 即使出错也尝试刷新
    alert('删除失败：' + (e.message || '请检查控制台')) // 显示错误提示
  }
}

// 格式化日期显示为中文格式（如 2025/11/12）
function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

// 页面加载完成后自动获取合同列表
onMounted(fetchContracts)
</script>

<style scoped>
.status-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

/* 状态样式（目前未使用，预留给后续状态显示） */
.status-active {
  background: #e1f3d8;
  color: #67c23a;
}

.status-expired {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-terminated {
  background: #fef0f0;
  color: #f56c6c;
}
</style>
