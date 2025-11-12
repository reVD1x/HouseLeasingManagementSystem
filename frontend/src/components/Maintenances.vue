<template>
  <div class="module-page">
    <div class="module-header">
      <div class="header-content">
        <div class="title"><h2>维修管理</h2></div> <!-- 页面标题 -->
        <router-link class="btn-add" to="/maintenances/add">+ 新增维修申请</router-link> <!-- 跳转到新增维修申请页面 -->
      </div>
    </div>

    <div class="module-content">
      <div v-if="loading" class="loading">加载中…</div> <!-- 数据加载中提示 -->
      <div v-else>
        <table class="data-table"> <!-- 数据表格 -->
          <thead>
            <tr>
              <th>ID</th>
              <th>房源</th>
              <th>问题</th>
              <th>提交者</th>
              <th>联系电话</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in items" :key="m.id"> <!-- 遍历维修记录 -->
              <td>{{ m.id }}</td> <!-- 显示维修记录ID -->
              <td>{{ getHouseDisplay(m) }}</td> <!-- 显示房源信息 -->
              <td>{{ m.description || m.issue || m.description }}</td> <!-- 显示问题描述 -->
              <td>{{ m.requesterName || m.requester || '' }}</td> <!-- 显示提交人姓名 -->
              <td>{{ m.contact || m.phone || '' }}</td> <!-- 显示联系电话 -->
              <td>{{ translateStatus(m.status) }}</td> <!-- 显示维修状态（翻译后） -->
              <td>
                <!-- 若状态不是已完成或已支付，可进行指派和完成操作 -->
                <button v-if="m.status !== 'COMPLETED' && m.status !== 'PAID'" @click="assign(m.id)">指派</button>
                <button v-if="m.status !== 'COMPLETED' && m.status !== 'PAID'" @click="closeReq(m.id)">完成</button>
                <button @click="remove(m.id)">删除</button> <!-- 删除维修记录按钮 -->
              </td>
            </tr>
            <tr v-if="items.length===0"><td colspan="7">无维修记录</td></tr> <!-- 当没有维修记录时显示 -->
          </tbody>
        </table>
      </div>

      <div v-if="error" class="error">{{ error }}</div> <!-- 错误提示信息 -->
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue' // 导入 Vue 的响应式和生命周期钩子
import api from '@/api' // 导入封装好的 API 模块

const items = ref([]) // 维修记录数据列表
const loading = ref(false) // 加载状态
const error = ref('') // 错误信息

// 数据标准化函数：确保返回的是一个数组
function normalizeList(res) {
  if (!res) return []
  if (Array.isArray(res)) return res
  if (res.content && Array.isArray(res.content)) return res.content
  if (res.data && Array.isArray(res.data)) return res.data
  return []
}

// 返回房源显示名称（优先级：houseAddress > house.address > house.houseAddress > houseId > house.id）
function getHouseDisplay(m) {
  if (!m) return ''
  if (m.houseAddress) return m.houseAddress
  if (m.house) {
    // 常见字段名判断
    const h = m.house
    if (h.address) return h.address
    if (h.houseAddress) return h.houseAddress
    if (h.addr) return h.addr
    if (h.id) return String(h.id)
  }
  if (m.houseId) return String(m.houseId)
  return ''
}

// 翻译维修状态码为中文名称（若无法识别则返回原值）
function translateStatus(status) {
  if (status === null || status === undefined) return ''
  const s = String(status).toUpperCase()
  const map = {
    PENDING: '待处理',
    IN_PROGRESS: '进行中',
    PROCESSING: '进行中',
    COMPLETED: '已完成',
    PAID: '已支付',
    CANCELLED: '已取消',
    CANCELED: '已取消'
  }
  return map[s] || status
}

// 加载维修列表数据
async function load() {
  loading.value = true // 开启加载状态
  error.value = ''
  try {
    const res = await api.MaintenanceAPI.search({ page: 0, size: 50 }) // 调用后端接口获取维修数据
    items.value = normalizeList(res) // 处理返回数据格式
  } catch (e) {
    console.error(e)
    error.value = '无法加载维修列表，显示为空。' // 加载失败提示
    items.value = []
  } finally {
    loading.value = false // 加载结束
  }
}

// 指派维修人员
async function assign(id) {
  const assigneeId = prompt('输入维修人员ID进行指派：') // 弹出输入框
  if (!assigneeId) return // 若未输入则返回
  try {
    // 更新维修状态为进行中（IN_PROGRESS）
    await api.MaintenanceAPI.updateStatus(id, 'IN_PROGRESS')
    await load() // 重新加载列表
  } catch (e) { alert('指派失败') }
}

// 完成维修
async function closeReq(id) {
  try {
    // 调用完成接口
    await api.MaintenanceAPI.complete(id)
    await load() // 刷新数据
  } catch (e) { alert('完成操作失败') }
}

// 删除维修记录
async function remove(id) {
  if (!confirm('确认删除该维修记录？')) return // 确认提示
  try {
    await api.MaintenanceAPI.delete(id) // 调用删除接口
    await load() // 刷新数据
  } catch (e) { alert('删除失败') }
}

onMounted(load) // 页面挂载时加载维修列表
</script>

<style scoped>
.module-page{padding:12px} /* 页面整体内边距 */
.header-content{display:flex;align-items:center;gap:12px} /* 顶部标题与按钮布局 */
.btn-add{margin-left:auto} /* 添加按钮靠右 */
.data-table{width:100%;border-collapse:collapse} /* 表格样式 */
.data-table th,.data-table td{border:1px solid #eee;padding:8px;text-align:left} /* 单元格样式 */
.loading{color:#666} /* 加载中文字颜色 */
.error{color:#c0392b;margin-top:8px} /* 错误信息样式 */
</style>
