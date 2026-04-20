<template>
  <div class="game-detail-page">
    <div v-if="game.id" class="container">
      <!-- 面包屑导航 -->
      <el-breadcrumb class="breadcrumb">
        <el-breadcrumb-item to="/">首页</el-breadcrumb-item>
        <el-breadcrumb-item to="/games">游戏库</el-breadcrumb-item>
        <el-breadcrumb-item class="current">{{ game.name }}</el-breadcrumb-item>
      </el-breadcrumb>

      <!-- 游戏详情主区域 -->
      <div class="game-main">
        <div class="game-media">
          <div class="cover-wrapper">
            <img :src="game.cover || '/default-game.png'" :alt="game.name" class="cover-image">
            <div v-if="game.discount > 0" class="discount-ribbon">
              -{{ game.discount }}%
            </div>
          </div>
          
          <!-- 媒体缩略图 -->
          <div v-if="game.screenshots && game.screenshots.length > 0" class="media-thumbnails">
            <div 
              v-for="(img, index) in game.screenshots" 
              :key="index"
              class="thumbnail"
              :class="{ active: currentImage === img }"
              @click="currentImage = img"
            >
              <img :src="img" :alt="`${game.name} 截图 ${index + 1}`">
            </div>
          </div>
        </div>

        <div class="game-info">
          <div class="info-header">
            <h1 class="game-title">{{ game.name }}</h1>
            <div class="game-tags">
              <span class="category-tag">{{ game.categoryName }}</span>
              <span v-if="game.isHot" class="tag tag-hot">热门</span>
              <span v-if="game.isNew" class="tag tag-new">新品</span>
            </div>
          </div>

          <div class="rating-section">
            <template v-if="game.rating && game.rating > 0">
              <el-rate v-model="game.rating" disabled allow-half show-score />
            </template>
            <template v-else>
              <span class="no-rating">暂无评分</span>
            </template>
            <span class="sales-count">已售 {{ game.sales || 0 }} 份</span>
          </div>

          <div class="price-section">
            <div v-if="game.discount > 0" class="discount-info">
              <span class="discount-badge">-{{ game.discount }}%</span>
              <div class="price-comparison">
                <span class="original-price">原价 ¥{{ game.originalPrice }}</span>
                <span class="current-price discount">¥{{ game.price }}</span>
              </div>
            </div>
            <div v-else class="price-info">
              <span class="current-price">¥{{ game.price }}</span>
            </div>
          </div>

          <div class="action-buttons">
            <el-button 
              type="primary" 
              size="large" 
              class="buy-btn"
              @click="buyNow"
            >
              <el-icon><ShoppingCart /></el-icon>
              立即购买
            </el-button>
            <el-button 
              size="large" 
              class="cart-btn"
              @click="addToCartHandler"
            >
              <el-icon><Plus /></el-icon>
              加入购物车
            </el-button>
          </div>

          <div class="game-meta">
            <div class="meta-item">
              <span class="meta-label">发行日期</span>
              <span class="meta-value">{{ game.releaseDate || '2024年' }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">开发商</span>
              <span class="meta-value">{{ game.developer || '未知' }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">平台</span>
              <span class="meta-value platforms">
                <el-icon><Monitor /></el-icon>
                <el-icon><Apple /></el-icon>
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 游戏详情内容 -->
      <div class="game-content">
        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="游戏介绍" name="description">
            <div class="content-section">
              <h2 class="section-title">关于这款游戏</h2>
              <div class="description-content" v-html="game.description || '暂无游戏介绍'"></div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="系统需求" name="requirements">
            <div class="content-section">
              <h2 class="section-title">系统需求</h2>
              <div class="requirements-content" v-html="game.requirements || '暂无系统需求信息'"></div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 评论区 -->
      <div class="comments-section">
        <div class="section-header">
          <h2 class="section-title">
            <el-icon><ChatDotRound /></el-icon>
            玩家评价
            <span class="comment-count">({{ comments.length }})</span>
          </h2>
          <div class="rating-summary">
            <div class="average-rating">
              <span class="rating-number">{{ averageRating }}</span>
              <el-rate v-model="averageRating" disabled allow-half />
            </div>
          </div>
        </div>

        <!-- 评论输入 -->
        <div v-if="isLoggedIn" class="comment-form">
          <!-- 用户已评论且不在编辑模式：显示已评论提示 -->
          <div v-if="userComment && !isEditing" class="user-comment-info">
            <div class="my-review-card">
              <div class="my-review-header">
                <el-avatar :size="40" :src="user.avatar || '/default-avatar.png'" />
                <div class="my-review-meta">
                  <span class="my-review-title">我的评价</span>
                  <el-rate v-model="userComment.rating" disabled allow-half size="small" />
                </div>
              </div>
              <p class="my-review-content">{{ userComment.content }}</p>
              <div class="my-review-actions">
                <el-button type="primary" size="small" @click="startEditComment">
                  <el-icon><Edit /></el-icon>修改评价
                </el-button>
                <el-button type="danger" size="small" @click="handleDeleteComment">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </div>
            </div>
          </div>
          
          <!-- 编辑模式或未评论时：显示评论表单 -->
          <template v-else>
            <div class="form-header">
              <el-avatar :size="48" :src="user.avatar || '/default-avatar.png'" />
              <div class="rating-input">
                <span>评分：</span>
                <el-rate 
                  v-model="commentRating" 
                  allow-half 
                  :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                  @change="(val) => console.log('评分改变:', val)"
                />
                <span class="rating-value">{{ commentRating }} 分</span>
              </div>
            </div>
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="4"
              :placeholder="isEditing ? '修改您的评价...' : '分享你的游戏体验...'"
              maxlength="500"
              show-word-limit
              class="comment-input"
            />
            <div class="form-actions">
              <el-button v-if="isEditing" @click="cancelEdit">取消</el-button>
              <el-button type="primary" @click="submitComment" :disabled="!commentContent.trim()">
                {{ isEditing ? '保存修改' : '发表评论' }}
              </el-button>
            </div>
          </template>
        </div>

        <div v-else class="login-tip">
          <el-icon><InfoFilled /></el-icon>
          <p>登录后可以发表评论</p>
          <el-button type="primary" @click="$router.push('/login')">立即登录</el-button>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item" :class="{ 'my-comment': isOwnComment(comment) }">
            <div class="comment-avatar">
              <el-avatar :size="48" :src="comment.avatar || '/default-avatar.png'" />
            </div>
            <div class="comment-content">
              <div class="comment-header">
                <span class="username">
                  {{ comment.username }}
                  <el-tag v-if="isOwnComment(comment)" size="small" type="primary" class="my-tag">我</el-tag>
                </span>
                <el-rate v-model="comment.rating" disabled allow-half size="small" />
                <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
              </div>
              <p class="comment-text">{{ comment.content }}</p>
              <div class="comment-actions">
                <el-button link size="small" @click="likeComment(comment)">
                  <el-icon><Thumb /></el-icon>
                  {{ comment.likes || 0 }}
                </el-button>
                <!-- 自己的评论显示编辑和删除按钮（如果在列表中显示） -->
                <template v-if="isOwnComment(comment) && !userComment">
                  <el-button link size="small" type="primary" @click="startEditComment">
                    <el-icon><Edit /></el-icon>修改
                  </el-button>
                  <el-button link size="small" type="danger" @click="handleDeleteComment">
                    <el-icon><Delete /></el-icon>删除
                  </el-button>
                </template>
              </div>
            </div>
          </div>
        </div>

        <!-- 加载更多 -->
        <div v-if="comments.length >= 10" class="load-more">
          <el-button link>加载更多评论</el-button>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else class="error-container">
      <el-icon class="error-icon"><CircleCloseFilled /></el-icon>
      <p>游戏加载失败</p>
      <el-button type="primary" @click="loadGame">重试</el-button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGameDetail } from '../../api/game'
import { addToCart } from '../../api/cart'
import { getCommentList, addComment, updateComment, deleteComment, getUserComment } from '../../api/comment'

export default {
  name: 'GameDetail',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()
    
    const game = ref({})
    const comments = ref([])
    const commentContent = ref('')
    const commentRating = ref(5)
    const activeTab = ref('description')
    const currentImage = ref('')
    const loading = ref(false)
    const userComment = ref(null)  // 用户当前游戏的评论
    const isEditing = ref(false)   // 是否正在编辑评论

    const isLoggedIn = computed(() => store.state.token)
    const user = computed(() => store.state.user || {})
    
    // 判断用户是否可以评论（已购买但未评论，或者是修改自己的评论）
    const canComment = computed(() => {
      if (!isLoggedIn.value) return false
      // 如果已有评论且不在编辑模式，则不能发表新评论
      if (userComment.value && !isEditing.value) return false
      return true
    })
    
    // 使用游戏详情中的评分，确保与头部显示一致
    const averageRating = computed(() => {
      // 优先使用 game.rating，如果没有则计算评论平均值
      if (game.value.rating !== undefined && game.value.rating !== null) {
        return Number(game.value.rating).toFixed(1)
      }
      if (comments.value.length === 0) return 0
      const sum = comments.value.reduce((acc, c) => acc + (c.rating || 5), 0)
      return (sum / comments.value.length).toFixed(1)
    })

    onMounted(() => {
      loadGame()
    })

    const loadGame = async () => {
      loading.value = true
      try {
        const res = await getGameDetail(route.params.id)
        game.value = res || {}
        currentImage.value = game.value.cover
        await loadComments()
      } catch (error) {
        console.error('加载游戏详情失败:', error)
      } finally {
        loading.value = false
      }
    }

    const loadComments = async () => {
      try {
        const res = await getCommentList({ gameId: route.params.id })
        comments.value = res.records || []
        
        // 如果已登录，检查用户是否已评论
        if (isLoggedIn.value) {
          await checkUserComment()
        }
      } catch (error) {
        console.error('加载评论失败:', error)
      }
    }
    
    // 检查用户是否已评论过该游戏
    const checkUserComment = async () => {
      try {
        const res = await getUserComment(route.params.id)
        if (res) {
          userComment.value = res
        } else {
          userComment.value = null
        }
      } catch (error) {
        console.error('检查用户评论失败:', error)
        userComment.value = null
      }
    }

    const addToCartHandler = async () => {
      if (!isLoggedIn.value) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
      }
      try {
        await addToCart({ gameId: game.value.id, quantity: 1 })
        ElMessage.success('已加入购物车')
        store.dispatch('getCartCount')
      } catch (error) {
        console.error('加入购物车失败:', error)
      }
    }

    const buyNow = async () => {
      await addToCartHandler()
      router.push('/cart')
    }

    const submitComment = async () => {
      if (!commentContent.value.trim()) {
        ElMessage.warning('请输入评论内容')
        return
      }
      // 检查用户登录状态
      if (!isLoggedIn.value) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
      }
      // 检查游戏ID
      if (!game.value.id) {
        ElMessage.error('游戏信息加载失败')
        return
      }
      try {
        // 确保评分精度正确，使用一位小数
        // 使用 toFixed 确保是 0.5 的倍数，然后转为数字
        let rating = Number(commentRating.value)
        // 确保在 0.5-5.0 范围内，并且是 0.5 的倍数
        rating = Math.max(0.5, Math.min(5.0, rating))
        rating = Math.round(rating * 2) / 2
        
        console.log('提交评分:', commentRating.value, '处理后:', rating)
        
        if (isEditing.value && userComment.value) {
          // 修改评论
          const params = {
            gameId: game.value.id,
            content: commentContent.value,
            rating: rating
          }
          await updateComment(userComment.value.id, params)
          ElMessage.success('评论修改成功')
          isEditing.value = false
        } else {
          // 新增评论
          const params = {
            gameId: game.value.id,
            content: commentContent.value,
            rating: rating
          }
          await addComment(params)
          ElMessage.success('评论成功')
        }
        
        commentContent.value = ''
        commentRating.value = 5
        await loadComments()
        // 重新加载游戏详情以更新评分
        await loadGame()
      } catch (error) {
        // 错误提示已由 request.js 响应拦截器统一处理，此处不再重复弹出
        console.error('发表评论失败:', error)
      }
    }
    
    // 开始编辑评论
    const startEditComment = () => {
      if (userComment.value) {
        commentContent.value = userComment.value.content
        // 确保评分是数字类型
        commentRating.value = Number(userComment.value.rating) || 5
        console.log('开始编辑评论，评分:', commentRating.value)
        isEditing.value = true
      }
    }
    
    // 取消编辑
    const cancelEdit = () => {
      isEditing.value = false
      commentContent.value = ''
      commentRating.value = 5
    }
    
    // 删除评论
    const handleDeleteComment = async () => {
      if (!userComment.value) return
      
      try {
        await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await deleteComment(userComment.value.id)
        ElMessage.success('评论删除成功')
        userComment.value = null
        cancelEdit()
        await loadComments()
        await loadGame()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除评论失败:', error)
          ElMessage.error(error.response?.data?.message || '删除评论失败')
        }
      }
    }
    
    // 判断评论是否属于当前用户
    const isOwnComment = (comment) => {
      return comment.userId === user.value.id
    }

    const likeComment = (comment) => {
      comment.likes = (comment.likes || 0) + 1
      ElMessage.success('点赞成功')
    }

    const formatTime = (time) => {
      if (!time) return ''
      const date = new Date(time)
      return date.toLocaleDateString('zh-CN')
    }

    return {
      game,
      comments,
      commentContent,
      commentRating,
      activeTab,
      currentImage,
      loading,
      userComment,
      isEditing,
      isLoggedIn,
      user,
      canComment,
      averageRating,
      loadGame,
      addToCartHandler,
      buyNow,
      submitComment,
      startEditComment,
      cancelEdit,
      handleDeleteComment,
      isOwnComment,
      likeComment,
      formatTime
    }
  }
}
</script>

