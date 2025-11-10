<template>
  <div class="module-page">
    <div class="module-header">
      <div class="header-content">
        <div class="title">
          <span class="emoji">🏠</span>
          <h2>房源管理</h2>
        </div>
        <router-link class="btn-add" to="/houses/add">
          <span class="icon">+</span>
          <span>新增房源</span>
        </router-link>
      </div>
    </div>

    <div class="module-toolbar">
      <div class="search-bar">
        <input 
          v-model="q" 
          type="text"
          class="search-input" 
          placeholder="搜索房屋地址..."
          @keyup.enter="fetchHouses"
        />
        <button class="btn-search" @click="fetchHouses">
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
              <th>地址</th>
              <th>户型</th>
              <th>面积</th>
              <th>租金</th>
              <th>装修</th>
              <th>配套设施</th>
              <th class="actions-column">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="h in houses" :key="h.id">
              <td>{{ h.address }}</td>
              <td>{{ h.houseType }}</td>
              <td>{{ h.area }} m²</td>
              <td>¥{{ h.rent }}/月</td>
              <td>{{ h.decoration }}</td>
              <td>{{ h.facilities }}</td>
              <td class="actions-cell">
                <router-link :to="`/houses/edit/${h.id}`" class="btn-edit">编辑</router-link>
                <button class="btn-delete" @click="deleteHouse(h.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="houses.length === 0" class="empty-state">
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

const houses = ref([])
const q = ref('')

async function fetchHouses() {
  try {
    const query = q.value ? `?address=${encodeURIComponent(q.value)}&page=0&size=20` : '?page=0&size=20'
    const res = await get(`/houses/search${query}`)
    houses.value = res.content || []
  } catch (e) {
    console.error('获取房源失败', e)
  }
}

async function deleteHouse(id) {
  if (confirm('确定要删除这个房源吗？')) {
    try {
      await del(`/houses/${id}`)
      await fetchHouses()
    } catch (e) {
      console.error('删除房源失败', e)
    }
  }
}

onMounted(() => {
  fetchHouses()
})
</script>

<style scoped>
.houses-page {
  padding: 0.5rem 0.5rem 1rem 0.5rem;
}

.table {
  margin-bottom: 0;
}

.table th {
  background-color: transparent;
  font-weight: 600;
  text-align: left;
}

.table td {
  vertical-align: middle;
}

.btn-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
}

.input-group .form-control:focus {
  border-color: #80bdff;
  box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header .title { display:flex; align-items:center; gap:8px; }
.page-header .title h2 { font-size:18px; margin:0; }
.page-header .emoji { font-size:20px }
.btn-add { background:#2fbf71; color:white; padding:6px 10px; border-radius:6px; text-decoration:none }

.search-row { display:flex; gap:8px; align-items:center; margin-bottom:12px }
.search-input { flex:1; padding:8px 10px; border:1px solid var(--color-border); border-radius:6px }
.btn-search { padding:8px 12px; border-radius:6px; background:#e6f6ef; border:1px solid #cbeed4 }

.table-wrap { overflow:auto }
.list-table { width:100%; border-collapse:collapse }
.list-table thead tr { border-bottom:1px solid var(--color-border); }
.list-table th, .list-table td { padding:10px 8px }
.list-table tbody tr { border-bottom:1px solid var(--color-border) }
.actions { white-space:nowrap }
.action-btn { margin-right:6px; padding:6px 8px; border-radius:6px; text-decoration:none }
.action-btn.edit { background:#ffd27a }
.action-btn.delete { background:#ffb3b3; border:none }
.empty { padding:16px; color:var(--color-text); text-align:center }
</style>