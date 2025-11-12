<template>
  <div class="col-md-8 offset-md-2">
    <h3>编辑租客</h3>
    <!-- 如果数据加载完成，显示表单 -->
    <form v-if="loaded" @submit.prevent="submit">
      <div class="mb-3">
        <label class="form-label">姓名</label>
        <input v-model="form.realName" class="form-control" required /> <!-- 租客姓名 -->
      </div>
      <div class="mb-3">
        <label class="form-label">身份证号</label>
        <input v-model="form.idCard" class="form-control" required /> <!-- 身份证号 -->
      </div>
      <div class="mb-3">
        <label class="form-label">电话</label>
        <input v-model="form.phone" class="form-control" /> <!-- 电话，可选 -->
      </div>

      <div class="text-end">
        <router-link to="/tenants" class="btn btn-secondary">取消</router-link> <!-- 返回租客列表 -->
        <button class="btn btn-success ms-2" type="submit">保存修改</button> <!-- 提交修改 -->
      </div>
    </form>

    <!-- 数据加载中提示 -->
    <div v-else class="text-center">载入中...</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get, put } from '@/utils/api'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const id = route.params.id // 路由参数：租客ID

// 表单数据初始化
const form = ref({ id: null, realName: '', idCard: '', phone: '' })
const loaded = ref(false) // 数据是否加载完成

// 加载租客数据
async function load() {
  try {
    const res = await get(`/users/${id}`) // 调用后端获取租客信息
    form.value = { 
      realName: res.realName, 
      idCard: res.idCard, 
      phone: res.phone 
    }
    loaded.value = true
  } catch (e) {
    console.error('加载租客失败', e)
    alert('加载失败')
  }
}

// 提交修改
async function submit() {
  try {
    await put(`/users/${id}`, form.value) // 调用后端更新租客信息
    router.push('/tenants') // 返回租客列表
  } catch (e) {
    console.error('保存失败', e)
    alert('保存失败：' + (e.message || '请检查控制台'))
  }
}

// 页面加载时调用
onMounted(load)
</script>
