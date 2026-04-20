<template>
  <div class="layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="container header-content">
        <!-- Logo -->
        <div class="logo" @click="$router.push('/')">
          <el-icon size="36" class="logo-icon"><GameController /></el-icon>
          <span class="logo-text">游戏商城</span>
        </div>
        
        <!-- 桌面导航 -->
        <nav class="nav-menu desktop-only flex">
          <router-link to="/" class="nav-link" :class="{ active: $route.path === '/' }">
            <el-icon><HomeFilled /></el-icon>
            首页
          </router-link>
          <router-link to="/games" class="nav-link" :class="{ active: $route.path.includes('/games') || $route.path.includes('/game/') }">
            <el-icon><Grid /></el-icon>
            游戏库
          </router-link>
        </nav>
        
        <!-- 搜索框 -->
        <div class="search-box desktop-only">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索游戏..."
            clearable
            @keyup.enter="handleSearch"
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        
        <!-- 用户操作区 -->
        <div class="user-actions">
          <!-- 购物车 -->
          <el-badge :value="cartCount" :hidden="cartCount === 0" class="cart-badge">
            <el-button 
              circle 
              class="icon-btn"
              @click="$router.push('/cart')"
            >
              <el-icon><ShoppingCart /></el-icon>
            </el-button>
          </el-badge>
          
          <!-- 登录状态 -->
          <template v-if="isLoggedIn">
            <el-dropdown @command="handleCommand" trigger="click">
              <div class="user-info">
                <el-avatar :size="36" :src="user.avatar || '/default-avatar.png'" class="user-avatar" />
                <span class="username desktop-only">{{ user.username }}</span>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="user-dropdown">
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="orders">
                    <el-icon><List /></el-icon>我的订单
                  </el-dropdown-item>
                  <el-dropdown-item command="cart">
                    <el-icon><ShoppingCart /></el-icon>购物车
                  </el-dropdown-item>
                  <el-dropdown-item v-if="user.role === 1" command="admin" divided>
                    <el-icon><Setting /></el-icon>后台管理
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          
          <template v-else>
            <el-button type="primary" class="login-btn" @click="$router.push('/login')">
              登录
            </el-button>
          </template>
          
          <!-- 移动端菜单按钮 -->
          <el-button circle class="icon-btn mobile-only" @click="mobileMenuOpen = true">
            <el-icon><Menu /></el-icon>
          </el-button>
        </div>
      </div>
    </header>
    
    <!-- 主内容区 -->
    <main class="main-content">
      <router-view />
    </main>
    
    <!-- 页脚 -->
    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-brand">
            <div class="logo">
              <el-icon size="28"><GameController /></el-icon>
              <span>游戏商城</span>
            </div>
            <p class="footer-desc">发现精彩游戏，享受极致体验</p>
          </div>
        </div>
        <div class="footer-bottom">
          <p>© 2026 游戏商城 Game Store. All rights reserved.</p>
        </div>
      </div>
    </footer>
    
    <!-- 移动端菜单 -->
    <transition name="slide-left">
      <div v-if="mobileMenuOpen" class="mobile-menu">
        <div class="mobile-menu-header">
          <span class="menu-title">菜单</span>
          <el-button circle class="close-btn" @click="mobileMenuOpen = false">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
        <nav class="mobile-nav">
          <router-link to="/" class="mobile-nav-link" @click="mobileMenuOpen = false">
            <el-icon><HomeFilled /></el-icon>首页
          </router-link>
          <router-link to="/games" class="mobile-nav-link" @click="mobileMenuOpen = false">
            <el-icon><Grid /></el-icon>游戏库
          </router-link>
          <router-link v-if="isLoggedIn" to="/cart" class="mobile-nav-link" @click="mobileMenuOpen = false">
            <el-icon><ShoppingCart /></el-icon>购物车
          </router-link>
          <router-link v-if="isLoggedIn" to="/orders" class="mobile-nav-link" @click="mobileMenuOpen = false">
            <el-icon><List /></el-icon>我的订单
          </router-link>
        </nav>
      </div>
    </transition>
    
    <!-- 遮罩层 -->
    <transition name="fade">
      <div v-if="mobileMenuOpen" class="overlay" @click="mobileMenuOpen = false"></div>
    </transition>
  </div>
</template>

<script>
import { computed, ref, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'Layout',
  setup() {
    const store = useStore()
    const router = useRouter()
    const searchKeyword = ref('')
    const mobileMenuOpen = ref(false)

    const isLoggedIn = computed(() => store.state.token)
    const user = computed(() => store.state.user)
    const cartCount = computed(() => store.state.cartCount || 0)

    onMounted(() => {
      if (isLoggedIn.value) {
        store.dispatch('getCartCount')
      }
    })

    const handleSearch = () => {
      if (searchKeyword.value.trim()) {
        router.push({
          path: '/games',
          query: { keyword: searchKeyword.value }
        })
      }
    }

    const handleCommand = (command) => {
      switch (command) {
        case 'profile':
          router.push('/profile')
          break
        case 'orders':
          router.push('/orders')
          break
        case 'cart':
          router.push('/cart')
          break
        case 'admin':
          router.push('/admin')
          break
        case 'logout':
          store.dispatch('logout')
          ElMessage.success('退出成功')
          router.push('/login')
          break
      }
    }

    return {
      isLoggedIn,
      user,
      cartCount,
      searchKeyword,
      mobileMenuOpen,
      handleSearch,
      handleCommand
    }
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏 */
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(27, 40, 56, 0.82);
  border-bottom: 1px solid rgba(86, 123, 156, 0.35);
  backdrop-filter: blur(14px);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.2);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  gap: var(--space-lg);
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  cursor: pointer;
  transition: opacity var(--transition-fast);
  flex-shrink: 0;
}