<style scoped>
.game-detail-page {
  padding: var(--space-lg) 0 var(--space-xxl);
}

/* 面包屑导航 */
.breadcrumb {
  margin-bottom: var(--space-lg);
}

.breadcrumb :deep(.el-breadcrumb__item) {
  color: var(--text-muted);
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  color: var(--text-secondary);
  transition: color var(--transition-fast);
}

.breadcrumb :deep(.el-breadcrumb__inner:hover) {
  color: var(--color-primary);
}

.breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--text-primary);
  font-weight: 600;
}

/* 游戏主区域 */
.game-main {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: var(--space-xl);
  margin-bottom: var(--space-xl);
  background: linear-gradient(160deg, rgba(20, 31, 45, 0.6) 0%, rgba(14, 23, 34, 0.5) 100%);
  border: 1px solid rgba(61, 106, 142, 0.35);
  border-radius: var(--radius-xl);
  padding: var(--space-lg);
  box-shadow: var(--shadow-soft);
}

/* 媒体区 */
.game-media {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.cover-wrapper {
  position: relative;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
}

.cover-image {
  width: 100%;
  aspect-ratio: 16 / 9;
  object-fit: cover;
}

.discount-ribbon {
  position: absolute;
  top: var(--space-md);
  right: -40px;
  background: var(--color-success);
  color: white;
  padding: 8px 50px;
  font-size: var(--font-size-lg);
  font-weight: bold;
  transform: rotate(45deg);
  box-shadow: var(--shadow-md);
}

.media-thumbnails {
  display: flex;
  gap: var(--space-sm);
  overflow-x: auto;
  padding-bottom: var(--space-sm);
}

.thumbnail {
  flex-shrink: 0;
  width: 120px;
  height: 68px;
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all var(--transition-fast);
}

.thumbnail.active,
.thumbnail:hover {
  border-color: var(--color-primary);
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 游戏信息区 */
.game-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.info-header {
  border-bottom: 1px solid var(--border-color);
  padding-bottom: var(--space-md);
}

.game-title {
  font-size: var(--font-size-xxl);
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: var(--space-md);
  line-height: 1.3;
}

.game-tags {
  display: flex;
  gap: var(--space-sm);
  flex-wrap: wrap;
}

.category-tag {
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  padding: 4px 12px;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
}

/* 评分区 */
.rating-section {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.rating-section :deep(.el-rate__text) {
  color: var(--text-secondary);
  margin-left: var(--space-sm);
}

.sales-count {
  color: var(--text-muted);
  font-size: var(--font-size-sm);
}

.no-rating {
  color: var(--text-muted);
  font-size: var(--font-size-sm);
}

/* 价格区 */
.price-section {
  background: linear-gradient(160deg, rgba(30, 49, 66, 0.82) 0%, rgba(21, 34, 47, 0.94) 100%);
  padding: var(--space-lg);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-soft);
}

.discount-info {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}

.discount-badge {
  background: var(--color-success);
  color: white;
  padding: 12px 16px;
  border-radius: var(--radius-md);
  font-size: 28px;
  font-weight: bold;
}

.price-comparison {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.original-price {
  font-size: var(--font-size-md);
  color: var(--text-muted);
  text-decoration: line-through;
}

.current-price {
  font-size: 42px;
  font-weight: bold;
  color: #a4d007;
}

.current-price.discount {
  color: #a4d007;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: var(--space-md);
}

.buy-btn {
  flex: 1;
  height: 56px;
  font-size: var(--font-size-lg);
  border-radius: var(--radius-md);
}

.cart-btn {
  height: 56px;
  padding: 0 24px;
  background: var(--bg-tertiary);
  border-color: var(--border-color);
  color: var(--text-primary);
  border-radius: var(--radius-md);
}

.cart-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.wishlist-btn {
  width: 56px;
  height: 56px;
  background: var(--bg-tertiary);
  border-color: var(--border-color);
  color: var(--text-muted);
  font-size: 20px;
}

.wishlist-btn:hover,
.wishlist-btn.active {
  border-color: #f39c12;
  color: #f39c12;
}

/* 元信息 */
.game-meta {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding-top: var(--space-md);
  border-top: 1px solid var(--border-color);
}

.meta-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.meta-label {
  color: var(--text-muted);
  font-size: var(--font-size-sm);
}

.meta-value {
  color: var(--text-primary);
  font-weight: 500;
}

.platforms {
  display: flex;
  gap: var(--space-sm);
}

.platforms .el-icon {
  font-size: 20px;
  color: var(--text-secondary);
}

/* 游戏内容区 */
.game-content {
  margin-bottom: var(--space-xxl);
}

.detail-tabs :deep(.el-tabs__header) {
  border-bottom: 1px solid var(--border-color);
  margin-bottom: var(--space-lg);
}

.detail-tabs :deep(.el-tabs__nav-wrap::after) {
  background: var(--border-color);
}

.detail-tabs :deep(.el-tabs__item) {
  color: var(--text-secondary);
  font-size: var(--font-size-md);
  padding: var(--space-md) var(--space-lg);
}

.detail-tabs :deep(.el-tabs__item.is-active) {
  color: var(--color-primary);
}

.detail-tabs :deep(.el-tabs__active-bar) {
  background: var(--color-primary);
  height: 3px;
}

.content-section {
  background: var(--gradient-surface);
  padding: var(--space-xl);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-soft);
}

.content-section .section-title {
  font-size: var(--font-size-lg);
  color: var(--text-primary);
  margin-bottom: var(--space-lg);
  padding-bottom: var(--space-md);
  border-bottom: 1px solid var(--border-color);
}

.description-content,
.requirements-content {
  color: var(--text-secondary);
  line-height: 1.8;
  font-size: var(--font-size-md);
}

.description-content :deep(p) {
  margin-bottom: var(--space-md);
}

/* 评论区 */
.comments-section {
  background: var(--gradient-surface);
  padding: var(--space-xl);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-soft);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-xl);
  padding-bottom: var(--space-lg);
  border-bottom: 1px solid var(--border-color);
}

