<template>
  <div class="order-page">
    <div class="container">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1 class="page-title">
          <el-icon><ShoppingBag /></el-icon>
          我的订单
        </h1>
        <p class="page-desc">查看和管理您的所有订单</p>
      </div>

      <!-- 订单统计卡片 -->
      <div class="stats-bar">
        <div 
          v-for="stat in orderStats" 
          :key="stat.key"
          class="stat-item"
          :class="{ active: activeTab === stat.key }"
          @click="activeTab = stat.key"
        >
          <span class="stat-number">{{ stat.count }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>

      <!-- 订单列表 -->
      <div v-if="orders.length > 0" class="order-list">
        <div 
          v-for="(order, index) in orders" 
          :key="order.id" 
          class="order-card"
          :class="{ 'fade-in-up': true }"
          :style="{ animationDelay: `${index * 0.1}s` }"
        >
          <!-- 订单头部 -->
          <div class="order-header">
            <div class="header-left">
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <span class="order-time">{{ formatTime(order.createTime) }}</span>
            </div>
            <div class="header-right">
              <el-tag 
                :type="getStatusType(order.status)" 
                effect="dark"
                class="status-tag"
                size="large"
              >
                <el-icon class="status-icon">
                  <component :is="getStatusIcon(order.status)" />
                </el-icon>
                {{ getStatusText(order.status) }}
              </el-tag>
            </div>
          </div>

          <!-- 订单商品 -->
          <div class="order-body">
            <div 
              v-for="item in order.items" 
              :key="item.id" 
              class="order-item"
              @click="goToGame(item.gameId)"
            >
              <img :src="item.gameCover || '/default-game.png'" :alt="item.gameName" class="item-image">
              <div class="item-info">
                <h4 class="item-name">{{ item.gameName }}</h4>
                <p class="item-category">{{ item.categoryName || '游戏' }}</p>
              </div>
              <div class="item-price">
                <span class="quantity">×{{ item.quantity }}</span>
                <span class="price">¥{{ item.price }}</span>
              </div>
            </div>
          </div>

          <!-- 订单底部 -->
          <div class="order-footer">
            <div class="footer-left">
              <span class="total-label">订单金额：</span>
              <span class="total-amount">¥{{ order.totalAmount }}</span>
            </div>
            <div class="footer-right">
              <el-button 
                v-if="order.status === 0"
                type="primary"
                @click="payOrderHandler(order)"
                class="action-btn"
              >
                <el-icon><Wallet /></el-icon>
                立即支付
              </el-button>
              <el-button 
                v-if="order.status === 0"
                @click="cancelOrderHandler(order.id)"
                class="action-btn"
              >
                取消订单
              </el-button>
              <el-button 
                v-if="order.status === 1"
                type="success"
                plain
                class="action-btn"
                @click="downloadGame(order)"
              >
                <el-icon><Download /></el-icon>
                下载游戏
              </el-button>
              <el-button 
                @click="viewOrderDetail(order)"
                class="action-btn"
              >
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading" class="empty-state">
        <div class="empty-icon">
          <el-icon><ShoppingBag /></el-icon>
        </div>
        <h3>暂无订单</h3>
        <p>您还没有下单，快去选购心仪的游戏吧</p>
        <el-button type="primary" size="large" @click="$router.push('/games')">
          去购物
        </el-button>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div v-for="i in 3" :key="i" class="order-card skeleton-card">
          <div class="skeleton-header skeleton"></div>
          <div class="skeleton-body">
            <div class="skeleton-image skeleton"></div>
            <div class="skeleton-info">
              <div class="skeleton-line skeleton" style="width: 60%"></div>
              <div class="skeleton-line skeleton" style="width: 40%"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="orders.length > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[5, 10, 20]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadOrders"
          @size-change="handleSizeChange"
          class="pagination"
        />
      </div>
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="订单详情"
      width="700px"
      class="order-detail-dialog"
    >
      <div v-if="selectedOrder" class="order-detail">
        <div class="detail-header">
          <div class="detail-row">
            <span class="detail-label">订单号</span>
            <span class="detail-value">{{ selectedOrder.orderNo }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">下单时间</span>
            <span class="detail-value">{{ selectedOrder.createTime }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">订单状态</span>
            <el-tag :type="getStatusType(selectedOrder.status)" effect="dark">
              {{ getStatusText(selectedOrder.status) }}
            </el-tag>
          </div>
          <div class="detail-row">
            <span class="detail-label">支付方式</span>
            <span class="detail-value">{{ selectedOrder.payType || '在线支付' }}</span>
          </div>
        </div>
        
        <div class="detail-items">
          <h4>商品明细</h4>
          <div 
            v-for="item in selectedOrder.items" 
            :key="item.id"
            class="detail-item"
          >
            <img :src="item.gameCover || '/default-game.png'" :alt="item.gameName">
            <div class="item-detail">
              <span class="item-name">{{ item.gameName }}</span>
              <span class="item-price">¥{{ item.price }} × {{ item.quantity }}</span>
            </div>
            <span class="item-total">¥{{ item.totalPrice }}</span>
          </div>
        </div>
        
        <div class="detail-summary">
          <div class="summary-row">
            <span>商品总额</span>
            <span>¥{{ selectedOrder.totalAmount }}</span>
          </div>
          <div class="summary-row">
            <span>优惠金额</span>
            <span>-¥{{ selectedOrder.discountAmount || '0.00' }}</span>
          </div>
          <div class="summary-row total">
            <span>实付金额</span>
            <span class="amount">¥{{ selectedOrder.totalAmount }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, payOrder, cancelOrder } from '../../api/order'

export default {
  name: 'OrderList',
  setup() {
    const router = useRouter()
    const orders = ref([])
    const activeTab = ref('all')
    const page = ref(1)
    const size = ref(10)
    const total = ref(0)
    const loading = ref(false)
    const detailVisible = ref(false)
    const selectedOrder = ref(null)

    // 订单统计
    const orderStats = ref([
      { key: 'all', label: '全部订单', count: 0 },
      { key: 'unpaid', label: '待支付', count: 0 },
      { key: 'paid', label: '已支付', count: 0 },
      { key: 'cancelled', label: '已取消', count: 0 }
    ])

    onMounted(() => {
      loadOrders()
      loadOrderStats()
    })

    // 监听标签切换
    watch(activeTab, () => {
      page.value = 1
      loadOrders()
    })

    const loadOrders = async () => {
      loading.value = true
      try {
        const statusMap = { all: null, unpaid: 0, paid: 1, cancelled: 2 }
        const res = await getOrderList({
          page: page.value,
          size: size.value,
          status: statusMap[activeTab.value]
        })
        orders.value = res.records || []
        total.value = res.total || 0
      } catch (error) {
        console.error('加载订单失败:', error)
      } finally {
        loading.value = false
      }
    }

    const loadOrderStats = async () => {
      try {
        const res = await getOrderList({ page: 1, size: 1000 })
        const allOrders = res.records || []
        orderStats.value[0].count = allOrders.length
        orderStats.value[1].count = allOrders.filter(o => o.status === 0).length
        orderStats.value[2].count = allOrders.filter(o => o.status === 1).length
        orderStats.value[3].count = allOrders.filter(o => o.status === 2).length
      } catch (error) {
        console.error('加载统计失败:', error)
      }
    }

    const handleSizeChange = (newSize) => {
      size.value = newSize
      page.value = 1
      loadOrders()
    }

    const getStatusType = (status) => {
      const types = { 0: 'warning', 1: 'success', 2: 'info' }
      return types[status] || 'info'
    }

    const getStatusText = (status) => {
      const texts = { 0: '待支付', 1: '已支付', 2: '已取消' }
      return texts[status] || '未知'
    }

    const getStatusIcon = (status) => {
      const icons = { 0: 'Wallet', 1: 'CircleCheck', 2: 'CircleClose' }
      return icons[status] || 'InfoFilled'
    }

    const payOrderHandler = async (order) => {
      try {
        await ElMessageBox.confirm(
          `确认支付订单 ${order.orderNo}？\n金额：¥${order.totalAmount}`,
          '确认支付',
          {
            confirmButtonText: '立即支付',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        await payOrder({ orderNo: order.orderNo })
        ElMessage.success('支付成功')
        loadOrders()
        loadOrderStats()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('支付失败:', error)
        }
      }
    }

    const cancelOrderHandler = async (id) => {
      try {
        await ElMessageBox.confirm('确定取消该订单？', '提示', { 
          type: 'warning',
          confirmButtonText: '确定取消',
          cancelButtonText: '再想想'
        })
        await cancelOrder(id)
        ElMessage.success('订单已取消')
        loadOrders()
        loadOrderStats()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消失败:', error)
        }
      }
    }

    const downloadGame = (order) => {
      ElMessage.info('下载功能即将上线')
    }

    const viewOrderDetail = (order) => {
      selectedOrder.value = order
      detailVisible.value = true
    }

    const goToGame = (gameId) => {
      if (gameId) {
        router.push(`/game/${gameId}`)
      }
    }

    const formatTime = (time) => {
      if (!time) return ''
      const date = new Date(time)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    return {
      orders,
      activeTab,
      orderStats,
      page,
      size,
      total,
      loading,
      detailVisible,
      selectedOrder,
      loadOrders,
      handleSizeChange,
      getStatusType,
      getStatusText,
      getStatusIcon,
      payOrderHandler,
      cancelOrderHandler,
      downloadGame,
      viewOrderDetail,
      goToGame,
      formatTime
    }
  }
}
</script>

<style scoped>
.order-page {
  padding: var(--space-xl) 0;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: var(--space-xl);
}

.page-title {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  font-size: var(--font-size-xxl);
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: var(--space-sm);
}

.page-title .el-icon {
  color: var(--color-primary);
}

.page-desc {
  color: var(--text-muted);
}

/* 统计栏 */
.stats-bar {
  display: flex;
  gap: var(--space-md);
  margin-bottom: var(--space-xl);
  overflow-x: auto;
  padding-bottom: var(--space-sm);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-md) var(--space-xl);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  min-width: 100px;
}

.stat-item:hover,
.stat-item.active {
  border-color: var(--color-primary);
  background: rgba(26, 159, 255, 0.1);
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: var(--text-primary);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin-top: 4px;
}

/* 订单列表 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
  margin-bottom: var(--space-xl);
}

.order-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all var(--transition-normal);
}

.order-card:hover {
  border-color: var(--border-color);
  box-shadow: var(--shadow-md);
}

/* 订单头部 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-md) var(--space-lg);
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  flex-wrap: wrap;
  gap: var(--space-sm);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  flex-wrap: wrap;
}

.order-no {
  font-weight: 500;
  color: var(--text-primary);
}

.order-time {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

.status-tag {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
}

.status-icon {
  font-size: 14px;
}

/* 订单商品 */
.order-body {
  padding: var(--space-md) var(--space-lg);
}

.order-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md) 0;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.order-item:last-child {
  border-bottom: none;
}

.order-item:hover {
  background: var(--bg-secondary);
  margin: 0 calc(-1 * var(--space-lg));
  padding-left: var(--space-lg);
  padding-right: var(--space-lg);
}

.item-image {
  width: 80px;
  height: 45px;
  border-radius: var(--radius-sm);
  object-fit: cover;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: var(--font-size-md);
  color: var(--text-primary);
  margin-bottom: 4px;
}

.item-category {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.item-price {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  flex-shrink: 0;
}

.quantity {
  color: var(--text-muted);
  font-size: var(--font-size-sm);
}

.price {
  font-weight: bold;
  color: #a4d007;
  font-size: var(--font-size-md);
}

/* 订单底部 */
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-md) var(--space-lg);
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-color);
  flex-wrap: wrap;
  gap: var(--space-md);
}

