<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <el-icon size="28" class="logo-icon"><Setting /></el-icon>
          <span class="logo-text" v-show="!sidebarCollapsed">管理后台</span>
        </div>
        <el-button 
          circle 
          class="collapse-btn"
          @click="sidebarCollapsed = !sidebarCollapsed"
        >
          <el-icon>
            <Fold v-if="!sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
        </el-button>
      </div>
      
      <nav class="sidebar-menu">
        <router-link 
          v-for="item in menuItems" 
          :key="item.path"
          :to="item.path"
          class="menu-item"
          :class="{ active: $route.path === item.path }"
          :title="sidebarCollapsed ? item.label : ''"
        >
          <el-icon class="menu-icon"><component :is="item.icon" /></el-icon>
          <span class="menu-label" v-show="!sidebarCollapsed">{{ item.label }}</span>
          <el-icon v-show="!sidebarCollapsed" class="menu-arrow"><ArrowRight /></el-icon>
        </router-link>
      </nav>
      
      <div class="sidebar-footer" v-show="!sidebarCollapsed">
        <el-button class="back-btn" @click="$router.push('/')">
          <el-icon><Back /></el-icon>
          返回前台
        </el-button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 顶部栏 -->
      <header class="top-bar">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="top-actions">
          <el-tooltip content="全屏" placement="bottom">
            <el-button circle class="action-btn" @click="toggleFullscreen">
              <el-icon><FullScreen /></el-icon>
            </el-button>
          </el-tooltip>
          
          <el-dropdown @command="handleUserCommand" trigger="click">
            <div class="user-menu">
              <el-avatar :size="36" :src="user.avatar || '/default-avatar.png'" />
              <span class="username" v-if="!isMobile">{{ user.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="admin-dropdown">
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="home">
                  <el-icon><HomeFilled /></el-icon>返回前台
                </el-dropdown-item>
                <el-dropdown-item command="logout" class="logout-item">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 页面内容 -->
      <div class="page-content">
        <router-view />
      </div>
    </main>


  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'AdminLayout',
  setup() {
    const store = useStore()
    const router = useRouter()
    const route = useRoute()
    
    const sidebarCollapsed = ref(false)
    const isMobile = ref(false)
    
    const user = computed(() => store.state.user)
    
    const menuItems = [
      { path: '/admin/dashboard', label: '数据概览', icon: 'DataLine' },
      { path: '/admin/users', label: '用户管理', icon: 'User' },
      { path: '/admin/games', label: '游戏管理', icon: 'GameController' },
      { path: '/admin/categories', label: '分类管理', icon: 'Folder' },
      { path: '/admin/orders', label: '订单管理', icon: 'ShoppingCart' },
      { path: '/admin/comments', label: '评论管理', icon: 'ChatDotRound' },
      { path: '/admin/banners', label: '轮播图管理', icon: 'Picture' }
    ]
    
    const currentPageTitle = computed(() => {
      const item = menuItems.find(item => item.path === route.path)
      return item ? item.label : '管理后台'
    })
    
    // 响应式处理
    const handleResize = () => {
      isMobile.value = window.innerWidth <= 768
      if (isMobile.value) {
        sidebarCollapsed.value = true
      }
    }
    
    onMounted(() => {
      handleResize()
      window.addEventListener('resize', handleResize)
    })
    
    onUnmounted(() => {
      window.removeEventListener('resize', handleResize)
    })
    
    const toggleFullscreen = () => {
      if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen()
      } else {
        document.exitFullscreen()
      }
    }
    
    const handleUserCommand = (command) => {
      switch (command) {
        case 'profile':
          router.push('/profile')
          break
        case 'home':
          router.push('/')
          break
        case 'logout':
          store.dispatch('logout')
          ElMessage.success('退出成功')
          router.push('/login')
          break
      }
    }
    
    return {
      sidebarCollapsed,
      isMobile,
      user,
      menuItems,
      currentPageTitle,
      toggleFullscreen,
      handleUserCommand
    }
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(180deg, #111821 0%, #171a21 45%, #121921 100%);
}

/* 侧边栏 */
.sidebar {
  width: 250px;
  background: linear-gradient(180deg, rgba(26, 39, 55, 0.94) 0%, rgba(20, 31, 44, 0.98) 100%);
  border-right: 1px solid rgba(84, 123, 157, 0.42);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
  box-shadow: 10px 0 24px rgba(0, 0, 0, 0.24);
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-md);
  border-bottom: 1px solid var(--border-color);
  height: 64px;
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  overflow: hidden;
}

.logo-icon {
  color: var(--color-primary);
  flex-shrink: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: var(--text-primary);
  white-space: nowrap;
}

.collapse-btn {
  background: transparent !important;
  border: none !important;
  color: var(--text-muted) !important;
  flex-shrink: 0;
}

.collapse-btn:hover {
  color: var(--color-primary) !important;
}

/* 菜单 */
.sidebar-menu {
  flex: 1;
  padding: var(--space-md) 0;
  overflow-y: auto;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: var(--space-md) var(--space-lg);
  color: var(--text-secondary);
  text-decoration: none;
  transition: all var(--transition-fast);
  border-left: 2px solid transparent;
  margin: 0 var(--space-sm);
  border-radius: var(--radius-md);
  border: 1px solid transparent;
}

.menu-item:hover {
  background: rgba(26, 159, 255, 0.1);
  color: var(--text-primary);
  border-color: rgba(102, 192, 244, 0.4);
}

.menu-item.active {
  background: rgba(26, 159, 255, 0.1);
  color: var(--color-primary);
  border-left-color: transparent;
  border-color: rgba(102, 192, 244, 0.45);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

.menu-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.menu-label {
  flex: 1;
  margin-left: var(--space-md);
  white-space: nowrap;
}

.menu-arrow {
  font-size: 12px;
  opacity: 0;
  transition: all var(--transition-fast);
}

.menu-item:hover .menu-arrow {
  opacity: 1;
  transform: translateX(4px);
}

/* 侧边栏底部 */
.sidebar-footer {
  padding: var(--space-md);
  border-top: 1px solid var(--border-color);
}

.back-btn {
  width: 100%;
  background: var(--bg-tertiary) !important;
  border-color: var(--border-color) !important;
  color: var(--text-secondary) !important;
}

.back-btn:hover {
  border-color: var(--color-primary) !important;
  color: var(--color-primary) !important;
}

/* 主内容区 */
.main-content {
  flex: 1;
  margin-left: 250px;
  display: flex;
  flex-direction: column;
  transition: margin-left 0.3s ease;
}

.sidebar.collapsed + .main-content {
  margin-left: 64px;
}

/* 顶部栏 */
.top-bar {
  height: 64px;
  background: rgba(27, 40, 56, 0.82);
  border-bottom: 1px solid rgba(84, 123, 157, 0.38);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-xl);
  position: sticky;
  top: 0;
  z-index: 99;
  backdrop-filter: blur(12px);
}

.breadcrumb :deep(.el-breadcrumb__item) {
  color: var(--text-muted);
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  color: var(--text-secondary);
}

.breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--text-primary);
  font-weight: 500;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.action-btn {
  background: var(--bg-tertiary) !important;
  border: none !important;
  color: var(--text-secondary) !important;
}

