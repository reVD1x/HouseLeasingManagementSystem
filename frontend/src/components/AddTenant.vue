<template>
  <div class="col-md-8 offset-md-2">
    <h3>新增租客</h3>
    <form @submit.prevent="submit"> <!-- 阻止默认提交行为，改为调用 submit 方法 -->
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
        <input v-model="form.phone" class="form-control" /> <!-- 联系电话 -->
      </div>
      <div class="mb-3">
      </div>
      <div class="text-end"> <!-- 表单按钮区，右对齐 -->
        <router-link to="/tenants" class="btn btn-secondary">取消</router-link> <!-- 返回租客列表 -->
        <button class="btn btn-success ms-2" type="submit">保存</button> <!-- 提交表单 -->
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue' // 引入 ref，用于定义响应式数据
import { post } from '@/utils/api' // 引入封装的 post 请求方法
import { useRouter } from 'vue-router' // 引入 vue-router，用于页面跳转

const router = useRouter() // 获取路由实例
const form = ref({ realName: '', idCard: '', phone: '' }) // 定义租客信息表单的响应式对象

// 提交表单方法
async function submit() {
  try {
    const res = await post('/users', form.value) // 发送 POST 请求，将表单数据提交到后端
    if (res && res.id) router.push('/tenants') // 如果返回有 id，表示创建成功，跳转到租客列表页
    else alert('保存可能失败：' + JSON.stringify(res)) // 否则提示保存异常
  } catch (e) {
    console.error('保存租客失败', e) // 控制台输出错误信息
    alert('保存失败：' + (e.message || '请检查控制台')) // 弹出错误提示
  }
}
</script>
