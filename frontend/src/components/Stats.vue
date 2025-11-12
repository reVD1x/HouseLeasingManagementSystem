<template>
  <div class="stats-page"> <!-- 统计分析页面容器 -->
    <h2>统计分析</h2>

    <div v-if="loading" class="loading">正在加载统计数据…</div> <!-- 加载状态提示 -->
    <div v-else> <!-- 数据加载完成后显示内容 -->
      <div class="cards"> <!-- 顶部汇总卡片区域 -->
        <div class="card">
          <div class="card-title">房源总数</div>
          <div class="card-value">{{ summary.totalHouses }}</div> <!-- 显示房源总数 -->
        </div>
        <div class="card">
          <div class="card-title">已出租房源</div>
          <div class="card-value">{{ summary.rentedHouses }}</div> <!-- 显示已出租数量 -->
        </div>
        <div class="card">
          <div class="card-title">活跃租客</div>
          <div class="card-value">{{ summary.activeTenants }}</div> <!-- 显示活跃租客数量 -->
        </div>
        <div class="card">
          <div class="card-title">本月租金收入</div>
          <div class="card-value">¥{{ summary.monthlyIncome }}</div> <!-- 显示本月租金收入 -->
        </div>
      </div>

      <section class="chart-section"> <!-- 租金收入柱状图区域 -->
        <h3>近6个月租金收入</h3>
        <div class="bar-chart">
          <div
            v-for="(point, idx) in summary.monthlyIncomeSeries"
            :key="idx"
            class="bar-wrap"
          >
            <!-- 显示每个月的租金收入和百分比 -->
            <div class="bar-value">¥{{ point.value }}<div class="bar-percent">{{ point.percent }}%</div></div>
            <div class="bar-track">
              <!-- 柱状条高度由 barHeight 动态计算 -->
              <div class="bar" :style="{ height: barHeight(point.value) + '%' }" :title="point.label + ': ¥' + point.value"></div>
            </div>
            <div class="bar-label">{{ point.label }}</div> <!-- 月份标签 -->
          </div>
        </div>
      </section>

      <section class="list-section"> <!-- 待处理维修请求列表 -->
        <h3>待处理维修请求</h3>
        <ul>
          <li v-for="m in summary.pendingMaintenances" :key="m.id">
            {{ m.date }} - {{ m.houseAddress }} - {{ m.issue }}
          </li>
          <li v-if="!summary.pendingMaintenances || summary.pendingMaintenances.length===0">无待处理维修请求</li>
        </ul>
      </section>

      <section class="list-section"> <!-- 最新评论列表 -->
        <h3>最新评论（最近5条）</h3>
        <ul>
          <li v-for="r in summary.recentReviews" :key="r.id">
            <strong>{{ r.author }}:</strong> {{ r.content }} <span class="muted">({{ r.date }})</span>
          </li>
          <li v-if="!summary.recentReviews || summary.recentReviews.length===0">暂无评论</li>
        </ul>
      </section>
    </div>

    <div v-if="error" class="error">{{ error }}</div> <!-- 错误信息显示 -->
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue' // 引入 Vue 的响应式和生命周期函数
import api from '@/api' // 引入封装的 API 模块

const loading = ref(true) // 加载状态
const error = ref('') // 错误提示信息

// 定义统计数据对象及初始值
const summary = ref({
  totalHouses: 0,
  rentedHouses: 0,
  activeTenants: 0,
  monthlyIncome: 0,
  monthlyIncomeSeries: [],
  pendingMaintenances: [],
  recentReviews: [],
})

// 当无法获取后端数据时，生成演示数据
function fillDemoData() {
  // 提供友好的备用演示数据
  const now = new Date()
  const months = Array.from({ length: 6 }).map((_, i) => {
    const d = new Date(now.getFullYear(), now.getMonth() - (5 - i), 1)
    return {
      label: `${d.getMonth() + 1}月`,
      value: Math.round(2000 + Math.random() * 8000),
    }
  })
  summary.value = {
    totalHouses: 42,
    rentedHouses: 34,
    activeTenants: 58,
    monthlyIncome: months[months.length - 1].value,
    monthlyIncomeSeries: months,
    pendingMaintenances: [
      { id: 1, date: '2025-11-09', houseAddress: '紫金小区 3-201', issue: '水管漏水' },
    ],
    recentReviews: [
      { id: 1, author: '租客A', content: '房东很负责', date: '2025-11-08' },
      { id: 2, author: '租客B', content: '维修速度慢', date: '2025-11-05' },
    ],
  }
}

