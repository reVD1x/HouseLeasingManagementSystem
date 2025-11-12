<template>
  <div id="app-root"> <!-- 整个应用的根容器 -->
    <header class="app-header"> <!-- 页面头部区域 -->
      <div class="app-container"> <!-- 包裹头部内容的容器 -->
        <div class="logo">房屋租赁管理系统</div> <!-- 系统标题 -->
        <nav class="main-nav"> <!-- 主导航栏 -->
          <router-link to="/houses">房源管理</router-link> <!-- 跳转到房源管理页面 -->
          <router-link to="/tenants">租客管理</router-link> <!-- 跳转到租客管理页面 -->
          <router-link to="/contracts">合同管理</router-link> <!-- 跳转到合同管理页面 -->
          <router-link to="/rent-payments">租金管理</router-link> <!-- 跳转到租金管理页面 -->
          <router-link to="/maintenances">维修管理</router-link> <!-- 跳转到维修管理页面 -->
          <router-link to="/reviews">评价管理</router-link> <!-- 跳转到评价管理页面 -->
          <router-link to="/stats">统计分析</router-link> <!-- 跳转到统计分析页面 -->
        </nav>
        <div class="auth-actions"> <!-- 登录/登出操作区 -->
          <router-link v-if="!isAuthenticated" to="/login">登录</router-link> <!-- 未登录时显示登录按钮 -->
          <button v-else class="btn-logout" @click="logout">登出</button> <!-- 已登录时显示登出按钮 -->
        </div>
      </div>
    </header>

    <main class="app-main"> <!-- 主体内容区域 -->
      <router-view /> <!-- 用于渲染对应路由页面的组件 -->
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue' // 导入 Vue 的响应式变量和生命周期钩子
import { useRouter } from 'vue-router' // 导入路由钩子

const isAuthenticated = ref(false) // 是否已登录状态
const router = useRouter() // 路由实例

function checkAuth() { // 检查登录状态函数
  const token = localStorage.getItem('access-token') // 从本地存储获取 token
  isAuthenticated.value = !!token // 根据 token 是否存在更新登录状态
}

function logout() { // 登出函数
  localStorage.removeItem('access-token') // 移除本地 token
  checkAuth() // 重新检查登录状态
  // 通知其他组件（同一个标签页内）登录状态已改变
  try { window.dispatchEvent(new Event('auth-changed')) } catch(e) { /* 忽略错误 */ }
  // 跳转到登录页
  router.push('/login')
}

onMounted(() => { // 组件挂载时执行
  checkAuth() // 初始化时检查是否已登录
  window.addEventListener('storage', checkAuth) // 监听本地存储变化（跨标签页登录/登出同步）
  // 监听同标签页触发的登录状态事件
  window.addEventListener('auth-changed', checkAuth)
})

onUnmounted(() => { // 组件卸载时执行
  window.removeEventListener('storage', checkAuth) // 移除监听
  window.removeEventListener('auth-changed', checkAuth)
})
</script>

<style scoped>
.app-header { background:#2b9bd3; color:white; padding:12px 0; width:100%; } /* 头部背景样式 */
.app-header .app-container { display:flex; align-items:center; gap:16px; width:100%; max-width:none; padding:0 1rem; box-sizing:border-box } /* 头部内容容器布局 */
.logo { font-weight:700; flex:0 0 auto } /* 系统标题样式 */
.main-nav { display:flex; gap:12px; flex:1 1 auto; justify-content:center; align-items:center; flex-wrap:wrap; overflow-x:auto; -webkit-overflow-scrolling:touch } /* 导航栏布局 */
.main-nav a { color:rgba(255,255,255,0.95); text-decoration:none; padding:6px 10px; border-radius:6px; white-space:nowrap } /* 导航链接样式 */
@media (hover: hover) {
  .main-nav a:hover { background: rgba(255,255,255,0.12) } /* 鼠标悬停时导航背景变亮 */
}
.auth-actions { margin-left:auto; flex:0 0 auto } /* 登录/登出按钮容器样式 */
.btn-logout { background:transparent; border:1px solid rgba(255,255,255,0.2); color:white; padding:6px 10px; border-radius:6px } /* 登出按钮样式 */
.app-main { padding:16px } /* 主体内容内边距 */
</style>
