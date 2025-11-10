<template>
  <div class="login-page container mt-5" style="max-width:420px;">
    <h3>登录</h3>
    <div class="mb-3">
      <label class="form-label">口令</label>
      <input v-model="password" type="password" class="form-control" placeholder="请输入口令" />
    </div>
    <div class="mb-3 text-danger" v-if="error">{{ error }}</div>
    <div>
      <button class="btn btn-primary me-2" @click="submit">登录</button>
      <button class="btn btn-secondary" @click="cancel">取消</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { post } from '@/utils/api'

const password = ref('')
const error = ref('')
const router = useRouter()

async function submit() {
  error.value = ''
  try {
    const res = await post('/auth/login', { password: password.value })
    if (res && res.success) {
      // 后端返回 token 字段
      if (res.token) {
        localStorage.setItem('access-token', res.token)
        // 通知全局登录状态变化
        window.dispatchEvent(new Event('auth-changed'))
        router.push('/')
      } else {
        error.value = '未收到 token，请检查后端返回值。'
      }
    } else {
      error.value = res?.message || '登录失败'
    }
  } catch (e) {
    console.error('登录错误', e)
    error.value = e.message || '网络或服务器错误'
  }
}

function cancel() {
  router.push('/')
}
</script>

<style scoped>
.login-page { padding: 1rem; }
</style>
