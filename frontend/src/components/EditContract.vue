<template>
  <div class="col-md-8 offset-md-2">
    <h3>编辑合同</h3>
    <form v-if="loaded" @submit.prevent="submit">
      <div class="mb-3">
        <label class="form-label">合同编号</label>
        <input v-model="form.contractNo" class="form-control" required />
      </div>
      <div class="row">
        <div class="col-md-4 mb-3">
          <label class="form-label">房屋</label>
          <select v-model.number="form.houseId" class="form-select" required>
            <option value="">-- 请选择房屋 --</option>
            <option v-for="h in houses" :key="h.id" :value="h.id">{{ h.address }} (id: {{ h.id }})</option>
          </select>
        </div>
        <div class="col-md-4 mb-3">
          <label class="form-label">租客</label>
          <select v-model.number="form.tenantId" class="form-select" required>
            <option value="">-- 请选择租客 --</option>
            <option v-for="t in tenants" :key="t.id" :value="t.id">{{ t.realName }} (id: {{ t.id }})</option>
          </select>
        </div>
        <div class="col-md-4 mb-3">
          <label class="form-label">房东</label>
          <select v-model.number="form.landlordId" class="form-select" required>
            <option value="">-- 请选择房东 --</option>
            <option v-for="u in landlords" :key="u.id" :value="u.id">{{ u.realName }} (id: {{ u.id }})</option>
          </select>
        </div>
      </div>
      <div class="row">
        <div class="col-md-6 mb-3">
          <label class="form-label">开始日期</label>
          <input type="date" v-model="form.startDate" class="form-control" required />
        </div>
        <div class="col-md-6 mb-3">
          <label class="form-label">结束日期</label>
          <input type="date" v-model="form.endDate" class="form-control" required />
        </div>
      </div>
      <div class="mb-3">
        <label class="form-label">租金金额</label>
        <input type="number" v-model.number="form.rentAmount" class="form-control" required />
      </div>
      <div class="mb-3">
        <label class="form-label">违约条款</label>
        <textarea v-model="form.breachClause" class="form-control" rows="3"></textarea>
      </div>
      <div class="text-end">
        <router-link to="/contracts" class="btn btn-secondary">取消</router-link>
        <button class="btn btn-success ms-2" type="submit">保存修改</button>
      </div>
    </form>
    <div v-else class="text-center">载入中...</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get, put } from '@/utils/api'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const id = route.params.id
const form = ref({ contractNo: '', houseId: null, tenantId: null, landlordId: null, startDate: '', endDate: '', rentAmount: null, breachClause: '' })
const landlords = ref([])
const houses = ref([])
const tenants = ref([])
const loaded = ref(false)

async function load() {
  try {
    const [cRes, uRes, hRes] = await Promise.all([
      get(`/contracts/${id}`),
      get('/users?page=0&size=200'),
      get('/houses/search?page=0&size=200')
    ])
    form.value = {
      contractNo: cRes.contractNo,
      houseId: cRes.houseId,
      tenantId: cRes.tenantId,
      landlordId: cRes.landlordId,
      startDate: cRes.startDate,
      endDate: cRes.endDate,
      rentAmount: cRes.rentAmount,
      breachClause: cRes.breachClause || ''
    }
    landlords.value = uRes?.content || []
    tenants.value = uRes?.content || []
    houses.value = hRes?.content || []
    loaded.value = true
  } catch (e) {
    console.error('加载合同或下拉数据失败', e)
    alert('加载失败：' + (e.message || JSON.stringify(e)))
  }
}

async function submit() {
  try {
    const houseId = Number(form.value.houseId)
    const tenantId = Number(form.value.tenantId)
    const landlordId = Number(form.value.landlordId)
    if (!Number.isFinite(houseId) || houseId <= 0) return alert('请输入有效的房屋ID（数字）')
    if (!Number.isFinite(tenantId) || tenantId <= 0) return alert('请输入有效的租客ID（数字）')
    if (form.value.landlordId && (!Number.isFinite(landlordId) || landlordId <= 0)) return alert('请输入有效的房东ID（数字）')

    function toIsoDate(s) {
      const d = new Date(s)
      if (Number.isNaN(d.getTime())) return null
      return d.toISOString().slice(0,10)
    }
    const sd = toIsoDate(form.value.startDate)
    const ed = toIsoDate(form.value.endDate)
    if (!sd || !ed) return alert('请填写有效的开始和结束日期，格式为 YYYY-MM-DD')

    const payload = {
      contractNo: form.value.contractNo,
      house: { id: houseId },
      tenant: { id: tenantId },
      landlord: form.value.landlordId ? { id: landlordId } : undefined,
      startDate: sd,
      endDate: ed,
      rentAmount: Number(form.value.rentAmount),
      breachClause: form.value.breachClause
    }
    await put(`/contracts/${id}`, payload)
    router.push('/contracts')
  } catch (e) {
    console.error('保存失败', e)
    alert('保存失败：' + (e.message || '请检查控制台'))
  }
}

onMounted(load)
</script>
