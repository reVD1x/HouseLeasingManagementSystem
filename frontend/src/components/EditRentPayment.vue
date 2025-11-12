<template>
  <div class="col-md-8 offset-md-2">
    <h3>编辑租金记录</h3>
    <!-- 加载完成后显示表单 -->
    <form v-if="loaded" @submit.prevent="submit">
      <div class="mb-3">
        <label class="form-label">合同ID</label>
        <input v-model.number="form.contractId" class="form-control" required /> <!-- 合同ID -->
      </div>

      <div class="mb-3">
        <label class="form-label">应付日期</label>
        <input type="date" v-model="form.dueDate" class="form-control" required /> <!-- 应付日期 -->
      </div>

      <div class="mb-3">
        <label class="form-label">金额</label>
        <input type="number" v-model.number="form.amount" class="form-control" required /> <!-- 金额 -->
      </div>

      <div class="mb-3">
        <label class="form-label">状态</label>
        <select v-model="form.status" class="form-select">
          <option value="PENDING">待付</option>
          <option value="PAID">已付</option>
          <option value="OVERDUE">已逾期</option>
        </select> <!-- 租金状态 -->
      </div>

      <div class="row">
        <div class="col-md-6 mb-3">
          <label class="form-label">计费周期开始（可选）</label>
          <input type="date" v-model="form.periodStart" class="form-control" /> <!-- 计费周期开始 -->
        </div>
        <div class="col-md-6 mb-3">
          <label class="form-label">计费周期结束（可选）</label>
          <input type="date" v-model="form.periodEnd" class="form-control" /> <!-- 计费周期结束 -->
        </div>
      </div>

      <div class="mb-3">
        <label class="form-label">逾期罚金（可选）</label>
        <input type="number" v-model.number="form.penalty" class="form-control" /> <!-- 逾期罚金 -->
      </div>

      <div class="text-end">
        <router-link to="/rent-payments" class="btn btn-secondary">取消</router-link> <!-- 返回列表 -->
        <button class="btn btn-success ms-2" type="submit">保存修改</button> <!-- 提交修改 -->
      </div>
    </form>

    <!-- 加载中提示 -->
    <div v-else class="text-center">载入中...</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get, put } from '@/utils/api'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const id = route.params.id // 获取路由参数id

// 表单数据初始化
const form = ref({
  id: null,
  contractId: null,
  dueDate: '',
  amount: null,
  status: 'PENDING',
  periodStart: '',
  periodEnd: '',
  penalty: null
})
const loaded = ref(false) // 是否加载完成

// 加载租金记录数据
async function load() {
  try {
    const res = await get(`/rent-payments/${id}`)
    form.value = {
      id: res.id,
      contractId: res.contractId,
      dueDate: res.dueDate,
      amount: res.amount,
      status: res.status || 'PENDING',
      periodStart: res.periodStart || '',
      periodEnd: res.periodEnd || '',
      penalty: res.penalty || null
    }
    loaded.value = true
  } catch (e) {
    console.error('加载失败', e)
    alert('加载失败')
  }
}

// 提交修改
async function submit() {
  try {
    const contractId = Number(form.value.contractId)
    if (!Number.isFinite(contractId) || contractId <= 0) return alert('请输入有效的合同ID（数字）')

    const d = new Date(form.value.dueDate)
    if (Number.isNaN(d.getTime())) return alert('请输入有效的应付日期')

    const payload = {
      contract: { id: contractId }, // 后端需要嵌套对象
      dueDate: d.toISOString().slice(0, 10), // 转为 YYYY-MM-DD
      amount: Number(form.value.amount),
      status: form.value.status,
      periodStart: form.value.periodStart ? new Date(form.value.periodStart).toISOString().slice(0, 10) : undefined,
      periodEnd: form.value.periodEnd ? new Date(form.value.periodEnd).toISOString().slice(0, 10) : undefined,
      penalty: form.value.penalty ? Number(form.value.penalty) : undefined
    }

    await put(`/rent-payments/${id}`, payload) // 调用后端接口保存
    router.push('/rent-payments') // 返回列表页
  } catch (e) {
    console.error('保存失败', e)
    alert('保存失败：' + (e.message || '请检查控制台'))
  }
}

// 页面加载时调用
onMounted(load)
</script>
