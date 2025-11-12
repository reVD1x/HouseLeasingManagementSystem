<template>
  <div class="col-md-8 offset-md-2">
    <h3>编辑合同</h3>
    <!-- 只有加载完成后显示表单 -->
    <form v-if="loaded" @submit.prevent="submit">
      <div class="mb-3">
        <label class="form-label">合同编号</label>
        <input v-model="form.contractNo" class="form-control" required /> <!-- 双向绑定合同编号 -->
      </div>
      <div class="row">
        <div class="col-md-4 mb-3">
          <label class="form-label">房屋</label>
          <select v-model.number="form.houseId" class="form-select" required>
            <option value="">-- 请选择房屋 --</option>
            <!-- 遍历房屋列表生成下拉选项 -->
            <option v-for="h in houses" :key="h.id" :value="h.id">{{ h.address }} (id: {{ h.id }})</option>
          </select>
        </div>
        <div class="col-md-4 mb-3">
          <label class="form-label">租客</label>
          <select v-model.number="form.tenantId" class="form-select" required>
            <option value="">-- 请选择租客 --</option>
            <!-- 遍历租客列表生成下拉选项 -->
            <option v-for="t in tenants" :key="t.id" :value="t.id">{{ t.realName }} (id: {{ t.id }})</option>
          </select>
        </div>
        <div class="col-md-4 mb-3">
          <label class="form-label">房东</label>
          <select v-model.number="form.landlordId" class="form-select" required>
            <option value="">-- 请选择房东 --</option>
            <!-- 遍历房东列表生成下拉选项 -->
            <option v-for="u in landlords" :key="u.id" :value="u.id">{{ u.realName }} (id: {{ u.id }})</option>
          </select>
        </div>
      </div>

      <div class="row">
        <div class="col-md-6 mb-3">
          <label class="form-label">开始日期</label>
          <input type="date" v-model="form.startDate" class="form-control" required /> <!-- 开始日期 -->
        </div>
        <div class="col-md-6 mb-3">
          <label class="form-label">结束日期</label>
          <input type="date" v-model="form.endDate" class="form-control" required /> <!-- 结束日期 -->
        </div>
      </div>

      <div class="mb-3">
        <label class="form-label">租金金额</label>
        <input type="number" v-model.number="form.rentAmount" class="form-control" required /> <!-- 租金 -->
      </div>

      <div class="mb-3">
        <label class="form-label">违约条款</label>
        <textarea v-model="form.breachClause" class="form-control" rows="3"></textarea> <!-- 可选违约条款 -->
      </div>

      <div class="text-end">
        <router-link to="/contracts" class="btn btn-secondary">取消</router-link> <!-- 取消返回列表 -->
        <button class="btn btn-success ms-2" type="submit">保存修改</button> <!-- 提交修改 -->
      </div>
    </form>

    <!-- 加载中提示 -->
    <div v-else class="text-center">载入中...</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get, put } from '@/utils/api' // 导入接口方法
import { useRoute, useRouter } from 'vue-router'

const route = useRoute() // 获取路由参数
const router = useRouter() // 用于页面跳转
const id = route.params.id // 合同ID
// 表单数据
const form = ref({ contractNo: '', houseId: null, tenantId: null, landlordId: null, startDate: '', endDate: '', rentAmount: null, breachClause: '' })
const landlords = ref([]) // 房东列表
const houses = ref([]) // 房屋列表
const tenants = ref([]) // 租客列表
const loaded = ref(false) // 是否完成数据加载

// 加载合同和下拉列表数据
async function load() {
  try {
    const [cRes, uRes, hRes] = await Promise.all([
      get(`/contracts/${id}`), // 获取合同详情
      get('/users?page=0&size=200'), // 获取用户列表（房东/租客）
      get('/houses/search?page=0&size=200') // 获取房屋列表
    ])
    // 初始化表单数据
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
    loaded.value = true // 数据加载完成
  } catch (e) {
    console.error('加载合同或下拉数据失败', e)
    alert('加载失败：' + (e.message || JSON.stringify(e)))
  }
}

// 提交修改
async function submit() {
  try {
    // 数字类型转换与前端校验
    const houseId = Number(form.value.houseId)
    const tenantId = Number(form.value.tenantId)
    const landlordId = Number(form.value.landlordId)
    if (!Number.isFinite(houseId) || houseId <= 0) return alert('请输入有效的房屋ID（数字）')
    if (!Number.isFinite(tenantId) || tenantId <= 0) return alert('请输入有效的租客ID（数字）')
    if (form.value.landlordId && (!Number.isFinite(landlordId) || landlordId <= 0)) return alert('请输入有效的房东ID（数字）')

    // 日期格式处理
    function toIsoDate(s) {
      const d = new Date(s)
      if (Number.isNaN(d.getTime())) return null
      return d.toISOString().slice(0,10)
    }
    const sd = toIsoDate(form.value.startDate)
    const ed = toIsoDate(form.value.endDate)
    if (!sd || !ed) return alert('请填写有效的开始和结束日期，格式为 YYYY-MM-DD')

    // 构造请求体
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
    await put(`/contracts/${id}`, payload) // 发送修改请求
    router.push('/contracts') // 返回合同列表
  } catch (e) {
    console.error('保存失败', e)
    alert('保存失败：' + (e.message || '请检查控制台'))
  }
}

// 页面加载完成后执行 load 函数
onMounted(load)
</script>