.comments-section .section-title {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  font-size: var(--font-size-lg);
  color: var(--text-primary);
  margin: 0;
}

.comments-section .section-title .el-icon {
  color: var(--color-primary);
}

.comment-count {
  color: var(--text-muted);
  font-weight: normal;
}

.rating-summary {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.average-rating {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.rating-number {
  font-size: 36px;
  font-weight: bold;
  color: #f39c12;
}

/* 用户已评论信息 */
.user-comment-info {
  margin-bottom: var(--space-lg);
}

/* 我的评价卡片 */
.my-review-card {
  background: linear-gradient(160deg, rgba(30, 47, 64, 0.6) 0%, rgba(20, 31, 44, 0.8) 100%);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--space-lg);
  transition: all var(--transition-normal);
}

.my-review-card:hover {
  border-color: rgba(26, 159, 255, 0.4);
  box-shadow: 0 4px 20px rgba(26, 159, 255, 0.1);
}

.my-review-header {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  margin-bottom: var(--space-md);
}

.my-review-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.my-review-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
}

.my-review-content {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  line-height: 1.7;
  margin-bottom: var(--space-md);
  padding: var(--space-sm) 0;
  border-bottom: 1px solid var(--border-color);
}

.my-review-actions {
  display: flex;
  gap: var(--space-sm);
}

