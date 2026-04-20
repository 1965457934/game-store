<template>
  <div class="home">
    <!-- Banner 轮播区 -->
    <section class="banner-section">
      <div class="container">
        <el-carousel 
          height="480px" 
          class="banner-carousel"
          :interval="5000"
          type="card"
          arrow="always"
        >
          <el-carousel-item v-for="banner in banners" :key="banner.id" @click="goToLink(banner.link)">
            <div class="banner-item">
              <img :src="banner.image" :alt="banner.title" class="banner-image">
              <div class="banner-overlay" @click.stop>
                <div class="banner-content">
                  <h2 class="banner-title">{{ banner.title }}</h2>
                  <p class="banner-desc">{{ banner.description }}</p>
                  <el-button type="primary" size="large" class="banner-btn" @click.stop="goToLink(banner.link)">
                    立即查看 <el-icon class="btn-icon"><ArrowRight /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </section>

    <!-- 热门游戏区 -->
    <section class="section hot-games">
      <div class="container">
        <div class="section-header">
          <div class="section-title">
            <el-icon class="title-icon"><Hot /></el-icon>
            <h2>热门游戏</h2>
          </div>
          <router-link to="/games" class="view-all">
            查看全部 <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>
        
        <div class="games-grid">
          <div 
            v-for="(game, index) in topGames" 
            :key="game.id" 
            class="game-card"
            :class="{ 'fade-in-up': true }"
            :style="{ animationDelay: `${index * 0.1}s` }"
            @click="goToDetail(game.id)"
          >
            <div class="card-image-wrapper img-zoom">
              <img :src="game.cover || '/default-game.png'" :alt="game.name" class="game-cover">
              <div class="card-overlay">
                <div class="overlay-content">
                  <el-button type="primary" circle class="view-btn">
                    <el-icon><View /></el-icon>
                  </el-button>
                </div>
              </div>
              <div class="game-tags">
                <span v-if="game.isHot" class="tag tag-hot">热门</span>
                <span v-if="game.isNew" class="tag tag-new">新品</span>
                <span v-if="game.discount" class="tag tag-discount">-{{ game.discount }}%</span>
              </div>
            </div>
            <div class="card-info">
              <h3 class="game-name text-ellipsis">{{ game.name }}</h3>
              <p class="game-category">{{ game.categoryName }}</p>
              <div class="game-price">
                <template v-if="game.discount">
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
      </div>
    </section>

    <!-- 新品推荐区 -->
    <section class="section new-games" v-if="newGames.length > 0">
      <div class="container">
        <div class="section-header">
          <div class="section-title">
            <el-icon class="title-icon"><StarFilled /></el-icon>
            <h2>新品推荐</h2>
          </div>
          <router-link to="/games" class="view-all">
            查看全部 <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>
        
        <div class="games-grid compact">
          <div 
            v-for="(game, index) in newGames" 
            :key="game.id" 
            class="game-card compact"
            @click="goToDetail(game.id)"
          >
            <div class="card-image-wrapper img-zoom">
              <img :src="game.cover || '/default-game.png'" :alt="game.name" class="game-cover">
              <div class="card-overlay">
                <el-icon class="view-icon"><View /></el-icon>
              </div>
              <span class="tag tag-new">新品</span>
            </div>
            <div class="card-info">
              <h3 class="game-name text-ellipsis">{{ game.name }}</h3>
              <span class="current-price">¥{{ game.price }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 特色展示 -->
    <section class="section features">
      <div class="container">
        <div class="features-grid">
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><Lock /></el-icon>
            </div>
            <h3>正品保障</h3>
            <p>官方授权，100% 正版游戏</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><Promotion /></el-icon>
            </div>
            <h3>极速发货</h3>
            <p>自动发货，即刻畅玩</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><Ticket /></el-icon>
            </div>
            <h3>超值优惠</h3>
            <p>定期折扣，省钱省心</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBannerList } from '../api/banner'
