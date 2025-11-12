<template>
  <div class="form-page">
    <h2>发布评价</h2>
    <form @submit.prevent="submit"> <!-- 阻止表单默认提交，调用自定义 submit 方法 -->
      <div class="row"><label>合同/房源</label>
        <div v-if="loadingContracts">加载合同中…</div> <!-- 如果正在加载合同则显示提示 -->
        <div v-else>
          <select v-model="form.contractId" @change="onContractChange"> <!-- 选择合同时触发更新逻辑 -->
            <option value="">-- 请选择合同 --</option>
            <!-- 遍历合同列表显示合同编号和房源地址 -->
            <option v-for="c in contracts" :key="c.id" :value="c.id">
              {{ c.contractNo }} - {{ c.houseAddress || (c.house && c.house.address) }}
            </option>
          </select>
        </div>
      </div>

      <div class="row"><label>作者</label>
        <div v-if="loadingUsers">加载用户中…</div> <!-- 用户数据加载提示 -->
        <div v-else>
          <select v-model="form.reviewerId" @change="onReviewerChange"> <!-- 选择作者（评价人） -->
            <option value="">-- 请选择作者 --</option>
            <option v-for="u in users" :key="u.id" :value="u.id">
              {{ u.realName || u.name || u.username }}
            </option>
          </select>
        </div>
      </div>

      <div class="row"><label>被评价人</label>
        <input readonly :value="selectedRevieweeName" /> <!-- 根据选择的合同自动显示被评价人 -->
      </div>

      <div class="row"><label>评分（1-5）</label><input type="number" v-model.number="form.rating" min="1" max="5" /></div> <!-- 输入评分 -->
      <div class="row"><label>评价内容</label><textarea v-model="form.comment"></textarea></div> <!-- 输入评价文字 -->
      <div class="actions"><button type="submit">提交</button><router-link to="/reviews">取消</router-link></div> <!-- 提交或取消按钮 -->
    </form>
    <div v-if="error" class="error">{{ error }}</div> <!-- 错误信息显示 -->
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue' // 引入Vue的核心函数
import api from '@/api' // 引入封装好的API模块
import { useRouter } from 'vue-router' // 引入路由功能

const router = useRouter() // 获取路由对象
const error = ref('') // 错误提示信息
// 初始化表单数据，默认评价类型为租客对房东
const form = ref({ contractId: '', reviewerId: '', revieweeId: '', reviewType: 'TENANT_TO_LANDLORD', rating: 5, comment: '' })

const contracts = ref([]) // 合同列表
const users = ref([]) // 用户列表
const loadingContracts = ref(false) // 合同加载状态
const loadingUsers = ref(false) // 用户加载状态

// 通用数据格式处理函数，确保接口返回格式统一为数组
function normalizeList(res) {
  if (!res) return []
  if (Array.isArray(res)) return res
  if (res.content && Array.isArray(res.content)) return res.content
  if (res.data && Array.isArray(res.data)) return res.data
  return []
}

// 加载合同列表
async function loadContracts() {
  loadingContracts.value = true
  try {
    const res = await api.ContractsAPI.search({ page: 0, size: 200 }) // 调用合同接口
    contracts.value = normalizeList(res)
  } catch (e) {
    console.error('加载合同失败', e)
    contracts.value = [] // 出错时设为空数组
  } finally {
    loadingContracts.value = false
  }
}

// 加载用户列表
async function loadUsers() {
  loadingUsers.value = true
  try {
    const res = await api.UsersAPI.list({ page: 0, size: 200 }) // 调用用户接口
    users.value = normalizeList(res)
  } catch (e) {
    console.error('加载用户失败', e)
    users.value = []
  } finally {
    loadingUsers.value = false
  }
}

// 根据选中的合同和作者动态确定“被评价人”和“评价类型”
function updateRevieweeAndType() {
  const cid = form.value.contractId
  if (!cid) { form.value.revieweeId = ''; return } // 未选择合同则清空
  const c = contracts.value.find(x => String(x.id) === String(cid)) // 查找对应合同
  if (!c) { form.value.revieweeId = ''; return }

  const tenantId = c.tenantId ?? (c.tenant && c.tenant.id) ?? null // 获取租客ID
  const landlordId = c.landlordId ?? (c.landlord && c.landlord.id) ?? null // 获取房东ID
  const reviewerId = form.value.reviewerId ? String(form.value.reviewerId) : null

  // 若作者为租客，则被评价人为房东
  if (reviewerId && tenantId && String(reviewerId) === String(tenantId)) {
    form.value.revieweeId = landlordId || ''
    form.value.reviewType = 'TENANT_TO_LANDLORD'
    return
  }
  // 若作者为房东，则被评价人为租客
  if (reviewerId && landlordId && String(reviewerId) === String(landlordId)) {
    form.value.revieweeId = tenantId || ''
    form.value.reviewType = 'LANDLORD_TO_TENANT'
    return
  }
  // 默认情况：如果无法判断，则优先设定为房东评价租客
  if (tenantId) {
    form.value.revieweeId = tenantId
    form.value.reviewType = 'LANDLORD_TO_TENANT'
  } else if (landlordId) {
    form.value.revieweeId = landlordId
    form.value.reviewType = 'TENANT_TO_LANDLORD'
  } else {
    form.value.revieweeId = ''
  }
}

// 合同或作者变化时更新评价对象
function onContractChange() { updateRevieweeAndType() }
function onReviewerChange() { updateRevieweeAndType() }

// 计算属性：显示被评价人的姓名
const selectedRevieweeName = computed(() => {
  if (!form.value.revieweeId) return ''
  const u = users.value.find(x => String(x.id) === String(form.value.revieweeId))
  return u ? (u.realName || u.name || u.username || '') : ''
})

// 表单提交函数
async function submit() {
  error.value = ''
  // 基础校验
  if (!form.value.contractId) { error.value = '请选择合同'; return }
  if (!form.value.reviewerId) { error.value = '请选择作者（评价人）'; return }
  if (!form.value.revieweeId) { error.value = '无法确定被评价人，请选择正确的合同或手动填写'; return }

  try {
    // 构造请求数据
    const payload = {
      contractId: Number(form.value.contractId),
      reviewerId: Number(form.value.reviewerId),
      revieweeId: Number(form.value.revieweeId),
      reviewType: form.value.reviewType,
      rating: form.value.rating,
      comment: form.value.comment
    }
    await api.ReviewsAPI.create(payload) // 调用后端接口提交评价
    router.push('/reviews') // 跳转至评价列表页
  } catch (e) {
    console.error(e)
    error.value = '提交失败，请稍后重试' // 错误提示
  }
}

// 组件挂载时加载合同与用户数据
onMounted(() => { loadContracts(); loadUsers() })
</script>

<style scoped>
.form-page{padding:12px} /* 页面整体内边距 */
.row{margin-bottom:8px} /* 每行间距 */
.row label{display:block;font-size:13px;color:#444;margin-bottom:4px} /* 标签样式 */
.row input,.row textarea, .row select{width:100%;padding:8px;border:1px solid #ddd;border-radius:4px} /* 表单控件样式 */
.actions{display:flex;gap:12px;align-items:center;margin-top:12px} /* 按钮区域布局 */
.error{color:#c0392b;margin-top:8px} /* 错误信息样式 */
</style>
