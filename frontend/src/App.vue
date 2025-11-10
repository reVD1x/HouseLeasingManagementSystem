<template>
  <div id="app-root">
    <header class="app-header">
      <div class="container">
        <div class="logo">房屋租赁管理系统</div>
        <nav class="main-nav">
          <router-link to="/houses">房源管理</router-link>
          <router-link to="/tenants">租客管理</router-link>
          <router-link to="/contracts">合同管理</router-link>
          <router-link to="/rent-payments">租金管理</router-link>
        </nav>
        <div class="auth-actions">
          <router-link v-if="!isAuthenticated" to="/login">登录</router-link>
          <button v-else class="btn-logout" @click="logout">登出</button>
        </div>
      </div>
    </header>

    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const isAuthenticated = ref(false)
const router = useRouter()

function checkAuth() {
  const token = localStorage.getItem('access-token')
  isAuthenticated.value = !!token
}

function logout() {
  localStorage.removeItem('access-token')
  checkAuth()
  // navigate to login
  router.push('/login')
}

onMounted(() => {
  checkAuth()
  window.addEventListener('storage', checkAuth)
})

onUnmounted(() => {
  window.removeEventListener('storage', checkAuth)
})
</script>

<style scoped>
.app-header { background:#2b9bd3; color:white; padding:12px 0 }
.app-header .container { display:flex; align-items:center; gap:16px }
.logo { font-weight:700 }
.main-nav { display:flex; gap:12px; margin-left:8px }
.main-nav a { color:rgba(255,255,255,0.95); text-decoration:none }
.auth-actions { margin-left:auto }
.btn-logout { background:transparent; border:1px solid rgba(255,255,255,0.2); color:white; padding:6px 10px; border-radius:6px }
.app-main { padding:16px }
</style>
