<template>
  <div class="col-md-8 offset-md-2">
    <h3>编辑房源</h3>
    <!-- 加载完成后显示表单 -->
    <form v-if="loaded" @submit.prevent="submit">
      <div class="mb-3">
        <input v-model="form.address" class="form-control" required /> <!-- 房屋地址 -->
      </div>
      <div class="mb-3">
        <label class="form-label">房东（选择已有用户） *</label>
        <select v-model.number="form.landlordId" class="form-select" required>
          <option value="">-- 请选择房东 --</option>
          <!-- 遍历房东列表生成选项 -->
          <option v-for="u in landlords" :key="u.id" :value="u.id">{{ u.realName }} (id: {{ u.id }})</option>
        </select>
      </div>

      <div class="row">
        <div class="col-md-4 mb-3">
          <label class="form-label">户型</label>
          <input v-model="form.houseType" class="form-control" /> <!-- 户型 -->
        </div>
        <div class="col-md-4 mb-3">
          <label class="form-label">面积(㎡)</label>
          <input type="number" v-model.number="form.area" class="form-control" /> <!-- 面积 -->
        </div>
        <div class="col-md-4 mb-3">
          <label class="form-label">租金(元/月)</label>
          <input type="number" v-model.number="form.rent" class="form-control" /> <!-- 租金 -->
        </div>
      </div>

      <div class="mb-3">
        <label class="form-label">装修情况</label>
        <input v-model="form.decoration" class="form-control" /> <!-- 装修情况 -->
      </div>
      <div class="mb-3">
        <label class="form-label">配套设施</label>
        <input v-model="form.facilities" class="form-control" /> <!-- 配套设施 -->
      </div>

      <div class="text-end">
        <router-link to="/houses" class="btn btn-secondary">取消</router-link> <!-- 返回列表 -->
        <button class="btn btn-success ms-2" type="submit">保存修改</button> <!-- 提交 -->
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
const id = route.params.id

// 表单数据初始化
const form = ref({ id: null, address: '', houseType: '', area: null, rent: null, decoration: '', facilities: '', status: 'AVAILABLE', landlordId: null })
const landlords = ref([]) // 房东列表
const loaded = ref(false) // 是否加载完成

// 加载房源数据
async function load() {
  try {
    const res = await get(`/houses/${id}`)

    // 映射后端返回的字段到表单对象
    form.value = {
      id: res.id,
      address: res.address,
      houseType: res.houseType,
      area: res.area,
      rent: res.rent,
      decoration: res.decoration,
      facilities: res.facilities,
      status: res.status
    }

    // 处理房东字段（可能是 landlordId 或 landlord 对象）
    if (typeof res.landlordId !== 'undefined' && res.landlordId !== null) {
      form.value.landlordId = Number(res.landlordId)
    } else if (res.landlord && typeof res.landlord.id !== 'undefined') {
      form.value.landlordId = Number(res.landlord.id)
    }

    // 确保 status 为字符串
    if (res.status) form.value.status = String(res.status)
    loaded.value = true
  } catch (e) {
    console.error('加载房源失败', e)
    const msg = e && (e.message || e.body || JSON.stringify(e))
    alert('加载失败：' + (msg || '请检查控制台'))
  }
}

// 提交修改
async function submit() {
  try {
    const landlordId = Number(form.value.landlordId)
    if (!Number.isFinite(landlordId) || landlordId <= 0) return alert('请输入有效的房东ID（数字）')

    const payload = { ...form.value, landlordId: landlordId }
    await put(`/houses/${id}`, payload) // 发送 PUT 请求
    router.push('/houses') // 返回列表
  } catch (e) {
    console.error('保存失败', e)
    alert('保存失败：' + (e.message || '请检查控制台'))
  }
}

// 页面加载时获取房东列表并加载房源数据
onMounted(async () => {
  try {
    const res = await get('/users?page=0&size=200')
    landlords.value = res?.content || []
  } catch (e) {
    console.error('加载用户列表失败', e)
  }
  await load()
})
</script>
