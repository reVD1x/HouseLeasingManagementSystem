<template>
  <div class="module-page">
    <div class="module-header">
      <div class="header-content">
        <div class="title">
          <span class="emoji">💰</span>
          <h2>租金管理</h2>
        </div>
        <div style="display:flex;gap:8px;align-items:center">
          <router-link class="btn-add" to="/rent-payments/add">
            <span class="icon">+</span>
            <span>登记租金</span>
          </router-link>
          <button class="btn-add" @click="showOverdue">查看逾期</button>
        </div>
      </div>
    </div>

    <div class="module-toolbar">
      <div class="search-bar">
        <input 
          v-model="q" 
          type="text"
          class="search-input" 
          placeholder="搜索合同编号..."
          @keyup.enter="loadItems"
        />
        <button class="btn-search" @click="loadItems">
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
              <th>应付日期</th>
              <th>金额</th>
              <th>状态</th>
              <th class="actions-column">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in items" :key="r.id">
              <td>{{ r.contractNo }}</td>
              <td>{{ r.houseAddress }}</td>
              <td>{{ r.tenantName }}</td>
              <td>{{ formatDate(r.dueDate) }}</td>
              <td>¥{{ r.amount }}</td>
              <td>
                <span :class="getStatusClass(r.status)">
                  {{ getStatusText(r.status) }}
                </span>
              </td>
              <td class="actions-cell">
                <router-link :to="`/rent-payments/edit/${r.id}`" class="btn-edit">编辑</router-link>
                <button class="btn-delete" @click="del(r.id)">删除</button>
                <button class="btn-edit" @click="markRemind(r.id)">提醒</button>
                <button class="btn-edit" @click="markPaid(r.id)">标记已付</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="items.length === 0" class="empty-state">
          <span class="icon">📭</span>
          <p>暂无数据</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get, del as apiDel, post } from '@/utils/api'
const items = ref([])
const q = ref('')

// renamed to avoid colliding with the global fetch and to be clearer
async function loadItems() {
  try {
    const baseQuery = '?page=0&size=20'
    // try directly searching by contractId (if user entered a numeric id)
    if (q.value) {
      const maybeNum = Number(q.value)
      if (Number.isFinite(maybeNum) && maybeNum > 0) {
        const res = await get(`/rent-payments/search?contractId=${maybeNum}&page=0&size=20`)
        items.value = res.content || []
        // if we found results, return early
        if ((items.value || []).length > 0) return
      }

      // fallback: try search contract by contractNo, then query rent-payments by contract id
      try {
        const cRes = await get(`/contracts/search?contractNo=${encodeURIComponent(q.value)}&page=0&size=20`)
        const contracts = cRes.content || []
        if (contracts.length > 0) {
          const contractId = contracts[0].id
          const rpRes = await get(`/rent-payments/search?contractId=${contractId}&page=0&size=20`)
          items.value = rpRes.content || []
          return
        }
      } catch (e) {
        // ignore contract search errors and fall through to default listing
        console.warn('合同搜索回退失败', e)
      }
    }

    const res = await get(`/rent-payments/search${baseQuery}`)
    items.value = res.content || []
  } catch (e) {
    console.error('获取租金记录失败', e)
    items.value = []
  }
}

async function del(id) {
  if (!confirm('确认删除该记录？')) return
  try {
    await apiDel(`/rent-payments/${id}`)
    await loadItems()
  } catch (e) {
    console.error('删除失败', e)
    alert('删除失败：' + (e.message || '请检查控制台'))
  }
}

function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

function getStatusText(status) {
  const statusMap = {
    PAID: '已缴费',
    PENDING: '未缴费',
    OVERDUE: '已逾期'
  }
  return statusMap[status] || status
}

function getStatusClass(status) {
  return {
    'status-tag': true,
    'status-success': status === 'PAID',
    'status-warning': false,
    'status-danger': status === 'PENDING' || status === 'OVERDUE'
  }
}

async function markRemind(id) {
  try {
    // use central API helper to ensure Authorization header and error handling
    await post(`/rent-payments/${id}/remind`)
    alert('已发送提醒（后端已标记）')
    await loadItems()
  } catch (e) {
    console.error('提醒失败', e)
    alert('提醒失败：' + (e.message || '请检查控制台'))
  }
}

async function markPaid(id) {
  try {
    await post(`/rent-payments/${id}/pay`)
    alert('已标记为已付')
    await loadItems()
  } catch (e) {
    console.error('标记已付失败', e)
    alert('标记失败：' + (e.message || '请检查控制台'))
  }
}


async function showOverdue() {
  try {
    const res = await get('/rent-payments/overdue')
    // backend returns List<RentPayment>, replace current table with overdue list
    items.value = Array.isArray(res) ? res : (res || [])
    // clear search box since we're showing overdue results
    q.value = ''
  } catch (e) {
    console.error('获取逾期列表失败', e)
    alert('获取逾期列表失败：' + (e.message || '请检查控制台'))
  }
}

onMounted(loadItems)
</script>

<style scoped>
.status-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-success {
  background: #e1f3d8;
  color: #67c23a;
}

.status-warning {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-danger {
  background: #fef0f0;
  color: #f56c6c;
}
</style>
