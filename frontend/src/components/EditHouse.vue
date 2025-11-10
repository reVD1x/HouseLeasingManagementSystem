<template>
  <div class="col-md-8 offset-md-2">
    <h3>编辑房源</h3>
    <form v-if="loaded" @submit.prevent="submit">
      <div class="mb-3">
        <input v-model="form.address" class="form-control" required />
      </div>
        <div class="mb-3">
          <label class="form-label">房东（选择已有用户） *</label>
          <select v-model.number="form.landlordId" class="form-select" required>
            <option value="">-- 请选择房东 --</option>
            <option v-for="u in landlords" :key="u.id" :value="u.id">{{ u.realName }} (id: {{ u.id }})</option>
          </select>
          <div class="form-text">从用户列表中选择房东，避免手动输入 ID 错误</div>
        </div>
      <div class="row">
        <div class="col-md-4 mb-3">
          <label class="form-label">户型</label>
          <input v-model="form.houseType" class="form-control" />
        </div>
        <div class="col-md-4 mb-3">
          <label class="form-label">面积(㎡)</label>
          <input type="number" v-model.number="form.area" class="form-control" />
        </div>
        <div class="col-md-4 mb-3">
          <label class="form-label">租金(元/月)</label>
          <input type="number" v-model.number="form.rent" class="form-control" />
        </div>
      </div>
      <div class="mb-3">
        <label class="form-label">装修情况</label>
        <input v-model="form.decoration" class="form-control" />
      </div>
      <div class="mb-3">
        <label class="form-label">配套设施</label>
        <input v-model="form.facilities" class="form-control" />
      </div>
      
      <div class="text-end">
        <router-link to="/houses" class="btn btn-secondary">取消</router-link>
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
const form = ref({ id: null, address: '', houseType: '', area: null, rent: null, decoration: '', facilities: '', status: 'AVAILABLE', landlordId: null })
const landlords = ref([])
const loaded = ref(false)

async function load() {
  try {
    const res = await get(`/houses/${id}`)
    // map fields from entity to DTO-like form
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
      // 后端 house 结构可能包含 landlord 对象
      if (res.landlord && typeof res.landlord.id !== 'undefined') {
        form.value.landlordId = res.landlord.id
      }
    loaded.value = true
  } catch (e) {
    console.error('加载房源失败', e)
    // 尝试显示更详细的错误信息，便于调试（可能包含非 JSON 字符串）
    const msg = e && (e.message || e.body || JSON.stringify(e))
    alert('加载失败：' + (msg || '请检查控制台'))
  }
}

async function submit() {
  try {
  // ensure landlordId present and numeric
  const landlordId = Number(form.value.landlordId)
  if (!Number.isFinite(landlordId) || landlordId <= 0) return alert('请输入有效的房东ID（数字）')
  const payload = { ...form.value, landlordId: landlordId }
  const res = await put(`/houses/${id}`, payload)
    router.push('/houses')
  } catch (e) {
    console.error('保存失败', e)
    alert('保存失败：' + (e.message || '请检查控制台'))
  }
}

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
