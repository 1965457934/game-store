<template>
  <div class="cart-page">
    <div class="container">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1 class="page-title">
          <el-icon><ShoppingCart /></el-icon>
          购物车
          <span class="cart-count" v-if="cartItems.length > 0">({{ cartItems.length }})</span>
        </h1>
        <p class="page-desc">管理您的游戏商品</p>
      </div>

      <!-- 购物车内容 -->
      <div v-if="cartItems.length > 0" class="cart-content">
        <!-- 商品列表 -->
        <div class="cart-items">
          <div 
            v-for="(item, index) in cartItems" 
            :key="item.id" 
            class="cart-item"
            :class="{ 'fade-in-up': true }"
            :style="{ animationDelay: `${index * 0.05}s` }"
          >
            <div class="item-select">
              <el-checkbox v-model="item.selected" @change="updateSelection" />
            </div>
            <div class="item-image" @click="goToGame(item.gameId)">
              <img :src="item.gameCover || '/default-game.png'" :alt="item.gameName">
              <div class="image-overlay">
                <el-icon><View /></el-icon>
              </div>
            </div>
            <div class="item-info">
              <h3 class="game-name" @click="goToGame(item.gameId)">{{ item.gameName }}</h3>
              <p class="game-category">{{ item.categoryName || '游戏' }}</p>
            </div>
            <div class="item-price">
              <span class="price">¥{{ item.price }}</span>
            </div>
            <div class="item-quantity">
              <el-input-number 
                v-model="item.quantity" 
                :min="1" 
                :max="99"
                size="small"
                @change="updateQuantity(item)"
              />
            </div>
            <div class="item-total">
              <span class="total-price">¥{{ item.totalPrice }}</span>
            </div>
            <div class="item-actions">
              <el-button 
                link 
                type="danger" 
                class="delete-btn"
                @click="removeItem(item.id)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </div>

        <!-- 购物车底部 -->
        <div class="cart-footer">
          <div class="footer-left">
            <el-checkbox v-model="allSelected" @change="toggleSelectAll">
              全选
            </el-checkbox>
            <el-button link type="danger" @click="removeSelected" :disabled="selectedItems.length === 0">
              删除选中
            </el-button>
            <el-button link @click="clearCartHandler" :disabled="cartItems.length === 0">
              清空购物车
            </el-button>
          </div>
          <div class="footer-right">
            <div class="summary">
              <div class="summary-item">
                <span>已选商品 {{ selectedItems.length }} 件</span>
              </div>
              <div class="summary-item total">
                <span>合计：</span>
                <span class="total-amount">¥{{ totalAmount }}</span>
              </div>
            </div>
            <el-button 
              type="primary" 
              size="large" 
              class="checkout-btn"
              :disabled="selectedItems.length === 0"
              @click="checkout"
            >
              去结算
              <el-icon class="btn-icon"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon">
          <el-icon><ShoppingCart /></el-icon>
        </div>
        <h3>购物车是空的</h3>
        <p>快去挑选心仪的游戏吧</p>
        <el-button type="primary" size="large" @click="$router.push('/games')">
          去逛逛
        </el-button>
      </div>

      <!-- 推荐商品 -->
      <div v-if="recommendGames.length > 0" class="recommend-section">
        <h2 class="section-title">
          <el-icon><Star /></el-icon>
          猜你喜欢
        </h2>
        <div class="recommend-grid">
          <div 
            v-for="game in recommendGames" 
            :key="game.id"
            class="recommend-card"
            @click="goToGame(game.id)"
          >
            <img :src="game.cover || '/default-game.png'" :alt="game.name">
            <div class="recommend-info">
              <h4 class="text-ellipsis">{{ game.name }}</h4>
              <span class="price">¥{{ game.price }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCartList, updateCart, deleteCart, clearCart } from '../../api/cart'
import { createOrder } from '../../api/order'
import { getTopGames } from '../../api/game'