/* 评论表单 */
.comment-form {
  background: linear-gradient(160deg, rgba(28, 43, 58, 0.76) 0%, rgba(21, 31, 45, 0.92) 100%);
  padding: var(--space-lg);
  border-radius: var(--radius-lg);
  margin-bottom: var(--space-xl);
  border: 1px solid rgba(61, 106, 142, 0.35);
}

.form-header {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  margin-bottom: var(--space-md);
}

.rating-input {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  color: var(--text-secondary);
}

.rating-value {
  margin-left: var(--space-sm);
  font-weight: 500;
  color: #f39c12;
}

.comment-input :deep(.el-textarea__inner) {
  background: var(--bg-tertiary);
  border-color: var(--border-color);
  color: var(--text-primary);
}

.comment-input :deep(.el-textarea__inner:focus) {
  border-color: var(--color-primary);
}

.comment-input :deep(.el-input__count) {
  color: var(--text-muted);
  background: transparent;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-md);
}

/* 登录提示 */
.login-tip {
  text-align: center;
  padding: var(--space-xl);
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  margin-bottom: var(--space-xl);
}

.login-tip .el-icon {
  font-size: 48px;
  color: var(--text-muted);
  margin-bottom: var(--space-md);
}

.login-tip p {
  color: var(--text-secondary);
  margin-bottom: var(--space-md);
}