.footer-left {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.total-label {
  color: var(--text-muted);
}

.total-amount {
  font-size: 24px;
  font-weight: bold;
  color: #a4d007;
}

.footer-right {
  display: flex;
  gap: var(--space-sm);
  flex-wrap: wrap;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: var(--space-xxl);
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
}

.empty-icon {
  width: 120px;
  height: 120px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--space-lg);
}

.empty-icon .el-icon {
  font-size: 60px;
  color: var(--text-muted);
}

.empty-state h3 {
  font-size: var(--font-size-lg);
  color: var(--text-primary);
  margin-bottom: var(--space-sm);
}

.empty-state p {
  color: var(--text-muted);
  margin-bottom: var(--space-xl);
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.skeleton-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-lg);
}

.skeleton-header {
  height: 24px;
  width: 200px;
  margin-bottom: var(--space-md);
}

.skeleton-body {
  display: flex;
  gap: var(--space-md);
}

.skeleton-image {
  width: 80px;
  height: 45px;
  border-radius: var(--radius-sm);
}

.skeleton-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
  justify-content: center;
}

.skeleton-line {
  height: 16px;
  border-radius: var(--radius-sm);
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding-top: var(--space-xl);
}

/* 订单详情弹窗 */
.order-detail-dialog :deep(.el-dialog) {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
}

