<script setup>
import { ref, onMounted } from 'vue'


const isAuthenticated = ref(false)

// 检查是否已登录
const checkAuth = () => {
  const token = localStorage.getItem('access-token')
  isAuthenticated.value = !!token
}

onMounted(() => {
  checkAuth()
  // 监听登录成功事件
  window.addEventListener('storage', checkAuth)
})

// 监听登录成功
const handleLoginSuccess = () => {
  checkAuth()
}
</script>

<style scoped>
header {
  line-height: 1.5;
}

.logo {
  display: block;
  margin: 0 auto 2rem;
}

@media (min-width: 1024px) {
  header {
    display: flex;
    place-items: center;
    padding-right: calc(var(--section-gap) / 2);
  }

  .logo {
    margin: 0 2rem 0 0;
  }

  header .wrapper {
    display: flex;
    place-items: flex-start;
    flex-wrap: wrap;
  }
}
</style>