/* 评论列表 */
.comment-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.comment-item {
  display: flex;
  gap: var(--space-md);
  padding-bottom: var(--space-lg);
  border-bottom: 1px solid var(--border-color);
}

.comment-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  margin-bottom: var(--space-sm);
  flex-wrap: wrap;
}

.username {
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: var(--space-xs);
}

.my-tag {
  font-size: 10px;
  padding: 0 4px;
  height: 18px;
}

.comment-item.my-comment {
  background: rgba(26, 159, 255, 0.05);
  border-radius: var(--radius-md);
  padding: var(--space-md);
  margin: 0 calc(-1 * var(--space-md));
  border: 1px solid rgba(102, 192, 244, 0.3);
}

.comment-time {
  color: var(--text-muted);
  font-size: var(--font-size-xs);
}

.comment-text {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: var(--space-sm);
}

.comment-actions {
  display: flex;
  gap: var(--space-md);
}

.comment-actions .el-button {
  color: var(--text-muted);
}

.comment-actions .el-button:hover {
  color: var(--color-primary);
}

.load-more {
  text-align: center;
  padding-top: var(--space-lg);
}

/* 加载状态 */
.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  gap: var(--space-lg);
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid var(--border-color);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-container p,
.error-container p {
  color: var(--text-muted);
}

.error-icon {
  font-size: 64px;
  color: var(--color-danger);
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 响应式调整 */
@media (max-width: 992px) {
  .game-main {
    grid-template-columns: 1fr;
    padding: var(--space-md);
  }
  
  .game-title {
    font-size: var(--font-size-xl);
  }
  
  .discount-badge {
    font-size: var(--font-size-lg);
    padding: 8px 12px;
  }
  
  .current-price {
    font-size: 32px;
  }
}

@media (max-width: 576px) {
  .action-buttons {
    flex-direction: column;
  }
  
  .buy-btn,
  .cart-btn {
    width: 100%;
  }
  
  .wishlist-btn {
    width: 100%;
    height: 48px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-md);
  }
  
  .comment-header {
    gap: var(--space-sm);
  }
}
</style>