import { getTopGames, getNewGames } from '../api/game'

export default {
  name: 'Home',
  setup() {
    const router = useRouter()
    const banners = ref([])
    const topGames = ref([])
    const newGames = ref([])

    onMounted(async () => {
      await Promise.all([
        loadBanners(),
        loadTopGames(),
        loadNewGames()
      ])
    })

    const loadBanners = async () => {
      try {
        const res = await getBannerList()
        banners.value = res || []
      } catch (error) {
        console.error('加载轮播图失败:', error)
      }
    }

    const loadTopGames = async () => {
      try {
        const res = await getTopGames(6)
        topGames.value = res || []
      } catch (error) {
        console.error('加载热门游戏失败:', error)
      }
    }

    const loadNewGames = async () => {
      try {
        const res = await getNewGames(6)
        newGames.value = res || []
      } catch (error) {
        console.error('加载新品游戏失败:', error)
      }
    }

    const goToLink = (link) => {
      console.log('轮播图点击，link值:', link, '类型:', typeof link)
      
      // 处理link可能是数字的情况
      if (typeof link === 'number') {
        link = String(link)
      }
      
      if (!link || link === 'null' || link === 'undefined') {
        ElMessage.info('该轮播图未设置链接')
        return
      }
      
      // 确保link是字符串
      link = String(link).trim()
      
      // 外部链接
      if (link.startsWith('http')) {
        window.open(link, '_blank')
      } 
      // 游戏详情页 /game/xxx
      else if (link.startsWith('/game/')) {
        router.push(link)
      }
      // 其他内部路径
      else if (link.startsWith('/')) {
        router.push(link)
      }
      // 纯数字作为游戏ID处理
      else if (/^\d+$/.test(link)) {
        router.push(`/game/${link}`)
      }
      // 默认当作路径处理
      else {
        router.push(link)
      }
    }

    const goToDetail = (id) => {
      router.push(`/game/${id}`)
    }

    return {
      banners,
      topGames,
      newGames,
      goToLink,
      goToDetail
    }
  }
}
</script>

<style scoped>
.home {
  padding-bottom: var(--space-xl);
  position: relative;
}

/* Banner 轮播区 */
.banner-section {
  padding: var(--space-xl) 0;
  background: linear-gradient(180deg, rgba(17, 24, 33, 0.2) 0%, rgba(17, 24, 33, 0.55) 100%);
}

.banner-carousel {
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
}

.banner-carousel :deep(.el-carousel__container) {
  border-radius: var(--radius-lg);
}

.banner-item {
  position: relative;
  width: 100%;
  height: 100%;
  cursor: pointer;
  overflow: hidden;
  border-radius: var(--radius-lg);
  z-index: 1;
}

.banner-item::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 2;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.banner-item:hover .banner-image {
  transform: scale(1.03);
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to top,
    rgba(23, 26, 33, 0.95) 0%,
    rgba(23, 26, 33, 0.6) 40%,
    transparent 100%
  );
  display: flex;
  align-items: flex-end;
  padding: var(--space-xl);
  pointer-events: none; /* 允许点击穿透到下层 */
}

.banner-content {
  max-width: 600px;
  pointer-events: auto; /* 恢复内容区域的点击 */
}

.banner-content {
  max-width: 600px;
}

.banner-title {
  font-size: var(--font-size-xxl);
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: var(--space-sm);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.banner-desc {
  font-size: var(--font-size-md);
  color: var(--text-secondary);
  margin-bottom: var(--space-md);
  line-height: 1.6;
}

.banner-btn {
  padding: 12px 28px;
  font-size: var(--font-size-md);
  border-radius: var(--radius-md);
}

.btn-icon {
  margin-left: var(--space-xs);
  transition: transform var(--transition-fast);
}

.banner-btn:hover .btn-icon {
  transform: translateX(4px);
}

/* 轮播图指示器样式 */
.banner-carousel :deep(.el-carousel__indicators) {
  bottom: 20px;
}

.banner-carousel :deep(.el-carousel__button) {
  width: 40px;
  height: 4px;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.3);
}