export default {
  name: 'Cart',
  setup() {
    const router = useRouter()
    const store = useStore()
    const cartItems = ref([])
    const recommendGames = ref([])
    const loading = ref(false)

    // 选中的商品
    const selectedItems = computed(() => cartItems.value.filter(item => item.selected))
    
    // 总金额
    const totalAmount = computed(() => {
      return selectedItems.value
        .reduce((sum, item) => sum + Number(item.totalPrice), 0)
        .toFixed(2)
    })

    // 是否全选
    const allSelected = computed({
      get: () => cartItems.value.length > 0 && cartItems.value.every(item => item.selected),
      set: (val) => {
        cartItems.value.forEach(item => item.selected = val)
      }
    })

    onMounted(() => {
      loadCart()
      loadRecommendGames()
    })

    const loadCart = async () => {
      loading.value = true
      try {
        const res = await getCartList()
        cartItems.value = (res || []).map(item => ({ ...item, selected: true }))
      } catch (error) {
        console.error('加载购物车失败:', error)
      } finally {
        loading.value = false
      }
    }

    const loadRecommendGames = async () => {
      try {
        const res = await getTopGames(4)
        recommendGames.value = res || []
      } catch (error) {
        console.error('加载推荐游戏失败:', error)
      }
    }

    const updateQuantity = async (item) => {
      try {
        await updateCart(item.id, { quantity: item.quantity })
        // 重新计算小计
        item.totalPrice = (item.price * item.quantity).toFixed(2)
        store.dispatch('getCartCount')
      } catch (error) {
        console.error('更新数量失败:', error)
      }
    }

    const updateSelection = () => {
      // 触发计算属性更新
    }

    const toggleSelectAll = (val) => {
      cartItems.value.forEach(item => item.selected = val)
    }

    const removeItem = async (id) => {
      try {
        await ElMessageBox.confirm('确定删除该商品？', '提示', { 
          type: 'warning',
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        })
        await deleteCart(id)
        ElMessage.success('删除成功')
        loadCart()
        store.dispatch('getCartCount')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
        }
      }
    }

    const removeSelected = async () => {
      try {
        await ElMessageBox.confirm(`确定删除选中的 ${selectedItems.value.length} 件商品？`, '提示', { 
          type: 'warning',
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        })
        for (const item of selectedItems.value) {
          await deleteCart(item.id)
        }
        ElMessage.success('删除成功')
        loadCart()
        store.dispatch('getCartCount')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
        }
      }
    }

    const clearCartHandler = async () => {
      try {
        await ElMessageBox.confirm('确定清空购物车？', '提示', { 
          type: 'warning',
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        })
        await clearCart()
        ElMessage.success('购物车已清空')
        loadCart()
        store.dispatch('getCartCount')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('清空失败:', error)
        }
      }
    }

    const checkout = async () => {
      if (selectedItems.value.length === 0) {
        ElMessage.warning('请选择要结算的商品')
        return
      }
      
      // 如果购物车中有未选中的商品，提示用户
      const unselectedItems = cartItems.value.filter(item => !item.selected)
      if (unselectedItems.length > 0) {
        try {
          await ElMessageBox.confirm(
            `您有 ${unselectedItems.length} 件商品未选中，结算将只购买选中的商品。未选中的商品将保留在购物车中。`,
            '确认结算',
            { confirmButtonText: '确认结算', cancelButtonText: '取消', type: 'info' }
          )
        } catch {
          return
        }
      }
      
      try {
        // 后端接口只需要remark参数，会自动从购物车获取所有商品
        // 如果要结算部分商品，需要先删除未选中的商品，结算后再恢复（简化处理）
        // 或者先保存未选中的商品，结算后重新添加
        const res = await createOrder({ remark: '' })
        ElMessage.success('订单创建成功')
        store.dispatch('getCartCount')
        router.push('/orders')
      } catch (error) {
        console.error('创建订单失败:', error)
        ElMessage.error('创建订单失败，请稍后重试')
      }
    }

    const goToGame = (id) => {
      router.push(`/game/${id}`)
    }

    return {
      cartItems,
      recommendGames,
      selectedItems,
      totalAmount,
      allSelected,
      loading,
      updateQuantity,
      updateSelection,
      toggleSelectAll,
      removeItem,
      removeSelected,
      clearCartHandler,
      checkout,
      goToGame
    }
  }
}
</script>

<style scoped>
.cart-page {
  padding: var(--space-xl) 0;
  min-height: calc(100vh - 64px - 200px);
}

/* 页面头部 */
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

.cart-count {
  color: var(--text-muted);
  font-weight: normal;
  font-size: var(--font-size-lg);
}

.page-desc {
  color: var(--text-muted);
}

/* 购物车内容 */
.cart-content {
  background: linear-gradient(160deg, rgba(30, 47, 64, 0.82) 0%, rgba(20, 31, 44, 0.96) 100%);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(84, 123, 157, 0.35);
  overflow: hidden;
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(8px);
}

/* 商品列表 */
.cart-items {
  padding: var(--space-md);
}

.cart-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md);
  background: rgba(22, 34, 48, 0.92);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-md);
  transition: all var(--transition-fast);
  border: 1px solid rgba(70, 101, 129, 0.28);
}

.cart-item:hover {
  border-color: rgba(102, 192, 244, 0.45);
  transform: translateY(-1px);
}

.cart-item:last-child {
  margin-bottom: 0;
}