// 计算柱状图高度（百分比）
function barHeight(value) {
  const series = summary.value.monthlyIncomeSeries || []
  if (series.length === 0) return 10 // 若无数据，返回默认高度
  const vals = series.map((p) => Number(p.value) || 0)
  const max = Math.max(...vals, 1)
  const v = Number(value) || 0
  const percent = Math.round((v / max) * 100)
  return Math.max(4, percent) // 确保最低显示高度
}

// 从后端加载统计数据
async function loadSummary() {
  loading.value = true
  error.value = ''
  try {
    // 使用统一封装的 API 方法
    const res = await api.StatsAPI.overview()
    if (res && typeof res === 'object') {
      // 解析后端返回的数据，兼容不同字段命名方式
      const rawSeries = (res.monthlyIncomeSeries ?? res.monthly_income_series ?? res.incomeSeries ?? [])
      const series = Array.isArray(rawSeries) ? rawSeries.map((p, idx) => {
        let label = null
        let rawVal = null
        if (p == null) return { label: '', value: 0 }
        if (Array.isArray(p)) { label = p[0]; rawVal = p[1] }
        else if (typeof p === 'object') { label = p.label ?? p.month ?? p[0] ?? (`${idx + 1}月`); rawVal = p.value ?? p.amount ?? p[1] ?? 0 }
        else { label = String(p); rawVal = 0 }
        const value = Number(rawVal) || 0
        return { label: String(label ?? `${idx + 1}月`), value }
      }) : []

      // 为每个月的数据计算百分比
      const valsForPercent = series.map(s => s.value || 0)
      const maxVal = Math.max(...valsForPercent, 1)
      const seriesWithPercent = series.map(s => ({ ...s, percent: Math.max(4, Math.round((Number(s.value || 0) / maxVal) * 100)) }))

      console.debug('Stats overview series:', series)

      // 统一赋值给 summary 对象
      summary.value = {
        totalHouses: Number(res.totalHouses ?? res.total_houses ?? res.total ?? 0),
        rentedHouses: Number(res.rentedHouses ?? res.rented_houses ?? res.rented ?? 0),
        activeTenants: Number(res.activeTenants ?? res.active_tenants ?? res.tenants ?? 0),
        monthlyIncome: Number(res.monthlyIncome ?? res.monthly_income ?? res.monthly ?? 0),
        monthlyIncomeSeries: seriesWithPercent,
        pendingMaintenances: res.pendingMaintenances ?? res.pending_maintenances ?? res.pending ?? [],
        recentReviews: res.recentReviews ?? res.recent_reviews ?? res.reviews ?? [],
      }
    } else {
      // 数据结构异常则使用演示数据
      fillDemoData()
    }
  } catch (e) {
    // 获取失败时提示用户，并填充演示数据
    error.value = '无法获取后台统计数据，已显示示例数据。'
    fillDemoData()
  } finally {
    loading.value = false
  }
}

// 页面挂载后立即加载统计数据
onMounted(() => {
  loadSummary()
})
</script>

<style scoped>
.stats-page { padding: 12px } /* 页面整体内边距 */
.loading { color: #666 } /* 加载文字样式 */
.error { margin-top:12px; color: #c0392c } /* 错误提示样式 */
.cards { display:flex; gap:12px; flex-wrap:wrap } /* 卡片区域布局 */
.card { background: #fff; border-radius:8px; padding:12px; min-width:160px; box-shadow: 0 1px 3px rgba(0,0,0,0.08) } /* 卡片样式 */
.card-title { font-size: 13px; color:#666 } /* 卡片标题样式 */
.card-value { font-size: 20px; font-weight:700; margin-top:8px } /* 卡片数值样式 */
.chart-section { margin-top:18px } /* 图表区外边距 */
.bar-chart { display:flex; gap:12px; align-items:end; height:220px; padding:8px; background:#fafafa; border-radius:8px } /* 柱状图整体样式 */
.bar-wrap { display:flex; flex-direction:column; align-items:center; width:64px; } /* 单个柱子容器 */
.bar-value{ font-size:12px; color:#1f6fa8; margin-bottom:6px } /* 柱状值样式 */
.bar-percent { font-size: 10px; color: #999; margin-top: 2px; } /* 百分比文字样式 */
.bar-track{ width:100%; height:120px; display:flex; align-items:end; justify-content:center } /* 柱子轨道区域 */
.bar { width:80%; background:linear-gradient(180deg,#2b9bd3,#1f6fa8); border-radius:6px 6px 0 0; min-height:4px; transition:height .25s ease; display:block } /* 柱条样式 */
.bar-label { margin-top:6px; font-size:12px; color:#444 } /* 柱子底部标签 */
.list-section { margin-top:14px } /* 列表区域外边距 */
.muted { color:#888; font-size:12px; margin-left:6px } /* 评论日期灰色文字 */
</style>
