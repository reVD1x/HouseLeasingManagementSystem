<template>
  <div class="module-page">
    <div class="module-header">
      <div class="header-content">
        <div class="title">
          <span class="emoji">📄</span>
          <h2>合同管理</h2>
        </div>
        <router-link class="btn-add" to="/contracts/add">
          <span class="icon">+</span>
          <span>新建合同</span>
        </router-link>
      </div>
    </div>

    <div class="module-toolbar">
      <div class="search-bar">
        <input 
          v-model="q" 
          type="text"
          class="search-input" 
          placeholder="搜索合同编号..."
          @keyup.enter="fetchContracts"
        />
        <button class="btn-search" @click="fetchContracts">
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
            <tr v-for="c in contracts" :key="c.id">
              <td>{{ c.contractNo }}</td>
              <td>{{ c.houseAddress }}</td>
              <td>{{ c.tenantName }}</td>
              <td>{{ c.landlordName }}</td>
              <td>{{ formatDate(c.startDate) }}</td>
              <td>{{ formatDate(c.endDate) }}</td>
              <td>¥{{ c.rentAmount }}/月</td>
              <td class="actions-cell">
                <router-link :to="`/contracts/edit/${c.id}`" class="btn-edit">编辑</router-link>
                <button class="btn-delete" @click="deleteContract(c.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="contracts.length === 0" class="empty-state">
          <span class="icon">📭</span>
          <p>暂无数据</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get, del } from '@/utils/api'
const contracts = ref([])
const q = ref('')

async function fetchContracts() {
  try {
    const query = q.value ? `?contractNo=${encodeURIComponent(q.value)}&page=0&size=20` : '?page=0&size=20'
    const res = await get(`/contracts/search${query}`)
    contracts.value = res.content || []
  } catch (e) {
    console.error('获取合同失败', e)
    contracts.value = []
  }
}

async function deleteContract(id) {
  if (!confirm('确认删除该合同？')) return
  try {
    await del(`/contracts/${id}`)
    await fetchContracts()
  } catch (e) {
    console.error('删除失败', e)
    alert('删除失败：' + (e.message || '请检查控制台'))
  }
}

function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

onMounted(fetchContracts)
</script>

<style scoped>
.status-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

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