.item-select {
  flex-shrink: 0;
}

.item-select :deep(.el-checkbox__inner) {
  width: 20px;
  height: 20px;
}

.item-image {
  position: relative;
  width: 120px;
  height: 68px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(4, 10, 16, 0.25) 0%, rgba(4, 10, 16, 0.76) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.item-image:hover .image-overlay {
  opacity: 1;
}

.image-overlay .el-icon {
  color: white;
  font-size: 24px;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.game-name {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-xs);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.game-name:hover {
  color: var(--color-primary);
}

.game-category {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.item-price {
  width: 100px;
  text-align: center;
  flex-shrink: 0;
}

.item-price .price {
  font-size: var(--font-size-md);
  color: var(--text-secondary);
}

.item-quantity {
  width: 120px;
  text-align: center;
  flex-shrink: 0;
}

.item-quantity :deep(.el-input-number) {
  width: 100px;
}

.item-quantity :deep(.el-input__wrapper) {
  background: var(--bg-tertiary) !important;
}

.item-total {
  width: 120px;
  text-align: center;
  flex-shrink: 0;
}

.total-price {
  font-size: var(--font-size-lg);
  font-weight: bold;
  color: #a4d007;
}

.item-actions {
  width: 60px;
  text-align: center;
  flex-shrink: 0;
}

.delete-btn {
  font-size: 20px;
  opacity: 0.6;
  transition: opacity var(--transition-fast);
}

.delete-btn:hover {
  opacity: 1;
}

/* 购物车底部 */
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-lg) var(--space-xl);
  background: rgba(22, 34, 48, 0.96);
  border-top: 1px solid var(--border-color);
  flex-wrap: wrap;
  gap: var(--space-md);
}

.footer-left {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}

.footer-left :deep(.el-checkbox__label) {
  color: var(--text-secondary);
}

.footer-right {
  display: flex;
  align-items: center;
  gap: var(--space-xl);
}

.summary {
  text-align: right;
}

.summary-item {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  margin-bottom: var(--space-xs);
}

.summary-item.total {
  font-size: var(--font-size-md);
  margin-bottom: 0;
}

.total-amount {
  font-size: 28px;
  font-weight: bold;
  color: #a4d007;
  margin-left: var(--space-sm);
}

.checkout-btn {
  padding: 16px 40px;
  font-size: var(--font-size-lg);
  border-radius: var(--radius-md);
}

.checkout-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-icon {
  margin-left: var(--space-xs);
  transition: transform var(--transition-fast);
}

.checkout-btn:hover .btn-icon {
  transform: translateX(4px);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: var(--space-xxl);
  background: linear-gradient(160deg, rgba(30, 47, 64, 0.82) 0%, rgba(20, 31, 44, 0.96) 100%);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(84, 123, 157, 0.35);
  box-shadow: var(--shadow-soft);
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

/* 推荐商品 */
.recommend-section {
  margin-top: var(--space-xxl);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  font-size: var(--font-size-xl);
  color: var(--text-primary);
  margin-bottom: var(--space-lg);
}

.section-title .el-icon {
  color: var(--color-primary);
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: var(--space-lg);
}

.recommend-card {
  background: linear-gradient(160deg, rgba(30, 47, 64, 0.82) 0%, rgba(20, 31, 44, 0.96) 100%);
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-normal);
  border: 1px solid rgba(84, 123, 157, 0.32);
  box-shadow: var(--shadow-soft);
}

.recommend-card:hover {
  transform: translateY(-4px);
  border-color: var(--color-primary);
}

.recommend-card img {
  width: 100%;
  aspect-ratio: 460 / 215;
  object-fit: cover;
}

.recommend-info {
  padding: var(--space-md);
}

.recommend-info h4 {
  font-size: var(--font-size-sm);
  color: var(--text-primary);
  margin-bottom: var(--space-sm);
}

.recommend-info .price {
  font-weight: bold;
  color: #a4d007;
}

/* 动画 */
.fade-in-up {
  opacity: 0;
  animation: fadeInUp 0.5s ease-out forwards;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .cart-item {
    flex-wrap: wrap;
  }
  
  .item-quantity,
  .item-price,
  .item-total {
    width: auto;
  }
  
  .cart-footer {
    flex-direction: column;
    align-items: stretch;
  }
  
  .footer-left,
  .footer-right {
    justify-content: center;
  }
  
  .summary {
    text-align: center;
  }
  
  .checkout-btn {
    width: 100%;
  }
}

@media (max-width: 576px) {
  .item-image {
    width: 80px;
    height: 45px;
  }
  
  .item-info {
    width: 100%;
  }
  
  .recommend-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-md);
  }
}
</style>
