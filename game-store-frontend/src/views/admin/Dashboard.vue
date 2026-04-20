<template>
  <div class="admin-page dashboard">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div 
        v-for="(stat, index) in statsCards" 
        :key="stat.key"
        class="stat-card"
        :class="{ 'fade-in-up': true }"
        :style="{ animationDelay: `${index * 0.1}s` }"
        @click="goToDetail(stat)"
      >
        <div class="stat-icon" :style="{ background: stat.gradient }">
          <el-icon size="28"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
        <div class="stat-bg-icon">
          <el-icon><component :is="stat.icon" /></el-icon>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <!-- 销售趋势 -->
      <div class="chart-card sales-chart">
        <div class="card-header">
          <h3>销售趋势</h3>
          <div class="header-actions">
            <el-radio-group v-model="salesPeriod" size="small" @change="updateCharts">
              <el-radio-button label="week">本周</el-radio-button>
              <el-radio-button label="month">本月</el-radio-button>
              <el-radio-button label="year">全年</el-radio-button>
            </el-radio-group>
          </div>
        </div>
        <div ref="salesChartRef" class="chart-container"></div>
      </div>

      <!-- 分类占比 -->
      <div class="chart-card category-chart">
        <div class="card-header">
          <h3>游戏分类占比</h3>
        </div>
        <div ref="categoryChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 最近订单 -->
    <div class="data-card recent-orders">
      <div class="card-header">
        <h3>最近订单</h3>
        <el-button link type="primary" @click="$router.push('/admin/orders')">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <div class="order-list">
        <div 
          v-for="order in recentOrders" 
          :key="order.id"
          class="order-item"
          @click="viewOrder(order)"
        >
          <div class="order-info">
            <span class="order-no">{{ order.orderNo }}</span>
            <span class="order-time">{{ order.createTime }}</span>
          </div>
          <div class="order-amount">¥{{ order.totalAmount }}</div>
          <el-tag :type="getStatusType(order.status)" size="small">
            {{ getStatusText(order.status) }}
          </el-tag>
        </div>
        <el-empty v-if="recentOrders.length === 0" description="暂无订单" />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getStats } from '../../api/dashboard'
import { getOrderList } from '../../api/order'
import { getGameListForAdmin } from '../../api/game'
import { getCategoryList } from '../../api/category'