.logo:hover {
  opacity: 0.8;
}

.logo-icon {
  color: var(--color-primary);
}

.logo-text {
  font-size: var(--font-size-lg);
  font-weight: bold;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 导航菜单 */
.nav-menu {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: var(--space-md);
  flex-wrap: nowrap;
}

.nav-link {
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  align-items: center;
  gap: var(--space-xs);
  padding: var(--space-sm) var(--space-md);
  color: var(--text-secondary);
  text-decoration: none;
  font-weight: 500;
  border-radius: var(--radius-full);
  border: 1px solid transparent;
  transition: all var(--transition-normal);
  white-space: nowrap;
}

.nav-link:hover,
.nav-link.active {
  color: var(--color-primary);
  border-color: rgba(102, 192, 244, 0.4);
  background: rgba(26, 159, 255, 0.14);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.12);
}

/* 搜索框 */
.search-box {
  flex: 1;
  max-width: 400px;
}

.search-input :deep(.el-input__wrapper) {
  background: var(--bg-tertiary) !important;
  border-radius: var(--radius-full) !important;
  padding-left: var(--space-md) !important;
  border: 1px solid rgba(102, 192, 244, 0.2);
}

/* 用户操作区 */
.user-actions {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  flex-shrink: 0;
}

.icon-btn {
  background: var(--bg-tertiary) !important;
  border: none !important;
  color: var(--text-secondary) !important;
  transition: all var(--transition-normal);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.icon-btn:hover {
  background: var(--color-primary) !important;
  color: white !important;
  transform: scale(1.05);
}

.cart-badge :deep(.el-badge__content) {
  background: var(--color-danger);
  border: none;
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.user-info:hover {
  background: var(--bg-tertiary);
}

.user-avatar {
  border: 2px solid var(--border-color);
  transition: all var(--transition-normal);
}

.user-info:hover .user-avatar {
  border-color: var(--color-primary);
}

.username {
  color: var(--text-primary);
  font-weight: 500;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-icon {
  color: var(--text-muted);
  font-size: 12px;
}

.login-btn {
  padding: 8px 20px;
  border-radius: var(--radius-full);
}

/* 下拉菜单 */
.user-dropdown {
  background: var(--bg-card) !important;
  border: 1px solid var(--border-color) !important;
}

.user-dropdown :deep(.el-dropdown-menu__item) {
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-sm) var(--space-md);
}

.user-dropdown :deep(.el-dropdown-menu__item:hover) {
  background: var(--bg-tertiary) !important;
  color: var(--color-primary) !important;
}

/* 主内容区 */
.main-content {
  flex: 1;
  min-height: calc(100vh - 64px - 120px);
}

/* 页脚 */
.footer {
  background: linear-gradient(180deg, rgba(27, 40, 56, 0.9) 0%, rgba(20, 29, 40, 0.98) 100%);
  border-top: 1px solid var(--border-color);
  padding: var(--space-xl) 0 var(--space-md);
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-lg);
  margin-bottom: var(--space-lg);
}

.footer-brand .logo {
  margin-bottom: var(--space-sm);
}

.footer-desc {
  color: var(--text-muted);
  font-size: var(--font-size-sm);
}

.footer-links {
  display: flex;
  gap: var(--space-lg);
  flex-wrap: wrap;
}

.footer-link {
  color: var(--text-secondary);
  text-decoration: none;
  font-size: var(--font-size-sm);
  transition: color var(--transition-fast);
}

.footer-link:hover {
  color: var(--color-primary);
}

.footer-bottom {
  text-align: center;
  padding-top: var(--space-md);
  border-top: 1px solid var(--border-color);
  color: var(--text-muted);
  font-size: var(--font-size-xs);
}

/* 移动端菜单 */
.mobile-menu {
  position: fixed;
  top: 0;
  left: 0;
  width: 280px;
  height: 100vh;
  background: var(--bg-secondary);
  z-index: 101;
  padding: var(--space-md);
}

.mobile-menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: var(--space-md);
  border-bottom: 1px solid var(--border-color);
  margin-bottom: var(--space-md);
}

.menu-title {
  font-size: var(--font-size-lg);
  font-weight: bold;
  color: var(--text-primary);
}

.close-btn {
  background: transparent !important;
  border: none !important;
  color: var(--text-secondary) !important;
}

.mobile-nav {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.mobile-nav-link {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md);
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
}

.mobile-nav-link:hover,
.mobile-nav-link.router-link-active {
  background: var(--bg-tertiary);
  color: var(--color-primary);
}

/* 遮罩层 */
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  z-index: 100;
  backdrop-filter: blur(4px);
}

/* 动画 */
.slide-left-enter-active,
.slide-left-leave-active {
  transition: transform 0.3s ease;
}

.slide-left-enter-from,
.slide-left-leave-to {
  transform: translateX(-100%);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .search-box {
    max-width: 250px;
  }
}

@media (max-width: 768px) {
  .header-content {
    gap: var(--space-sm);
  }
  
  .logo-text {
    font-size: var(--font-size-md);
  }
  
  .user-actions {
    gap: var(--space-sm);
  }
}
</style>
