<template>
  <div class="form-page">
    <h2>提交维修申请</h2>
    <form @submit.prevent="submit">  <!-- 阻止表单默认提交，调用 submit 方法 -->
      <div class="row"><label>房源ID/地址</label>
        <div v-if="loadingHouses">加载房源中…</div>  <!-- 加载提示 -->
        <div v-else>
          <select v-model="form.houseId">  <!-- 下拉选择房源 -->
            <option value="">-- 请选择房源 --</option>
            <option v-for="h in houses" :key="h.houseId" :value="h.houseId">{{ h.houseId }} - {{ h.houseAddress }}</option>  <!-- 遍历房源列表 -->
          </select>
        </div>
      </div>
      <div class="row"><label>提交者姓名</label><input v-model="form.requesterName" /></div>  <!-- 输入申请人姓名 -->
      <div class="row"><label>联系电话</label><input v-model="form.contact" /></div>  <!-- 输入联系电话 -->
      <div class="row"><label>问题描述</label><textarea v-model="form.issue"></textarea></div>  <!-- 填写维修问题描述 -->
      <div class="actions">
        <button type="submit" :disabled="submitting">提交</button>  <!-- 提交按钮 -->
        <router-link to="/maintenances">取消</router-link>  <!-- 取消按钮，返回维修记录页 -->
      </div>
    </form>
    <div v-if="error" class="error">{{ error }}</div>  <!-- 错误信息显示区域 -->
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'  // 导入 Vue 组合式 API
import api from '@/api'  // 导入封装的 API 模块
import { useRouter } from 'vue-router'  // 导入 Vue Router

const router = useRouter()  // 获取路由实例
const error = ref('')  // 存放错误提示信息
const submitting = ref(false)  // 控制提交按钮禁用状态
const form = ref({ houseId: '', requesterName: '', contact: '', issue: '' })  // 定义表单数据对象

const houses = ref([])  // 房源列表
const loadingHouses = ref(false)  // 加载房源状态

// 通用数据提取函数：用于兼容不同API返回结构
function normalizeList(res) {
  if (!res) return []
  if (Array.isArray(res)) return res
  if (res.content && Array.isArray(res.content)) return res.content
  if (res.data && Array.isArray(res.data)) return res.data
  return []
}

// 从合同接口中加载房源列表
async function loadHousesFromContracts() {
  loadingHouses.value = true  // 设置加载状态
  try {
    const res = await api.ContractsAPI.search({ page: 0, size: 200 })  // 请求合同接口数据
    const list = normalizeList(res)  // 标准化结果
    // 提取唯一的 houseId / houseAddress 组合
    const map = new Map()
    list.forEach(c => {
      const hid = c.houseId ?? (c.house && c.house.id) ?? c.id ?? null  // 取房屋ID
      const addr = c.houseAddress ?? (c.house && c.house.address) ?? c.address ?? c.houseAddr ?? ''  // 取房屋地址
      if (hid != null && !map.has(String(hid))) map.set(String(hid), { houseId: hid, houseAddress: addr })  // 去重保存
    })

    // 如果合同中没拿到房源，尝试从 HousesAPI 加载
    if (map.size === 0) {
      try {
        const res2 = await api.HousesAPI.search({ page: 0, size: 200 })  // 请求房源接口
        const hlist = normalizeList(res2)
        hlist.forEach(h => {
          const hid = h.houseId ?? h.id ?? (h.house && h.house.id) ?? null  // 取房源ID
          const addr = h.houseAddress ?? h.address ?? (h.house && h.house.address) ?? h.title ?? ''  // 取房源地址
          if (hid != null && !map.has(String(hid))) map.set(String(hid), { houseId: hid, houseAddress: addr })
        })
      } catch (e) {
        console.warn('从 HousesAPI 加载房源失败，继续使用合同结果（或空列表）', e)
      }
    }

    houses.value = Array.from(map.values())  // 最终结果转为数组赋值
  } catch (e) {
    console.error('加载合同/房源失败', e)  // 打印错误日志
    houses.value = []  // 若失败则置空
  } finally {
    loadingHouses.value = false  // 关闭加载状态
  }
}

// 提交维修申请
async function submit() {
  error.value = ''  // 清空错误提示
  // 前端基础校验
  if (!form.value.houseId) { error.value = '请选择房源'; return }
  if (!form.value.issue || !String(form.value.issue).trim()) { error.value = '请填写问题描述'; return }
  if (String(form.value.issue).length > 1000) { error.value = '问题描述不能超过1000字'; return }

  submitting.value = true  // 开始提交，禁用按钮
  try {
    // 后端期望的数据结构（DTO）
    const payload = {
      houseId: Number(form.value.houseId),
      description: form.value.issue,
      status: 'PENDING',  // 默认状态为待处理
      requesterName: form.value.requesterName,
      contact: form.value.contact
    }
    await api.MaintenanceAPI.create(payload)  // 发送创建请求
    router.push('/maintenances')  // 成功后跳转维修列表页
  } catch (e) {
    console.error('提交维修请求失败：', e)  // 控制台打印错误，便于调试
    // 处理错误提示信息
    let msg = '提交失败，请重试'
    try {
      if (e && e.message) {
        // 有时后端返回对象，message 可能是 “[object Object]”
        if (e.message === '[object Object]' && typeof e === 'object') {
          msg = JSON.stringify(e)
        } else {
          msg = e.message
        }
      } else if (typeof e === 'string') {
        msg = e
      }
    } catch (err) {
      msg = '提交失败，请重试'
    }
    error.value = msg  // 显示错误消息
  } finally {
    submitting.value = false  // 无论成功失败都恢复按钮状态
  }
}

onMounted(loadHousesFromContracts)  // 组件加载时自动执行房源加载函数
</script>

<style scoped>
.form-page{padding:12px}  /* 页面整体内边距 */
.row{margin-bottom:8px}  /* 每行间距 */
.row label{display:block;font-size:13px;color:#444;margin-bottom:4px}  /* 标签样式 */
.row input,.row textarea, .row select{width:100%;padding:8px;border:1px solid #ddd;border-radius:4px}  /* 输入框与选择框样式 */
.actions{display:flex;gap:12px;align-items:center;margin-top:12px}  /* 按钮区域布局 */
.error{color:#c0392b;margin-top:8px}  /* 错误提示样式 */
</style>
