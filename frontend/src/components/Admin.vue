<template>
  <div class="admin-page">
    <h2>系统管理</h2>
    <div class="admin-actions">
      <button @click="backup">立即备份</button> <!-- 点击触发数据库或系统备份操作 -->
      <button @click="restore">恢复（上传）</button> <!-- 点击执行恢复操作（演示用） -->
    </div>

    <section class="roles">
      <h3>用户权限</h3>
      <div v-if="loadingUsers">加载用户中…</div> <!-- 数据加载时显示提示 -->
      <table v-else class="data-table"> <!-- 加载完成后显示用户表格 -->
        <thead>
          <tr>
            <th>ID</th>
            <th>姓名</th>
            <th>角色</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <!-- 遍历用户列表，显示每个用户信息 -->
          <tr v-for="u in users" :key="u.id">
            <td>{{u.id}}</td> <!-- 用户ID -->
            <td>{{u.realName||u.username}}</td> <!-- 姓名（优先显示真实姓名） -->
            <td>{{u.role||'USER'}}</td> <!-- 角色，默认显示为 USER -->
            <td><button @click="setRole(u.id)">设置角色</button></td> <!-- 点击修改角色 -->
          </tr>
        </tbody>
      </table>
    </section>

    <div v-if="error" class="error">{{ error }}</div> <!-- 错误提示信息 -->
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue' // 引入 Vue 组合式API：ref（响应式变量）、onMounted（生命周期钩子）
import api from '@/api' // 引入封装的后端API接口对象

const users = ref([]) // 存放用户数据的响应式数组
const loadingUsers = ref(false) // 控制加载状态
const error = ref('') // 存储错误提示信息

// 加载用户列表
async function loadUsers(){
  loadingUsers.value = true // 设置加载状态
  error.value = '' // 清空错误
  try {
    // 调用后端用户列表API，分页加载
    const res = await api.UsersAPI.list({ page: 0, size: 100 })
    users.value = res?.content ?? res ?? [] // 兼容不同API返回格式
  } catch (e) {
    error.value = '无法加载用户' // 加载失败提示
    users.value = [] // 清空数据
  } finally {
    loadingUsers.value = false // 结束加载状态
  }
}

// 设置用户角色
async function setRole(userId){
  const role = prompt('输入角色 (ADMIN / MANAGER / USER)') // 弹出输入框获取新角色
  if(!role) return // 如果未输入角色则取消操作
  try {
    await api.AdminAPI.setRole(userId, role) // 调用后端接口设置角色
    await loadUsers() // 重新加载用户列表
  } catch (e) {
    alert('设置失败') // 提示操作失败
  }
}

// 执行系统备份
async function backup(){
  try {
    await api.AdminAPI.backup() // 调用后端备份接口
    alert('备份已触发') // 提示备份已启动
  } catch (e) {
    alert('备份失败') // 备份出错提示
  }
}

// 恢复功能（演示说明用）
async function restore(){
  alert('如需上传备份，请使用后台控制台或API调用；本前端仅触发演示') // 演示提示
}

// 页面加载时执行用户数据加载
onMounted(loadUsers)
</script>

<style scoped>
.admin-page { padding: 12px } /* 页面整体内边距 */
.admin-actions { display: flex; gap: 12px; margin-bottom: 12px } /* 顶部操作按钮的布局 */
.data-table { width: 100%; border-collapse: collapse } /* 表格宽度与边框样式 */
.data-table th, .data-table td { border: 1px solid #eee; padding: 8px; text-align: left } /* 表格单元格样式 */
.error { color: #c0392b; margin-top: 8px } /* 错误提示样式（红色文字） */
</style>
