<template>
  <div class="module-page">
    <!-- 页面头部 -->
    <div class="module-header">
      <div class="header-content">
        <div class="title">
          <span class="emoji">🏠</span>
          <h2>房源管理</h2>
        </div>
        <!-- 新增房源按钮 -->
        <router-link class="btn-add" to="/houses/add">
          <span class="icon">+</span>
          <span>新增房源</span>
        </router-link>
      </div>
    </div>

    <!-- 搜索栏 -->
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

    <!-- 表格显示房源 -->
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
                <!-- 编辑房源 -->
                <router-link :to="`/houses/edit/${h.id}`" class="btn-edit">编辑</router-link>
                <!-- 删除房源 -->
                <button class="btn-delete" @click="deleteHouse(h.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- 空状态提示 -->
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

// 房源列表
const houses = ref([])
// 搜索关键字
const q = ref('')

// 规范化列表返回数据，兼容不同接口返回结构
function normalizeList(res){
  if (!res) return []
  if (Array.isArray(res)) return res
  if (res.content && Array.isArray(res.content)) return res.content
  if (res.data && Array.isArray(res.data)) return res.data
  return []
}

// 获取房源列表（可带搜索关键字）
async function fetchHouses() {
  try {
    const query = q.value 
      ? `?address=${encodeURIComponent(q.value)}&page=0&size=20` 
      : '?page=0&size=20'
    const res = await get(`/houses/search${query}`)
    houses.value = normalizeList(res)
  } catch (e) {
    console.error('获取房源失败', e)
    houses.value = []
  }
}

// 删除房源
async function deleteHouse(id) {
  if (confirm('确定要删除这个房源吗？')) {
    try {
      await del(`/houses/${id}`)
      await fetchHouses() // 删除后刷新列表
    } catch (e) {
      console.error('删除房源失败', e)
      alert('删除失败，请检查控制台')
    }
  }
}

// 页面加载时获取房源列表
onMounted(() => {
  fetchHouses()
})
</script>

<style scoped>
.module-page { padding:12px }
.table-container { overflow:auto }
.data-table th, .data-table td { padding:8px 10px; border-bottom:1px solid #eee }
.empty-state { text-align:center; padding:16px }
</style>
