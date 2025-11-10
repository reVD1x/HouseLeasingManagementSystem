<template>
  <div class="col-md-8 offset-md-2">
    <h3>新增租金记录</h3>
    <form @submit.prevent="submit">
      <div class="mb-3">
        <label class="form-label">合同ID</label>
        <input v-model.number="form.contractId" class="form-control" required />
      </div>
      <div class="mb-3">
        <label class="form-label">应付日期</label>
        <input type="date" v-model="form.dueDate" class="form-control" required />
      </div>
      <div class="mb-3">
        <label class="form-label">状态</label>
        <select v-model="form.status" class="form-select">
          <option value="PENDING">待付</option>
          <option value="PAID">已付</option>
          <option value="OVERDUE">已逾期</option>
        </select>
      </div>
      <div class="row">
        <div class="col-md-6 mb-3">
          <label class="form-label">计费周期开始（可选）</label>
          <input type="date" v-model="form.periodStart" class="form-control" />
        </div>
        <div class="col-md-6 mb-3">
          <label class="form-label">计费周期结束（可选）</label>
          <input type="date" v-model="form.periodEnd" class="form-control" />
        </div>
      </div>
      <div class="mb-3">
        <label class="form-label">逾期罚金（可选）</label>
        <input type="number" v-model.number="form.penalty" class="form-control" />
      </div>
      <div class="mb-3">
        <label class="form-label">金额</label>
        <input type="number" v-model.number="form.amount" class="form-control" required />
      </div>
      <div class="text-end">
        <router-link to="/rent-payments" class="btn btn-secondary">取消</router-link>
        <button class="btn btn-success ms-2" type="submit">保存</button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { post } from '@/utils/api'
import { useRouter } from 'vue-router'

const router = useRouter()
const form = ref({ contractId: null, dueDate: '', amount: null, status: 'PENDING', periodStart: '', periodEnd: '', penalty: null })

async function submit() {
  try {
    const contractId = Number(form.value.contractId)
    if (!Number.isFinite(contractId) || contractId <= 0) return alert('请输入有效的合同ID（数字）')
    const d = new Date(form.value.dueDate)
    if (Number.isNaN(d.getTime())) return alert('请输入有效的应付日期')
    const payload = {
      contract: { id: contractId },
      dueDate: d.toISOString().slice(0,10),
      amount: Number(form.value.amount),
      status: form.value.status,
      periodStart: form.value.periodStart ? new Date(form.value.periodStart).toISOString().slice(0,10) : undefined,
      periodEnd: form.value.periodEnd ? new Date(form.value.periodEnd).toISOString().slice(0,10) : undefined,
      penalty: form.value.penalty ? Number(form.value.penalty) : undefined
    }
    const res = await post('/rent-payments', payload)
    if (res && res.id) router.push('/rent-payments')
    else alert('保存可能失败：' + JSON.stringify(res))
  } catch (e) {
    console.error('保存租金失败', e)
    alert('保存失败：' + (e.message || '请检查控制台'))
  }
}
</script>
