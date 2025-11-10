<template>
  <div class="module-page">
    <div class="module-header">
      <div class="header-content">
        <div class="title">
          <span class="emoji">👥</span>
          <h2>租客管理</h2>
        </div>
        <router-link class="btn-add" to="/tenants/add">
          <span class="icon">+</span>
          <span>新增租客</span>
        </router-link>
      </div>
    </div>

    <div class="module-toolbar">
      <div class="search-bar">
        <input 
          v-model="q" 
          type="text"
          class="search-input" 
          placeholder="搜索租客姓名..."
          @keyup.enter="fetchTenants"
        />
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
              <th class="actions-column">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in tenants" :key="t.id">
              <td>{{ t.realName }}</td>
              <td>{{ t.idCard }}</td>
              <td>{{ t.phone }}</td>
              <td class="actions-cell">
                <router-link :to="`/tenants/edit/${t.id}`" class="btn-edit">编辑</router-link>
                <button class="btn-delete" @click="deleteTenant(t.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="tenants.length === 0" class="empty-state">
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
const tenants = ref([])
const q = ref('')

async function fetchTenants() {
  try {
    // 仅按姓名查询：当输入非空时调用 by-realname/{name}
    if (q.value) {
      const v = q.value.trim()
      try {
        const list = await get(`/users/by-realname/${encodeURIComponent(v)}`)
        tenants.value = list || []
      } catch (err) {
        tenants.value = []
      }
      return
    }
    // 默认分页查询
    const res = await get(`/users?page=0&size=20`)
    tenants.value = res.content || []
  } catch (e) {
    console.error('获取租客失败', e)
    tenants.value = []
    alert('获取租客失败：' + (e.message || '请检查控制台'))
  }
}

async function deleteTenant(id) {
  if (!confirm('确认删除该租客？')) return
  try {
    await del(`/users/${id}`)
    await fetchTenants()
  } catch (e) {
    console.error('删除失败', e)
    alert('删除失败：' + (e.message || '请检查控制台'))
  }
}

onMounted(fetchTenants)
</script>

<style scoped>
.me-2 { margin-right: .5rem }
</style>