.action-btn:hover {
  background: var(--color-primary) !important;
  color: white !important;
}

.notice-badge :deep(.el-badge__content) {
  background: var(--color-danger);
  border: none;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.user-menu:hover {
  background: var(--bg-tertiary);
}

.username {
  color: var(--text-primary);
  font-weight: 500;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-menu .el-icon {
  color: var(--text-muted);
  font-size: 12px;
}

/* 下拉菜单 */
.admin-dropdown :deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-sm) var(--space-md);
}

.admin-dropdown :deep(.el-dropdown-menu__item:hover) {
  background: var(--bg-tertiary) !important;
  color: var(--color-primary) !important;
}

.logout-item {
  color: var(--color-danger) !important;
}

/* 页面内容 */
.page-content {
  flex: 1;
  padding: var(--space-xl);
  overflow-y: auto;
  background: linear-gradient(180deg, rgba(15, 22, 32, 0.35) 0%, rgba(15, 22, 32, 0.1) 100%);
}

/* 通知弹窗 */
.notification-dialog :deep(.el-dialog) {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
}

.notification-dialog :deep(.el-dialog__title) {
  color: var(--text-primary);
}

.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  gap: var(--space-md);
  padding: var(--space-md);
  border-bottom: 1px solid var(--border-color);
}

.notification-item:last-child {
  border-bottom: none;
}

.notice-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.notice-icon.warning {
  background: rgba(230, 162, 60, 0.1);
  color: var(--color-warning);
}

.notice-icon.success {
  background: rgba(103, 194, 58, 0.1);
  color: var(--color-success);
}

.notice-icon.info {
  background: rgba(144, 147, 153, 0.1);
  color: var(--text-muted);
}

.notice-content {
  flex: 1;
}

.notice-title {
  color: var(--text-primary);
  font-weight: 500;
  margin-bottom: 4px;
}

.notice-desc {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  margin-bottom: 4px;
}

.notice-time {
  color: var(--text-muted);
  font-size: var(--font-size-xs);
}

/* 响应式 */
@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
  }
  
  .sidebar.open {
    transform: translateX(0);
  }
  
  .main-content {
    margin-left: 0 !important;
  }
  
  .top-bar {
    padding: 0 var(--space-md);
  }
  
  .page-content {
    padding: var(--space-md);
  }
}
</style>
