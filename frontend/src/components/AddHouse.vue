<template>
  <div class="col-md-8 offset-md-2">
    <h3>新增房源</h3>
    <form @submit.prevent="submit">  <!-- 表单提交时调用 submit 方法并阻止默认提交行为 -->
      <div class="mb-3">
        <label class="form-label">房屋地址</label>
        <input v-model="form.address" class="form-control" required />  <!-- 双向绑定房屋地址 -->
      </div>
      <div class="row">
        <div class="col-md-4 mb-3">
          <label class="form-label">户型</label>
          <input v-model="form.houseType" class="form-control" />  <!-- 输入房屋户型 -->
        </div>
        <div class="col-md-4 mb-3">
          <label class="form-label">面积(㎡)</label>
          <input type="number" v-model.number="form.area" class="form-control" />  <!-- 输入房屋面积 -->
        </div>
        <div class="col-md-4 mb-3">
          <label class="form-label">租金(元/月)</label>
          <input type="number" v-model.number="form.rent" class="form-control" />  <!-- 输入每月租金 -->
        </div>
      </div>
      <div class="mb-3">
        <label class="form-label">装修情况</label>
        <input v-model="form.decoration" class="form-control" />  <!-- 输入装修情况 -->
      </div>
      <div class="mb-3">
        <label class="form-label">配套设施</label>
        <input v-model="form.facilities" class="form-control" />  <!-- 输入配套设施，如空调、热水器等 -->
      </div>
      <div class="mb-3">
        <label class="form-label">房源状态 *</label>
        <select v-model="form.status" class="form-select" required>  <!-- 选择房源状态 -->
          <option value="">请选择</option>
          <option value="AVAILABLE">可租</option>
          <option value="RENTED">已租</option>
        </select>
      </div>
      <div class="mb-3">
        <label class="form-label">房东（选择已有用户） *</label>
        <select v-model.number="form.landlordId" class="form-select" required>  <!-- 选择房东 -->
          <option value="">-- 请选择房东 --</option>
          <option v-for="u in landlords" :key="u.id" :value="u.id">{{ u.realName }} (id: {{ u.id }})</option>  <!-- 遍历房东列表 -->
        </select>
      </div>
      <div class="text-end">
        <router-link to="/houses" class="btn btn-secondary">取消</router-link>  <!-- 返回房源列表页 -->
        <button class="btn btn-success ms-2" type="submit">保存</button>  <!-- 提交表单 -->
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'  // 从 Vue 导入组合式 API
import { post, get } from '@/utils/api'  // 导入封装的请求方法
import { useRouter } from 'vue-router'  // 导入 Vue Router 用于跳转页面

const router = useRouter()  // 获取路由实例

// 定义表单数据对象，包含房源的基本信息
const form = ref({ 
  address: '',         // 房屋地址
  houseType: '',       // 户型
  area: null,          // 面积
  rent: null,          // 租金
  decoration: '',      // 装修情况
  facilities: '',      // 配套设施
  status: 'AVAILABLE', // 默认状态为“可租”
  landlordId: null     // 房东ID
})

const landlords = ref([])  // 房东列表数据

onMounted(async () => {  // 组件挂载后执行
  try {
    const res = await get('/users?page=0&size=200')  // 从后端获取用户数据（作为房东）
    landlords.value = res?.content || []  // 如果返回为空则使用空数组
  } catch (e) {
    console.error('加载用户列表失败', e)  // 加载失败时打印错误
  }
})

async function submit() {  // 提交表单方法
  try {
    // 校验房东ID是否有效（必须为正整数）
    const landlordId = Number(form.value.landlordId)
    if (!Number.isFinite(landlordId) || landlordId <= 0) return alert('请输入有效的房东ID（数字）')

    // 组装提交数据（payload）
    const payload = { ...form.value, landlordId: landlordId }

    // 向后端提交新增房源请求
    const res = await post('/houses', payload)

    // 根据返回结果判断是否成功
    if (res && res.id) {
      router.push('/houses')  // 新增成功后跳转到房源列表页
    } else {
      alert('保存可能失败：' + JSON.stringify(res))  // 提示保存异常
    }
  } catch (e) {
    console.error('保存房源失败', e)  // 打印错误到控制台
    alert('保存失败：' + (e.message || '请检查控制台'))  // 弹出错误提示
  }
}
</script>
