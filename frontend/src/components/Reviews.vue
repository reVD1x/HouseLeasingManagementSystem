<template>
  <div class="module-page"> <!-- 模块页面容器 -->
    <div class="module-header"> <!-- 页面头部 -->
      <div class="header-content"> <!-- 头部内容 -->
        <div class="title"><span class="emoji">⭐</span><h2>评价管理</h2></div> <!-- 标题部分，带有星星图标 -->
        <router-link class="btn-add" to="/reviews/add"><span class="icon">+</span><span>新增评价</span></router-link> <!-- 跳转到新增评价页面的按钮 -->
      </div>
    </div>
    <div class="module-content"> <!-- 模块主要内容区域 -->
      <div class="table-container"> <!-- 表格容器 -->
        <table class="data-table"> <!-- 数据表格 -->
          <thead><tr><th>作者</th><th>评价序号</th><th>合同</th><th>评分</th><th>评论</th><th>操作</th></tr></thead> <!-- 表头 -->
          <tbody>
            <!-- 遍历评价列表，每条数据生成一行 -->
            <tr v-for="r in items" :key="r.id">
              <!-- 优先显示 reviewerName，其次 authorName，再其次显示 reviewer.realName 或 reviewer.username -->
              <td>{{ r.reviewerName || r.authorName || (r.reviewer && (r.reviewer.realName || r.reviewer.username)) || '' }}</td>
              <td>{{ r.id }}</td> <!-- 评价ID -->
              <td>{{ r.contractNo }}</td> <!-- 合同编号 -->
              <td>{{ r.rating }}</td> <!-- 评分 -->
              <td>{{ r.comment }}</td> <!-- 评论内容 -->
              <td><button @click="del(r.id)">删除</button></td> <!-- 删除按钮 -->
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue' // 引入响应式与生命周期钩子
import api from '@/api' // 引入API模块

const items = ref([]) // 存放评价列表的数据

// 加载评价列表
async function load(){ 
  try{ 
    const res = await api.ReviewsAPI.list({page:0,size:20}) // 调用后端接口获取评价数据
    items.value = res.content || res || [] // 兼容不同返回格式
  }catch(e){ 
    console.error(e) // 控制台打印错误
    items.value=[] // 出错时清空数据
  }
}

// 删除评价
async function del(id){ 
  if(!confirm('删除?'))return; // 弹出确认框
  try{ 
    await api.ReviewsAPI.delete(id) // 调用删除接口
    await load() // 删除后重新加载列表
  }catch(e){ 
    alert('删除失败') // 删除失败提示
  } 
}

onMounted(load) // 组件挂载时加载数据
</script>

<style scoped>
.module-page{padding:12px; width:100%; max-width:100%; box-sizing:border-box} /* 页面整体样式 */
.header-content{display:flex;align-items:center;gap:12px} /* 头部内容布局 */
.btn-add{margin-left:auto} /* 添加按钮靠右 */
.table-container{width:100%; overflow-x:auto} /* 表格支持横向滚动 */
.data-table{width:100%; border-collapse:collapse} /* 表格样式设置 */
.data-table th,.data-table td{border:1px solid #eee;padding:8px;text-align:left} /* 单元格边框与内边距 */
.loading{color:#666} /* 加载状态颜色 */
.error{color:#c0392b;margin-top:8px} /* 错误信息样式 */
</style>
