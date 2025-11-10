<template>
  <div class="col-md-8 offset-md-2">
    <h3>编辑租客</h3>
    <form v-if="loaded" @submit.prevent="submit">
      <div class="mb-3">
        <label class="form-label">姓名</label>
        <input v-model="form.realName" class="form-control" required />
      </div>
      <div class="mb-3">
        <label class="form-label">身份证号</label>
        <input v-model="form.idCard" class="form-control" required />
      </div>
      <div class="mb-3">
        <label class="form-label">电话</label>
        <input v-model="form.phone" class="form-control" />
      </div>
        <div class="mb-3">
          </div>
      <div class="text-end">
        <router-link to="/tenants" class="btn btn-secondary">取消</router-link>
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
const form = ref({ id: null, realName: '', idCard: '', phone: '' })
const loaded = ref(false)

async function load() {
  try {
    const res = await get(`/users/${id}`)
  form.value = { realName: res.realName, idCard: res.idCard, phone: res.phone }
    loaded.value = true
  } catch (e) {
    console.error('加载租客失败', e)
    alert('加载失败')
  }
}

async function submit() {
  try {
    await put(`/users/${id}`, form.value)
    router.push('/tenants')
  } catch (e) {
    console.error('保存失败', e)
    alert('保存失败：' + (e.message || '请检查控制台'))
  }
}

onMounted(load)
</script>