.order-detail-dialog :deep(.el-dialog__title) {
  color: var(--text-primary);
}

.order-detail {
  color: var(--text-secondary);
}

.detail-header {
  background: var(--bg-secondary);
  padding: var(--space-lg);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-lg);
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-sm) 0;
  border-bottom: 1px solid var(--border-color);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  color: var(--text-muted);
}

.detail-value {
  color: var(--text-primary);
  font-weight: 500;
}

.detail-items {
  margin-bottom: var(--space-lg);
}

.detail-items h4 {
  color: var(--text-primary);
  margin-bottom: var(--space-md);
}

.detail-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md);
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-sm);
}

.detail-item img {
  width: 60px;
  height: 34px;
  border-radius: var(--radius-sm);
  object-fit: cover;
}

.item-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-detail .item-name {
  color: var(--text-primary);
  margin-bottom: 4px;
}

.item-detail .item-price {
  color: var(--text-muted);
  font-size: var(--font-size-sm);
}

.item-total {
  font-weight: bold;
  color: #a4d007;
}

.detail-summary {
  background: var(--bg-secondary);
  padding: var(--space-lg);
  border-radius: var(--radius-md);
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: var(--space-sm) 0;
  color: var(--text-secondary);
}

.summary-row.total {
  border-top: 1px solid var(--border-color);
  margin-top: var(--space-sm);
  padding-top: var(--space-md);
  font-size: var(--font-size-lg);
  font-weight: bold;
}

.summary-row.total .amount {
  color: #a4d007;
  font-size: 24px;
}

/* 动画 */
.fade-in-up {
  opacity: 0;
  animation: fadeInUp 0.5s ease-out forwards;
}

/* 响应式 */
@media (max-width: 768px) {
  .order-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .order-footer {
    flex-direction: column;
    align-items: stretch;
  }
  
  .footer-left {
    justify-content: space-between;
  }
  
  .footer-right {
    justify-content: flex-end;
  }
  
  .action-btn {
    flex: 1;
    justify-content: center;
  }
}

@media (max-width: 576px) {
  .stats-bar {
    gap: var(--space-sm);
  }
  
  .stat-item {
    min-width: 80px;
    padding: var(--space-sm) var(--space-md);
  }
  
  .stat-number {
    font-size: 20px;
  }
  
  .order-item {
    flex-wrap: wrap;
  }
  
  .item-info {
    width: calc(100% - 100px);
  }
  
  .item-price {
    width: 100%;
    justify-content: flex-end;
    margin-top: var(--space-sm);
  }
}
</style>
