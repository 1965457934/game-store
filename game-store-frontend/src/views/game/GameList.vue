<template>
  <div class="game-list-page">
    <div class="container">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1 class="page-title">
          <el-icon><Grid /></el-icon>
          游戏库
        </h1>
        <p class="page-desc">发现精彩游戏，开启无限冒险</p>
      </div>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <div class="search-box">
          <el-input
            v-model="keyword"
            placeholder="搜索游戏名称..."
            clearable
            @keyup.enter="handleSearch"
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button @click="handleSearch" class="search-btn">
                搜索
              </el-button>
            </template>
          </el-input>
        </div>
        
        <div class="filter-options">
          <el-select v-model="selectedCategory" placeholder="全部分类" clearable @change="handleSearch" class="filter-select">
            <el-option 
              v-for="cat in categories" 
              :key="cat.id" 
              :label="cat.name" 
              :value="cat.id"
            />
          </el-select>
          
          <el-select v-model="sortBy" placeholder="默认排序" @change="handleSearch" class="filter-select">
            <el-option label="默认排序" value="default" />
            <el-option label="价格从低到高" value="price_asc" />
            <el-option label="价格从高到低" value="price_desc" />
            <el-option label="最新发布" value="newest" />
            <el-option label="销量最高" value="sales" />
          </el-select>
        </div>
      </div>

      <!-- 游戏网格 -->
      <div v-if="games.length > 0" class="games-grid">
        <div 
          v-for="(game, index) in games" 
          :key="game.id" 
          class="game-card"
          :class="{ 'fade-in-up': true }"
          :style="{ animationDelay: `${(index % 12) * 0.05}s` }"
          @click="goToDetail(game.id)"
        >
          <div class="card-image-wrapper img-zoom">
            <img :src="game.cover || '/default-game.png'" :alt="game.name" class="game-cover">
            <div class="card-overlay">
              <el-button type="primary" class="view-btn">
                <el-icon><View /></el-icon>
                查看详情
              </el-button>
            </div>
            <div class="game-tags">
              <span v-if="game.isHot" class="tag tag-hot">热门</span>
              <span v-if="game.isNew" class="tag tag-new">新品</span>
              <span v-if="game.discount > 0" class="tag tag-discount">-{{ game.discount }}%</span>
            </div>
          </div>
          <div class="card-info">
            <h3 class="game-name text-ellipsis-2">{{ game.name }}</h3>
            <p class="game-category">{{ game.categoryName }}</p>
            <div class="game-meta">
              <el-rate v-if="game.rating" v-model="game.rating" disabled size="small" />
              <span class="sales">已售 {{ game.sales || 0 }}</span>
            </div>
            <div class="game-price">
              <template v-if="game.discount > 0">
                <span class="discount-badge">-{{ game.discount }}%</span>
                <div class="price-wrapper">
                  <span class="original-price">¥{{ game.originalPrice }}</span>
                  <span class="current-price">¥{{ game.price }}</span>
                </div>
              </template>
              <template v-else>
                <span class="current-price">¥{{ game.price }}</span>
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading" class="empty-state">
        <el-icon class="empty-icon"><Box /></el-icon>
        <p>暂无相关游戏</p>
        <el-button type="primary" @click="resetFilter">清除筛选</el-button>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div v-for="i in 12" :key="i" class="game-card skeleton-card">
          <div class="skeleton-image skeleton"></div>
          <div class="skeleton-content">
            <div class="skeleton-line skeleton" style="width: 80%"></div>
            <div class="skeleton-line skeleton" style="width: 60%"></div>
            <div class="skeleton-line skeleton" style="width: 40%"></div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="games.length > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[12, 24, 36]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadGames"
          @size-change="handleSizeChange"
          class="pagination"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getGameList } from '../../api/game'
import { getCategoryList } from '../../api/category'

export default {
  name: 'GameList',
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    const games = ref([])
    const categories = ref([])
    const keyword = ref('')
    const selectedCategory = ref('')
    const sortBy = ref('default')
    const page = ref(1)
    const size = ref(12)
    const total = ref(0)
    const loading = ref(false)

    onMounted(() => {
      // 从 URL 参数恢复筛选状态
      if (route.query.keyword) {
        keyword.value = route.query.keyword
      }
      if (route.query.category) {
        selectedCategory.value = Number(route.query.category)
      }
      if (route.query.sort) {
        sortBy.value = route.query.sort
      }
      
      loadGames()
      loadCategories()
    })

    // 监听路由参数变化
    watch(() => route.query, (newQuery) => {
      if (newQuery.keyword !== undefined && newQuery.keyword !== keyword.value) {
        keyword.value = newQuery.keyword
        page.value = 1
        loadGames()
      }
    })

    const loadGames = async () => {
      loading.value = true
      try {
        const params = {
          page: page.value,
          size: size.value,
          keyword: keyword.value,
          categoryId: selectedCategory.value,
          sortBy: sortBy.value
        }
        const res = await getGameList(params)
        games.value = res.records || []
        total.value = res.total || 0
      } catch (error) {
        console.error('加载游戏列表失败:', error)
      } finally {
        loading.value = false
      }
    }

    const loadCategories = async () => {
      try {
        const res = await getCategoryList()
        categories.value = res || []
      } catch (error) {
        console.error('加载分类失败:', error)
      }
    }

    const handleSearch = () => {
      page.value = 1
      loadGames()
      // 更新 URL 参数
      const query = {}
      if (keyword.value) query.keyword = keyword.value
      if (selectedCategory.value) query.category = selectedCategory.value
      if (sortBy.value && sortBy.value !== 'default') query.sort = sortBy.value
      router.replace({ query })
    }

    const handleSizeChange = (newSize) => {
      size.value = newSize
      page.value = 1
      loadGames()
    }

    const resetFilter = () => {
      keyword.value = ''
      selectedCategory.value = ''
      sortBy.value = 'default'
      page.value = 1
      loadGames()
      router.replace({ query: {} })
    }

    const goToDetail = (id) => {
      router.push(`/game/${id}`)
    }

    return {
      games,
      categories,
      keyword,
      selectedCategory,
      sortBy,
      page,
      size,
      total,
      loading,
      loadGames,
      handleSearch,
      handleSizeChange,
      resetFilter,
      goToDetail
    }
  }
}
</script>