.banner-carousel :deep(.el-carousel__indicator.is-active .el-carousel__button) {
  background: var(--color-primary);
}

/* 通用区块样式 */
.section {
  padding: var(--space-xxl) 0;
}

.hot-games,
.new-games {
  position: relative;
}

.hot-games::before,
.new-games::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(180deg, rgba(26, 159, 255, 0.04) 0%, transparent 25%);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-xl);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.title-icon {
  font-size: 28px;
  color: var(--color-primary);
}

.section-title h2 {
  font-size: var(--font-size-xl);
  font-weight: bold;
  color: var(--text-primary);
}

.view-all {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  color: var(--text-secondary);
  text-decoration: none;
  font-size: var(--font-size-sm);
  transition: all var(--transition-fast);
}

.view-all:hover {
  color: var(--color-primary);
  gap: var(--space-sm);
}

/* 游戏网格 */
.games-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-lg);
}

.games-grid.compact {
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: var(--space-md);
}

/* 游戏卡片 */
.game-card {
  background: var(--gradient-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-normal);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-soft);
}

.game-card:hover {
  transform: translateY(-8px);
  border-color: var(--color-primary);
  box-shadow: var(--shadow-card-hover);
}

.game-card.compact {
  border-radius: var(--radius-md);
}

.game-card.compact:hover {
  transform: translateY(-4px);
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
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.game-card:hover .card-overlay {
  opacity: 1;
}

.overlay-content {
  transform: translateY(20px);
  transition: transform var(--transition-normal);
}

.game-card:hover .overlay-content {
  transform: translateY(0);
}

.view-btn {
  width: 48px;
  height: 48px;
  font-size: 20px;
}

.view-icon {
  font-size: 32px;
  color: white;
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
  background: linear-gradient(180deg, rgba(11, 18, 28, 0) 0%, rgba(11, 18, 28, 0.5) 100%);
}

.game-name {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-xs);
}

.game-category {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  margin-bottom: var(--space-sm);
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

/* 特色展示 */
.features {
  background: linear-gradient(180deg, rgba(27, 40, 56, 0.72) 0%, rgba(21, 31, 43, 0.92) 100%);
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--space-xl);
}

.feature-item {
  text-align: center;
  padding: var(--space-lg);
  border: 1px solid rgba(61, 106, 142, 0.45);
  border-radius: var(--radius-lg);
  background: rgba(17, 25, 36, 0.4);
  backdrop-filter: blur(8px);
}

.feature-icon {
  width: 64px;
  height: 64px;
  background: var(--gradient-primary);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--space-md);
  font-size: 28px;
  color: white;
}

.feature-item h3 {
  font-size: var(--font-size-md);
  color: var(--text-primary);
  margin-bottom: var(--space-xs);
}

.feature-item p {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

/* 动画 */
.fade-in-up {
  opacity: 0;
  animation: fadeInUp 0.6s ease-out forwards;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .banner-section {
    padding: var(--space-md) 0;
  }
  
  .banner-title {
    font-size: var(--font-size-xl);
  }
  
  .banner-desc {
    font-size: var(--font-size-sm);
  }
  
  .games-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }
}

@media (max-width: 576px) {
  .banner-carousel {
    height: 300px !important;
  }
  
  .banner-carousel :deep(.el-carousel__container) {
    height: 300px !important;
  }
  
  .banner-overlay {
    padding: var(--space-md);
  }
  
  .banner-title {
    font-size: var(--font-size-lg);
  }
  
  .games-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-sm);
  }
  
  .games-grid.compact {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .card-info {
    padding: var(--space-sm);
  }
  
  .game-name {
    font-size: var(--font-size-sm);
  }
  
  .current-price {
    font-size: var(--font-size-md);
  }
  
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-md);
  }
}
</style>
