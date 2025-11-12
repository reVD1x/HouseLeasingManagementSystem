<template>
  <div class="module-page">
    <div class="module-header">
      <div class="header-content">
        <div class="title">
          <span class="emoji">💰</span>
          <h2>租金管理</h2> <!-- 页面标题 -->
        </div>
        <div style="display:flex;gap:8px;align-items:center">
          <router-link class="btn-add" to="/rent-payments/add"> <!-- 跳转到租金登记页面 -->
            <span class="icon">+</span>
            <span>登记租金</span>
          </router-link>
          <button class="btn-add" @click="showOverdue">查看逾期</button> <!-- 查看逾期租金按钮 -->
        </div>
      </div>
    </div>

    <div class="module-toolbar">
      <div class="search-bar">
        <input 
          v-model="q"  <!-- 双向绑定搜索框输入内容 -->
          type="text"
          class="search-input" 
          placeholder="搜索合同编号..."
          @keyup.enter="loadItems" <!-- 回车触发查询 -->
        />
        <button class="btn-search" @click="loadItems"> <!-- 点击按钮执行查询 -->
          <span class="icon">🔍</span>
          <span>查询</span>
        </button>
      </div>
    </div>

    <div class="module-content">
      <div class="table-container">
        <table class="data-table"> <!-- 租金记录表格 -->
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
            <tr v-for="r in items" :key="r.id"> <!-- 遍历租金记录 -->
              <td>{{ r.contractNo }}</td> <!-- 合同编号 -->
              <td>{{ r.houseAddress }}</td> <!-- 房屋地址 -->
              <td>{{ r.tenantName }}</td> <!-- 租客姓名 -->
              <td>{{ formatDate(r.dueDate) }}</td> <!-- 格式化应付日期 -->
              <td>¥{{ r.amount }}</td> <!-- 金额显示 -->
              <td>
                <span :class="getStatusClass(r.status)"> <!-- 根据状态添加样式 -->
                  {{ getStatusText(r.status) }} <!-- 显示状态文字 -->
                </span>
              </td>
              <td class="actions-cell"> <!-- 操作列 -->
                <router-link :to="`/rent-payments/edit/${r.id}`" class="btn-edit">编辑</router-link> <!-- 编辑按钮 -->
                <button class="btn-delete" @click="del(r.id)">删除</button> <!-- 删除按钮 -->
                <button class="btn-edit" @click="markRemind(r.id)">提醒</button> <!-- 提醒租客按钮 -->
                <button class="btn-edit" @click="markPaid(r.id)">标记已付</button> <!-- 标记为已支付 -->
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="items.length === 0" class="empty-state"> <!-- 当没有数据时显示 -->
          <span class="icon">📭</span>
          <p>暂无数据</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue' // 导入Vue响应式API和生命周期函数
import { get, del as apiDel, post } from '@/utils/api' // 导入封装好的HTTP请求方法
const items = ref([]) // 存放租金记录列表
const q = ref('') // 搜索框绑定的输入内容

// 数据格式化函数，确保返回的是一个数组
function normalizeList(res) {
  if (!res) return []
  if (Array.isArray(res)) return res
  if (res.content && Array.isArray(res.content)) return res.content
  if (res.data && Array.isArray(res.data)) return res.data
  return []
}

// 加载租金记录（支持搜索合同编号或合同ID）
async function loadItems() {
  try {
    const baseQuery = '?page=0&size=20' // 分页参数
    // 如果输入了搜索内容，先尝试按合同ID查找
    if (q.value) {
      const maybeNum = Number(q.value)
      if (Number.isFinite(maybeNum) && maybeNum > 0) {
        const res = await get(`/rent-payments/search?contractId=${maybeNum}&page=0&size=20`)
        items.value = normalizeList(res)
        // 如果查到了结果则直接返回
        if ((items.value || []).length > 0) return
      }

      // 否则按合同编号查找，再通过合同ID查租金
      try {
        const cRes = await get(`/contracts/search?contractNo=${encodeURIComponent(q.value)}&page=0&size=20`)
        const contracts = normalizeList(cRes)
        if (contracts.length > 0) {
          const contractId = contracts[0].id
          const rpRes = await get(`/rent-payments/search?contractId=${contractId}&page=0&size=20`)
          items.value = normalizeList(rpRes)
          return
        }
      } catch (e) {
        // 忽略错误并继续执行默认加载
        console.warn('合同搜索回退失败', e)
      }
    }

    // 默认加载全部租金记录
    const res = await get(`/rent-payments/search${baseQuery}`)
    items.value = normalizeList(res)
  } catch (e) {
    console.error('获取租金记录失败', e)
    items.value = [] // 出错则清空列表
  }
}

// 删除租金记录
async function del(id) {
  if (!confirm('确认删除该记录？')) return // 弹出确认框
  try {
    await apiDel(`/rent-payments/${id}`) // 调用删除接口
    await loadItems() // 重新加载数据
  } catch (e) {
    console.error('删除失败', e)
    try { await loadItems() } catch (_) {}
    alert('删除失败：' + (e.message || '请检查控制台')) // 显示错误信息
  }
}

// 格式化日期
function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN') // 转换为中文日期格式
}

// 获取状态显示文本
function getStatusText(status) {
  const statusMap = {
    PAID: '已缴费',
    PENDING: '未缴费',
    OVERDUE: '已逾期'
  }
  return statusMap[status] || status // 若未匹配到则返回原值
}

// 获取状态对应的样式类
function getStatusClass(status) {
  return {
    'status-tag': true,
    'status-success': status === 'PAID', // 已缴费绿色
    'status-warning': false, // 未启用警告状态
    'status-danger': status === 'PENDING' || status === 'OVERDUE' // 未缴费或逾期红色
  }
}

// 标记租金提醒操作
async function markRemind(id) {
  try {
    await post(`/rent-payments/${id}/remind`) // 调用提醒接口
    alert('已发送提醒（后端已标记）')
    await loadItems()
  } catch (e) {
    console.error('提醒失败', e)
    alert('提醒失败：' + (e.message || '请检查控制台'))
  }
}

// 标记租金为已支付
async function markPaid(id) {
  try {
    await post(`/rent-payments/${id}/pay`) // 调用标记已付接口
    alert('已标记为已付')
    await loadItems()
  } catch (e) {
    console.error('标记已付失败', e)
    alert('标记失败：' + (e.message || '请检查控制台'))
  }
}

// 显示逾期租金列表
async function showOverdue() {
  try {
    const res = await get('/rent-payments/overdue') // 调用后端逾期查询接口
    items.value = normalizeList(res) // 更新表格为逾期数据
    q.value = '' // 清空搜索框
  } catch (e) {
    console.error('获取逾期列表失败', e)
    alert('获取逾期列表失败：' + (e.message || '请检查控制台'))
  }
}

onMounted(loadItems) // 页面加载时自动获取租金记录
</script>

<style scoped>
.status-tag {
  padding: 2px 8px; /* 状态标签内边距 */
  border-radius: 4px; /* 圆角 */
  font-size: 12px;
  font-weight: 500;
}

.status-success {
  background: #e1f3d8; /* 背景绿色 */
  color: #67c23a; /* 字体绿色 */
}

.status-warning {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-danger {
  background: #fef0f0; /* 背景浅红 */
  color: #f56c6c; /* 字体红色 */
}
</style>
