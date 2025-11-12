<template>
  <div class="col-md-8 offset-md-2">
    <h3>创建合同</h3>
    <form @submit.prevent="submit">  <!-- 提交表单时调用 submit 方法，并阻止默认提交行为 -->
      <div class="mb-3">
        <label class="form-label">合同编号</label>
        <input v-model="form.contractNo" class="form-control" required />  <!-- 双向绑定合同编号 -->
      </div>
      <div class="row">
        <div class="col-md-4 mb-3">
          <label class="form-label">房屋</label>
          <select v-model.number="form.houseId" class="form-select" required>  <!-- 选择房屋ID -->
            <option value="">-- 请选择房屋 --</option>
            <option v-for="h in houses" :key="h.id" :value="h.id">{{ h.address }} (id: {{ h.id }})</option>  <!-- 遍历房屋列表 -->
          </select>
        </div>
        <div class="col-md-4 mb-3">
          <label class="form-label">租客</label>
          <select v-model.number="form.tenantId" class="form-select" required>  <!-- 选择租客ID -->
            <option value="">-- 请选择租客 --</option>
            <option v-for="t in tenants" :key="t.id" :value="t.id">{{ t.realName }} (id: {{ t.id }})</option>  <!-- 遍历租客列表 -->
          </select>
        </div>
        <div class="col-md-4 mb-3">
          <label class="form-label">房东</label>
          <select v-model.number="form.landlordId" class="form-select" required>  <!-- 选择房东ID -->
            <option value="">-- 请选择房东 --</option>
            <option v-for="u in landlords" :key="u.id" :value="u.id">{{ u.realName }} (id: {{ u.id }})</option>  <!-- 遍历房东列表 -->
          </select>
        </div>
      </div>
      <div class="row">
        <div class="col-md-6 mb-3">
          <label class="form-label">开始日期</label>
          <input type="date" v-model="form.startDate" class="form-control" required />  <!-- 租约开始日期 -->
        </div>
        <div class="col-md-6 mb-3">
          <label class="form-label">结束日期</label>
          <input type="date" v-model="form.endDate" class="form-control" required />  <!-- 租约结束日期 -->
        </div>
      </div>
      <div class="mb-3">
        <label class="form-label">租金金额</label>
        <input type="number" v-model.number="form.rentAmount" class="form-control" required />  <!-- 租金输入 -->
      </div>
      <div class="mb-3">
        <label class="form-label">违约条款</label>
        <textarea v-model="form.breachClause" class="form-control" rows="3" placeholder="可选：填写违约责任描述"></textarea>  <!-- 可选违约条款 -->
      </div>
      <div class="text-end">
        <router-link to="/contracts" class="btn btn-secondary">取消</router-link>  <!-- 返回合同列表 -->
        <button class="btn btn-success ms-2" type="submit">保存</button>  <!-- 保存按钮 -->
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'  // 导入 Vue 组合式 API
import { post, get } from '@/utils/api'  // 导入封装好的 GET/POST 请求方法
import { useRouter } from 'vue-router'  // 导入路由对象

const router = useRouter()  // 获取路由实例，用于页面跳转
// 定义表单数据对象
const form = ref({ contractNo: '', houseId: null, tenantId: null, landlordId: null, startDate: '', endDate: '', rentAmount: null, breachClause: '' })
const landlords = ref([])  // 房东列表
const houses = ref([])     // 房屋列表
const tenants = ref([])    // 租客列表

onMounted(async () => {  // 组件挂载时执行
  try {
    // 并发请求用户、房屋、租客数据
    const [uRes, hRes, tRes] = await Promise.all([
      get('/users?page=0&size=200'),
      get('/houses/search?page=0&size=200'),
      get('/users?page=0&size=200')
    ])
    // users 作为房东和租客使用（后期可添加筛选）
    landlords.value = uRes?.content || []
    tenants.value = tRes?.content || []
    houses.value = hRes?.content || []
  } catch (e) {
    console.error('加载下拉数据失败', e)  // 请求失败时输出错误
  }
})

async function submit() {  // 提交表单函数
  try {
    // 简单前端校验：确保 ID 为数字且日期有效
    const houseId = Number(form.value.houseId)
    const tenantId = Number(form.value.tenantId)
    const landlordId = Number(form.value.landlordId)
    if (!Number.isFinite(houseId) || houseId <= 0) return alert('请输入有效的房屋ID（数字）')
    if (!Number.isFinite(tenantId) || tenantId <= 0) return alert('请输入有效的租客ID（数字）')
    if (form.value.landlordId && (!Number.isFinite(landlordId) || landlordId <= 0)) return alert('请输入有效的房东ID（数字）')

    // 将日期转换为 ISO 格式（YYYY-MM-DD）
    function toIsoDate(s) {
      const d = new Date(s)
      if (Number.isNaN(d.getTime())) return null
      return d.toISOString().slice(0,10)
    }
    const sd = toIsoDate(form.value.startDate)
    const ed = toIsoDate(form.value.endDate)
    if (!sd || !ed) return alert('请填写有效的开始和结束日期，格式为 YYYY-MM-DD')

    // 后端期望 house/tenant/landlord 为嵌套对象
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
    const res = await post('/contracts', payload)  // 向后端发送 POST 请求保存合同
    if (res && res.id) router.push('/contracts')  // 保存成功则跳转合同列表页
    else alert('保存可能失败：' + JSON.stringify(res))  // 返回异常时提示
  } catch (e) {
    console.error('保存合同失败', e)  // 控制台输出错误
    alert('保存失败：' + (e.message || '请检查控制台'))  // 弹出提示信息
  }
}

  // 根据用户要求移除的模板生成函数
</script>
