<template>
  <div id="app">
  <header class="site-header">
    <div class="container d-flex justify-content-between align-items-center">
      <h1 class="site-title">房屋租赁管理系统</h1>
      <nav class="site-nav">
        <router-link to="/houses" class="nav-link">房源管理</router-link>
        <router-link to="/tenants" class="nav-link">租客管理</router-link>
        <router-link to="/contracts" class="nav-link">合同管理</router-link>
        <router-link to="/rent-payments" class="nav-link">租金管理</router-link>
        <div class="auth-buttons">
          <router-link v-if="!loggedIn" to="/login" class="btn-login">登录</router-link>
          <button v-else class="btn-logout" @click="logout">登出</button>
        </div>
      </nav>
    </div>
  </header>

    <main class="container mt-4 site-main">
      <div class="content-card">
        <router-view></router-view>
      </div>
    </main>

    <footer class="footer mt-5 py-3 bg-light">
      <div class="container text-center">
        <small class="text-muted">&copy; 2025 房屋租赁管理系统</small>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loggedIn = ref(!!localStorage.getItem('access-token'))

function logout() {
  localStorage.removeItem('access-token')
  loggedIn.value = false
  // 跳回首页或登录页（项目中可能没有 /login 路由）
  router.push('/')
}
import { onMounted, onBeforeUnmount } from 'vue'

function onAuthChanged() {
  loggedIn.value = !!localStorage.getItem('access-token')
}

onMounted(() => {
  window.addEventListener('auth-changed', onAuthChanged)
})

onBeforeUnmount(() => {
  window.removeEventListener('auth-changed', onAuthChanged)
})
</script>

<style scoped>
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.site-header {
  background: #3498db;
  padding: 1rem 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.site-title {
  color: white;
  margin: 0;
  font-size: 1.5rem;
  font-weight: 500;
}

.site-nav {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.nav-link {
  color: white !important;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.nav-link:hover {
  background-color: rgba(255,255,255,0.1);
}

.nav-link.router-link-active {
  background-color: rgba(255,255,255,0.2);
  font-weight: 500;
}

.auth-buttons {
  margin-left: 1rem;
}

.btn-login, .btn-logout {
  background: white;
  color: #3498db;
  border: none;
  padding: 0.375rem 1rem;
  border-radius: 4px;
  font-size: 0.875rem;
  cursor: pointer;
  text-decoration: none;
  transition: background-color 0.2s;
}

.btn-login:hover, .btn-logout:hover {
  background: #f8f9fa;
}

.site-main {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 2rem 0;
}

.content-card {
  width: 100%;
  max-width: 1200px;
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.footer {
  background: #f8f9fa;
  padding: 1.5rem 0;
  margin-top: auto;
  border-top: 1px solid #dee2e6;
}
</style>