export default {
  name: 'Dashboard',
  setup() {
    const router = useRouter()
    
    const statsCards = ref([
      { key: 'users', label: '用户总数', value: '0', icon: 'User', gradient: 'linear-gradient(135deg, #409EFF 0%, #66b1ff 100%)', path: '/admin/users' },
      { key: 'games', label: '游戏总数', value: '0', icon: 'Grid', gradient: 'linear-gradient(135deg, #67C23A 0%, #85ce61 100%)', path: '/admin/games' },
      { key: 'orders', label: '订单总数', value: '0', icon: 'ShoppingCart', gradient: 'linear-gradient(135deg, #E6A23C 0%, #ebb563 100%)', path: '/admin/orders' },
      { key: 'amount', label: '总销售额', value: '¥0', icon: 'Money', gradient: 'linear-gradient(135deg, #F56C6C 0%, #f78989 100%)', path: '/admin/orders' }
    ])
    
    const salesPeriod = ref('week')
    const salesChartRef = ref(null)
    const categoryChartRef = ref(null)
    let salesChart = null
    let categoryChart = null
    
    const recentOrders = ref([])
    
    onMounted(async () => {
      await loadStats()
      await loadRecentOrders()
      nextTick(() => {
        initCharts()
      })
    })
    
    const loadStats = async () => {
      try {
        const res = await getStats()
        statsCards.value[0].value = res.userCount?.toString() || '0'
        statsCards.value[1].value = res.gameCount?.toString() || '0'
        statsCards.value[2].value = res.orderCount?.toString() || '0'
        statsCards.value[3].value = '¥' + (res.totalAmount || '0.00')
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    }
    
    const loadRecentOrders = async () => {
      try {
        const res = await getOrderList({ page: 1, size: 5 })
        recentOrders.value = res.records || []
      } catch (error) {
        console.error('加载订单失败:', error)
      }
    }

    const updateCharts = async () => {
      // 根据选择的时期更新图表数据
      await initCharts()
    }
    
    const initCharts = async () => {
      // 获取真实的订单数据
      let orderRes = { records: [] }
      try {
        orderRes = await getOrderList({ page: 1, size: 100 })
      } catch (e) {}
      const orders = orderRes.records || []

      // 获取真实的分类数据
      let categoryRes = []
      try {
        categoryRes = await getCategoryList()
      } catch (e) {}
      const categories = categoryRes || []

      // 获取真实的游戏数据
      let gameRes = { records: [] }
      try {
        gameRes = await getGameListForAdmin({ page: 1, size: 1000 })
      } catch (e) {}
      const games = gameRes.records || []

      // 计算分类占比数据
      const categoryData = categories.map(cat => {
        const count = games.filter(g => g.categoryId === cat.id).length
        return { name: cat.name, value: count }
      }).filter(item => item.value > 0)

      // 计算销售趋势数据（按天统计）
      const salesData = [0, 0, 0, 0, 0, 0, 0]
      const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      
      orders.forEach(order => {
        if (order.status === 1 && order.createTime) {
          const date = new Date(order.createTime)
          const day = date.getDay()
          const index = day === 0 ? 6 : day - 1
          salesData[index] += Number(order.totalAmount) || 0
        }
      })

      // 销售趋势图
      if (salesChartRef.value) {
        if (salesChart) salesChart.dispose()
        salesChart = echarts.init(salesChartRef.value, 'dark')
        const salesOption = {
          backgroundColor: 'transparent',
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: days,
            axisLine: { lineStyle: { color: '#4a5568' } }
          },
          yAxis: {
            type: 'value',
            axisLine: { lineStyle: { color: '#4a5568' } },
            splitLine: { lineStyle: { color: '#2d3748' } }
          },
          series: [{
            name: '销售额',
            type: 'line',
            smooth: true,
            data: salesData,
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(26, 159, 255, 0.3)' },
                { offset: 1, color: 'rgba(26, 159, 255, 0.05)' }
              ])
            },
            itemStyle: { color: '#1a9fff' },
            lineStyle: { width: 3 }
          }]
        }
        salesChart.setOption(salesOption)
      }
      
      // 分类占比图
      if (categoryChartRef.value) {
        if (categoryChart) categoryChart.dispose()
        categoryChart = echarts.init(categoryChartRef.value, 'dark')
        
        const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#9254de', '#13c2c2']
        const pieData = categoryData.length > 0 ? categoryData : [
          { value: 0, name: '暂无数据' }
        ]
        
        const categoryOption = {
          backgroundColor: 'transparent',
          tooltip: {
            trigger: 'item',
            formatter: '{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            right: '5%',
            top: 'center',
            textStyle: { color: '#a0aec0' }
          },
          series: [{
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['35%', '50%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#1a202c',
              borderWidth: 2
            },
            label: { show: false },
            data: pieData.map((item, index) => ({
              ...item,
              itemStyle: { color: colors[index % colors.length] }
            }))
          }]
        }
        categoryChart.setOption(categoryOption)
      }
      
      // 响应式
      window.addEventListener('resize', () => {
        salesChart?.resize()
        categoryChart?.resize()
      })
    }
    
    const goToDetail = (stat) => {
      if (stat.path) {
        router.push(stat.path)
      }
    }
    
    const getStatusType = (status) => {
      const types = { 0: 'warning', 1: 'success', 2: 'info' }
      return types[status] || 'info'
    }
    
    const getStatusText = (status) => {
      const texts = { 0: '待支付', 1: '已支付', 2: '已取消' }
      return texts[status] || '未知'
    }
    
    const viewOrder = (order) => {
      ElMessage.info(`查看订单 ${order.orderNo}`)
      router.push('/admin/orders')
    }
    
    return {
      statsCards,
      salesPeriod,
      salesChartRef,
      categoryChartRef,
      recentOrders,
      goToDetail,
      getStatusType,
      getStatusText,
      viewOrder,
      updateCharts
    }
  }
}
</script>

<style scoped>
@import '../../styles/admin-common.css';

.dashboard {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-lg);
}

.stat-card {
  background: linear-gradient(160deg, rgba(30, 47, 64, 0.86) 0%, rgba(20, 31, 44, 0.96) 100%);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--space-lg);
  display: flex;
  align-items: center;
  gap: var(--space-md);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(8px);
}

.stat-card:hover {
  transform: translateY(-3px);
  border-color: var(--color-primary);
  box-shadow: var(--shadow-card-hover);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  position: relative;
  z-index: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

.stat-bg-icon {
  position: absolute;
  right: -10px;
  bottom: -10px;
  font-size: 100px;
  opacity: 0.03;
  color: white;
}

/* 图表区域 */
.charts-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--space-lg);
}

.chart-card {
  background: linear-gradient(160deg, rgba(30, 47, 64, 0.82) 0%, rgba(20, 31, 44, 0.96) 100%);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--space-lg);
  box-shadow: var(--shadow-soft);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-lg);
}

.card-header h3 {
  font-size: var(--font-size-lg);
  color: var(--text-primary);
}

.chart-container {
  height: 300px;
}

/* 最近订单 */
.recent-orders {
  background: linear-gradient(160deg, rgba(30, 47, 64, 0.82) 0%, rgba(20, 31, 44, 0.96) 100%);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--space-lg);
  box-shadow: var(--shadow-soft);
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.order-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md);
  background: rgba(22, 34, 48, 0.9);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.order-item:hover {
  background: rgba(35, 57, 77, 0.95);
}

.order-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-no {
  font-weight: 500;
  color: var(--text-primary);
}

.order-time {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.order-amount {
  font-weight: bold;
  color: #a4d007;
}

/* 动画 */
.fade-in-up {
  opacity: 0;
  animation: fadeInUp 0.5s ease-out forwards;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