<style scoped>
.game-list-page {
  padding: var(--space-xl) 0;
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

.page-desc {
  color: var(--text-muted);
  font-size: var(--font-size-md);
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-md);
  margin-bottom: var(--space-xl);
  padding: var(--space-md);
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  flex-wrap: wrap;
}

.search-box {
  flex: 1;
  min-width: 300px;
}

.search-input :deep(.el-input__wrapper) {
  background: var(--bg-tertiary) !important;
  border-radius: var(--radius-md) !important;
}

.search-input :deep(.el-input-group__append) {
  background: var(--color-primary) !important;
  border-color: var(--color-primary) !important;
  padding: 0;
}

.search-btn {
  background: transparent !important;
  border: none !important;
  color: white !important;
  padding: 0 20px;
}

.filter-options {
  display: flex;
  gap: var(--space-md);
  flex-wrap: wrap;
}

.filter-select {
  width: 160px;
}

.filter-select :deep(.el-input__wrapper) {
  background: var(--bg-tertiary) !important;
}

/* 游戏网格 */
.games-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-lg);
  margin-bottom: var(--space-xl);
}

.game-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-normal);
  border: 1px solid var(--border-color);
}

.game-card:hover {
  transform: translateY(-8px);
  border-color: var(--color-primary);
  box-shadow: var(--shadow-card-hover);
}

.card-image-wrapper {
  position: relative;
  aspect-ratio: 460 / 215;
  overflow: hidden;
}

.game-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.game-card:hover .card-overlay {
  opacity: 1;
}

.view-btn {
  transform: translateY(20px);
  transition: transform var(--transition-normal);
}

.game-card:hover .view-btn {
  transform: translateY(0);
}

.game-tags {
  position: absolute;
  top: var(--space-sm);
  right: var(--space-sm);
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

/* 卡片信息 */
.card-info {
  padding: var(--space-md);
}

.game-name {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-xs);
  line-height: 1.4;
  min-height: 44px;
}

.game-category {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  margin-bottom: var(--space-sm);
}

.game-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-sm);
}

.game-meta :deep(.el-rate) {
  height: auto;
}

.sales {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.game-price {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.discount-badge {
  background: var(--color-success);
  color: white;
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  font-weight: bold;
}

.price-wrapper {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.original-price {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  text-decoration: line-through;
}

.current-price {
  font-size: var(--font-size-lg);
  font-weight: bold;
  color: #a4d007;
}

/* 骨架屏 */
.loading-state {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-lg);
}

.skeleton-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.skeleton-image {
  aspect-ratio: 460 / 215;
}

.skeleton-content {
  padding: var(--space-md);
}

.skeleton-line {
  height: 16px;
  margin-bottom: var(--space-sm);
  border-radius: var(--radius-sm);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: var(--space-xxl);
}

.empty-icon {
  font-size: 80px;
  color: var(--text-muted);
  margin-bottom: var(--space-md);
}

.empty-state p {
  color: var(--text-muted);
  margin-bottom: var(--space-lg);
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding-top: var(--space-xl);
  border-top: 1px solid var(--border-color);
}

.pagination {
  color: var(--text-secondary);
}

/* 动画 */
.fade-in-up {
  opacity: 0;
  animation: fadeInUp 0.5s ease-out forwards;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-box {
    min-width: auto;
  }
  
  .filter-options {
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: var(--font-size-xl);
  }
  
  .games-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-md);
  }
  
  .card-info {
    padding: var(--space-sm);
  }
  
  .game-name {
    font-size: var(--font-size-sm);
    min-height: 36px;
  }
  
  .current-price {
    font-size: var(--font-size-md);
  }
}

@media (max-width: 576px) {
  .filter-options {
    width: 100%;
  }
  
  .filter-select {
    flex: 1;
    width: auto;
  }
  
  .games-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-sm);
  }
}
</style>
