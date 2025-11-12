import { createRouter, createWebHashHistory } from 'vue-router' // 从 vue-router 导入创建路由和哈希模式历史记录的函数

// 定义前端路由表
const routes = [
  { path: '/', redirect: '/houses' }, // 默认路径重定向到 /houses（房源管理页面）

  // 房源管理模块
  { path: '/houses', component: () => import('./components/Houses.vue') }, // 房源列表页
  { path: '/houses/add', component: () => import('./components/AddHouse.vue') }, // 新增房源页
  { path: '/houses/edit/:id', component: () => import('./components/EditHouse.vue') }, // 编辑房源页（带动态参数 id）

  // 租客管理模块
  { path: '/tenants', component: () => import('./components/Tenants.vue') }, // 租客列表页
  { path: '/tenants/add', component: () => import('./components/AddTenant.vue') }, // 新增租客页
  { path: '/tenants/edit/:id', component: () => import('./components/EditTenant.vue') }, // 编辑租客页

  // 合同管理模块
  { path: '/contracts', component: () => import('./components/Contracts.vue') }, // 合同列表页
  { path: '/contracts/add', component: () => import('./components/AddContract.vue') }, // 新增合同页
  { path: '/contracts/edit/:id', component: () => import('./components/EditContract.vue') }, // 编辑合同页

  // 租金缴纳模块
  { path: '/rent-payments', component: () => import('./components/RentPayments.vue') }, // 租金记录页
  { path: '/rent-payments/add', component: () => import('./components/AddRentPayment.vue') }, // 新增租金记录页
  { path: '/rent-payments/edit/:id', component: () => import('./components/EditRentPayment.vue') }, // 编辑租金记录页

  // 维修管理模块
  { path: '/maintenances', component: () => import('./components/Maintenances.vue') }, // 维修列表页
  { path: '/maintenances/add', component: () => import('./components/AddMaintenance.vue') }, // 新增维修记录页

  // 评论管理模块
  { path: '/reviews', component: () => import('./components/Reviews.vue') }, // 评论列表页
  { path: '/reviews/add', component: () => import('./components/AddReview.vue') }, // 新增评论页

  // 统计分析模块
  { path: '/stats', component: () => import('./components/Stats.vue') }, // 数据统计页

  // 管理员页面
  { path: '/admin', component: () => import('./components/Admin.vue') }, // 管理员中心页

  // 登录页面
  { path: '/login', component: () => import('./components/Login.vue') }, // 用户登录页
]

// 创建路由实例，使用哈希模式（URL 中带 #）
const router = createRouter({ history: createWebHashHistory(), routes })

export default router // 导出路由实例供 main.js 引入使用
