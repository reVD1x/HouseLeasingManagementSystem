<template>
  <div class="col-md-8 offset-md-2">
    <h3>新增房源</h3>
    <form @submit.prevent="submit">
      <div class="mb-3">
        <label class="form-label">房屋地址</label>
        <input v-model="form.address" class="form-control" required />
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
      <div class="mb-3">
        <label class="form-label">房源状态 *</label>
        <select v-model="form.status" class="form-select" required>
          <option value="">请选择</option>
          <option value="AVAILABLE">可租</option>
          <option value="RENTED">已租</option>
        </select>
      </div>
      <div class="mb-3">
        <label class="form-label">房东（选择已有用户） *</label>
        <select v-model.number="form.landlordId" class="form-select" required>
          <option value="">-- 请选择房东 --</option>
          <option v-for="u in landlords" :key="u.id" :value="u.id">{{ u.realName }} (id: {{ u.id }})</option>
        </select>
        <div class="form-text">从用户列表中选择房东，避免手动输入 ID 错误</div>
      </div>
      <div class="text-end">
        <router-link to="/houses" class="btn btn-secondary">取消</router-link>
        <button class="btn btn-success ms-2" type="submit">保存</button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { post, get } from '@/utils/api'
import { useRouter } from 'vue-router'

const router = useRouter()
const form = ref({ address: '', houseType: '', area: null, rent: null, decoration: '', facilities: '', status: 'AVAILABLE', landlordId: null })
const landlords = ref([])

onMounted(async () => {
  try {
    const res = await get('/users?page=0&size=200')
    landlords.value = res?.content || []
  } catch (e) {
    console.error('加载用户列表失败', e)
  }
})

async function submit() {
  try {
    // ensure landlordId present and numeric
    const landlordId = Number(form.value.landlordId)
    if (!Number.isFinite(landlordId) || landlordId <= 0) return alert('请输入有效的房东ID（数字）')
    const payload = { ...form.value, landlordId: landlordId }
    const res = await post('/houses', payload)
    // 如果返回错误对象，后端可能返回 400+ 和 message
    if (res && res.id) {
      router.push('/houses')
    } else {
      alert('保存可能失败：' + JSON.stringify(res))
    }
  } catch (e) {
    console.error('保存房源失败', e)
    alert('保存失败：' + (e.message || '请检查控制台'))
  }
}
</script>
