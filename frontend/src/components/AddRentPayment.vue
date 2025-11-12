<template>
  <div class="col-md-8 offset-md-2">
    <h3>新增租金记录</h3>
    <form @submit.prevent="submit">  <!-- 阻止默认提交事件，调用submit方法 -->
      <div class="mb-3">
        <label class="form-label">合同ID</label>
        <input v-model.number="form.contractId" class="form-control" required />  <!-- 双向绑定合同ID -->
      </div>
      <div class="mb-3">
        <label class="form-label">应付日期</label>
        <input type="date" v-model="form.dueDate" class="form-control" required />  <!-- 双向绑定应付日期 -->
      </div>
      <div class="mb-3">
        <label class="form-label">状态</label>
        <select v-model="form.status" class="form-select">  <!-- 选择租金状态 -->
          <option value="PENDING">待付</option>
          <option value="PAID">已付</option>
          <option value="OVERDUE">已逾期</option>
        </select>
      </div>
      <div class="row">
        <div class="col-md-6 mb-3">
          <label class="form-label">计费周期开始（可选）</label>
          <input type="date" v-model="form.periodStart" class="form-control" />  <!-- 计费周期起始日期 -->
        </div>
        <div class="col-md-6 mb-3">
          <label class="form-label">计费周期结束（可选）</label>
          <input type="date" v-model="form.periodEnd" class="form-control" />  <!-- 计费周期结束日期 -->
        </div>
      </div>
      <div class="mb-3">
        <label class="form-label">逾期罚金（可选）</label>
        <input type="number" v-model.number="form.penalty" class="form-control" />  <!-- 输入逾期罚金 -->
      </div>
      <div class="mb-3">
        <label class="form-label">金额</label>
        <input type="number" v-model.number="form.amount" class="form-control" required />  <!-- 输入租金金额 -->
      </div>
      <div class="text-end">
        <router-link to="/rent-payments" class="btn btn-secondary">取消</router-link>  <!-- 返回租金记录页面 -->
        <button class="btn btn-success ms-2" type="submit">保存</button>  <!-- 提交按钮 -->
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'  // 引入 ref 用于响应式数据
import { RentPaymentsAPI } from '../api'  // 引入租金支付相关 API
import { useRouter } from 'vue-router'  // 引入 Vue Router

const router = useRouter()  // 获取路由实例
// 定义表单数据对象并设置初始值
const form = ref({ 
  contractId: null, 
  dueDate: '', 
  amount: null, 
  status: 'PENDING', 
  periodStart: '', 
  periodEnd: '', 
  penalty: null 
})

async function submit() {  // 提交表单的异步函数
  try {
    const contractId = Number(form.value.contractId)  // 将合同ID转为数字类型
    if (!Number.isFinite(contractId) || contractId <= 0) return alert('请输入有效的合同ID（数字）')  // 验证合同ID

    const d = new Date(form.value.dueDate)  // 解析应付日期
    if (Number.isNaN(d.getTime())) return alert('请输入有效的应付日期')  // 验证日期格式

    // 构建要发送给后端的请求数据
    const payload = {
      contractId: contractId,  // 合同ID
      dueDate: d.toISOString().slice(0,10),  // 格式化为 yyyy-MM-dd
      amount: Number(form.value.amount),  // 金额
      status: form.value.status,  // 状态
      periodStart: form.value.periodStart ? new Date(form.value.periodStart).toISOString().slice(0,10) : undefined,  // 可选：周期开始
      periodEnd: form.value.periodEnd ? new Date(form.value.periodEnd).toISOString().slice(0,10) : undefined,  // 可选：周期结束
      penalty: form.value.penalty ? Number(form.value.penalty) : undefined  // 可选：罚金
    }

    const res = await RentPaymentsAPI.create(payload)  // 调用后端接口创建租金记录
    if (res && (res.id || res.success)) router.push('/rent-payments')  // 保存成功则跳转
    else alert('保存可能失败：' + JSON.stringify(res))  // 否则提示失败
  } catch (e) {
    console.error('保存租金失败', e)  // 控制台打印错误
    alert('保存失败：' + (e.message || '请检查控制台'))  // 弹出提示信息
  }
}
</script>

<style scoped>
.col-md-8 { padding: 1rem }  /* 为表单容器添加内边距 */
</style>
