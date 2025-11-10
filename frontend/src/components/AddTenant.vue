<template>
  <div class="col-md-8 offset-md-2">
    <h3>新增租客</h3>
    <form @submit.prevent="submit">
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
const form = ref({ realName: '', idCard: '', phone: '' })

async function submit() {
  try {
    const res = await post('/users', form.value)
    if (res && res.id) router.push('/tenants')
    else alert('保存可能失败：' + JSON.stringify(res))
  } catch (e) {
    console.error('保存租客失败', e)
    alert('保存失败：' + (e.message || '请检查控制台'))
  }
}
</script>
