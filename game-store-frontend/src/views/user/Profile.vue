<template>
  <div class="profile-page">
    <div class="container">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1 class="page-title">
          <el-icon><UserFilled /></el-icon>
          个人中心
        </h1>
        <p class="page-desc">管理您的个人信息和账户设置</p>
      </div>

      <div class="profile-layout">
        <!-- 左侧导航 -->
        <aside class="profile-sidebar">
          <div class="user-card">
            <div class="avatar-wrapper" @click="showAvatarDialog = true">
              <el-avatar 
                :size="100" 
                :src="form.avatar || '/default-avatar.png'"
                class="user-avatar"
              />
              <div class="avatar-overlay">
                <el-icon><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </div>
            <h3 class="user-name">{{ form.username }}</h3>
            <p class="user-role">
              <el-tag :type="form.role === 1 ? 'danger' : 'info'" effect="dark">
                {{ form.role === 1 ? '管理员' : '普通用户' }}
              </el-tag>
            </p>
            <p class="join-time">加入时间：{{ formatDate(form.createTime) }}</p>
          </div>
          
          <nav class="profile-nav">
            <a 
              v-for="item in menuItems" 
              :key="item.key"
              class="nav-item"
              :class="{ active: activeTab === item.key }"
              @click="activeTab = item.key"
            >
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
              <el-icon class="arrow"><ArrowRight /></el-icon>
            </a>
          </nav>
        </aside>

        <!-- 右侧内容 -->
        <main class="profile-content">
          <!-- 基本信息 -->
          <section v-if="activeTab === 'info'" class="content-section fade-in">
            <div class="section-header">
              <h2>基本信息</h2>
              <p>更新您的个人资料</p>
            </div>
            
            <el-form 
              :model="form" 
              :rules="rules" 
              ref="infoForm"
              label-position="top"
              class="profile-form"
            >
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="用户名" prop="username">
                    <el-input v-model="form.username" disabled>
                      <template #prefix>
                        <el-icon><User /></el-icon>
                      </template>
                    </el-input>
                    <span class="form-tip">用户名不可修改</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="手机号" prop="phone">
                    <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11">
                      <template #prefix>
                        <el-icon><Phone /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="form.email" placeholder="请输入邮箱">
                  <template #prefix>
                    <el-icon><Message /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="个人简介" prop="bio">
                <el-input 
                  v-model="form.bio" 
                  type="textarea" 
                  :rows="4"
                  placeholder="介绍一下自己..."
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" size="large" :loading="saving" @click="saveProfile">
                  <el-icon><Check /></el-icon>
                  保存修改
                </el-button>
                <el-button size="large" @click="resetForm">
                  重置
                </el-button>
              </el-form-item>
            </el-form>
          </section>

          <!-- 安全设置 -->
          <section v-if="activeTab === 'security'" class="content-section fade-in">
            <div class="section-header">
              <h2>安全设置</h2>
              <p>修改密码保护账户安全</p>
            </div>
            
            <el-form 
              :model="passwordForm" 
              :rules="passwordRules" 
              ref="passwordFormRef"
              label-position="top"
              class="profile-form"
            >
              <el-form-item label="当前密码" prop="oldPassword">
                <el-input 
                  v-model="passwordForm.oldPassword" 
                  type="password"
                  placeholder="请输入当前密码"
                  show-password
                >
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="新密码" prop="newPassword">
                <el-input 
                  v-model="passwordForm.newPassword" 
                  type="password"
                  placeholder="请输入新密码（6-20个字符）"
                  show-password
                >
                  <template #prefix>
                    <el-icon><Key /></el-icon>
                  </template>
                </el-input>
                <div class="password-strength" v-if="passwordForm.newPassword">
                  <span>密码强度：</span>
                  <div class="strength-bar">
                    <div 
                      class="strength-fill" 
                      :class="passwordStrength"
                      :style="{ width: passwordStrengthWidth + '%' }"
                    ></div>
                  </div>
                  <span :class="passwordStrength">{{ passwordStrengthText }}</span>
                </div>
              </el-form-item>
              
              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input 
                  v-model="passwordForm.confirmPassword" 
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
                >
                  <template #prefix>
                    <el-icon><CircleCheck /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" size="large" :loading="changingPassword" @click="changePassword">
                  <el-icon><Lock /></el-icon>
                  修改密码
                </el-button>
              </el-form-item>
            </el-form>
          </section>

          <!-- 账户统计 -->
          <section v-if="activeTab === 'stats'" class="content-section fade-in">
            <div class="section-header">
              <h2>账户统计</h2>
              <p>查看您的账户数据</p>
            </div>
            
            <div class="stats-grid">
              <div class="stat-card">
                <div class="stat-icon blue">
                  <el-icon><ShoppingCart /></el-icon>
                </div>
                <div class="stat-info">
                  <span class="stat-value">{{ stats.orderCount }}</span>
                  <span class="stat-label">订单数量</span>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon green">
                  <el-icon><Coin /></el-icon>
                </div>
                <div class="stat-info">
                  <span class="stat-value">¥{{ stats.totalSpent }}</span>
                  <span class="stat-label">累计消费</span>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon orange">
                  <el-icon><Star /></el-icon>
                </div>
                <div class="stat-info">
                  <span class="stat-value">{{ stats.commentCount }}</span>
                  <span class="stat-label">评论数量</span>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon purple">
                  <el-icon><Calendar /></el-icon>
                </div>
                <div class="stat-info">
                  <span class="stat-value">{{ stats.daysJoined }}</span>
                  <span class="stat-label">加入天数</span>
                </div>
              </div>
            </div>
            
            <div class="recent-activity">
              <h3>最近活动</h3>
              <div v-if="activities.length > 0" class="activity-list">
                <div v-for="(activity, index) in activities" :key="index" class="activity-item">
                  <div class="activity-icon" :class="activity.type">
                    <el-icon><component :is="activity.icon" /></el-icon>
                  </div>
                  <div class="activity-content">
                    <p class="activity-text">{{ activity.text }}</p>
                    <span class="activity-time">{{ activity.time }}</span>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无活动记录" />
            </div>
          </section>
        </main>
      </div>
    </div>

    <!-- 头像上传弹窗 -->
    <el-dialog
      v-model="showAvatarDialog"
      title="更换头像"
      width="500px"
      class="avatar-dialog"
    >
      <div class="avatar-upload-content">
        <div class="avatar-preview">
          <el-avatar :size="150" :src="tempAvatar || form.avatar || '/default-avatar.png'" />
        </div>
        <image-upload v-model="tempAvatar" @change="onAvatarChange" />
        <p class="upload-tip">支持 JPG、PNG 格式，文件大小不超过 2MB</p>
      </div>
      <template #footer>
        <el-button @click="showAvatarDialog = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="saveAvatar">
          保存头像
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateUserInfo, updatePassword } from '../../api/user'
import { getOrderList } from '../../api/order'
import { getCommentList } from '../../api/comment'
import ImageUpload from '../../components/ImageUpload.vue'

export default {
  name: 'Profile',
  components: {
    ImageUpload
  },
  setup() {
    const store = useStore()
    const infoForm = ref(null)
    const passwordFormRef = ref(null)
    
    const activeTab = ref('info')
    const saving = ref(false)
    const changingPassword = ref(false)
    const showAvatarDialog = ref(false)
    const uploading = ref(false)
    const tempAvatar = ref('')
    
    const form = ref({
      username: '',
      phone: '',
      email: '',
      avatar: '',
      bio: '',
      role: 0,
      createTime: ''
    })
    
    const originalForm = ref({})
    
    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    const stats = ref({
      orderCount: 0,
      totalSpent: '0.00',
      commentCount: 0,
      daysJoined: 0
    })
    
    const activities = ref([])
    
    const menuItems = [
      { key: 'info', label: '基本信息', icon: 'User' },
      { key: 'security', label: '安全设置', icon: 'Lock' },
      { key: 'stats', label: '账户统计', icon: 'DataLine' }
    ]
    
    const rules = {
      phone: [
        { pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' }
      ],
      email: [
        { type: 'email', message: '邮箱格式错误', trigger: 'blur' }
      ]
    }
    
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    
    const passwordRules = {
      oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度应为 6-20 个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认新密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
      ]
    }
    
    // 密码强度计算
    const passwordStrength = computed(() => {
      const pwd = passwordForm.newPassword
      if (!pwd) return ''
      let score = 0
      if (pwd.length >= 8) score++
      if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) score++
      if (/\d/.test(pwd)) score++
      if (/[^a-zA-Z0-9]/.test(pwd)) score++
      
      if (score <= 1) return 'weak'
      if (score === 2) return 'medium'
      return 'strong'
    })
    
    const passwordStrengthWidth = computed(() => {
      const strength = passwordStrength.value
      if (strength === 'weak') return 33
      if (strength === 'medium') return 66
      if (strength === 'strong') return 100
      return 0
    })
    
    const passwordStrengthText = computed(() => {
      const strength = passwordStrength.value
      if (strength === 'weak') return '弱'
      if (strength === 'medium') return '中'
      if (strength === 'strong') return '强'
      return ''
    })
    
    onMounted(async () => {
      await loadUserInfo()
      await loadStats()
    })
    
    const loadUserInfo = async () => {
      try {
        const res = await getUserInfo()
        form.value = { ...form.value, ...res }
        originalForm.value = { ...res }
      } catch (error) {
        console.error('加载用户信息失败:', error)
      }
    }
    
    const loadStats = async () => {
      try {
        // 获取当前用户ID
        const userId = store.state.user?.id
        
        // 并行获取订单和评论数据
        const [orderRes, commentRes] = await Promise.all([
          getOrderList({ page: 1, size: 1000 }),
          getCommentList({ page: 1, size: 1000, userId })
        ])
        
        const orders = orderRes.records || []
        const comments = commentRes.records || []
        
        // 订单统计
        stats.value.orderCount = orders.length
        stats.value.totalSpent = orders
          .filter(o => o.status === 1)
          .reduce((sum, o) => sum + Number(o.totalAmount), 0)
          .toFixed(2)
        
        // 评论统计
        stats.value.commentCount = comments.length
        
        // 计算加入天数
        if (form.value.createTime) {
          const joinDate = new Date(form.value.createTime)
          const now = new Date()
          stats.value.daysJoined = Math.floor((now - joinDate) / (1000 * 60 * 60 * 24))
        }
        
        // 生成真实活动记录（合并订单和评论）
        const orderActivities = orders.slice(0, 3).map(o => ({
          type: o.status === 1 ? 'success' : 'warning',
          icon: o.status === 1 ? 'ShoppingCart' : 'Wallet',
          text: o.status === 1 ? `购买了订单 ${o.orderNo}` : `创建了订单 ${o.orderNo}`,
          time: o.createTime,
          sortTime: new Date(o.createTime).getTime()
        }))
        
        const commentActivities = comments.slice(0, 3).map(c => ({
          type: 'primary',
          icon: 'ChatDotRound',
          text: `评论了游戏《${c.gameName || '未知游戏'}》`,
          time: c.createTime,
          sortTime: new Date(c.createTime).getTime()
        }))
        
        // 合并并按时间倒序排列
        activities.value = [...orderActivities, ...commentActivities]
          .sort((a, b) => b.sortTime - a.sortTime)
          .slice(0, 5)
          .map(({ type, icon, text, time }) => ({ type, icon, text, time }))
      } catch (error) {
        console.error('加载统计失败:', error)
      }
    }
    
    const saveProfile = async () => {
      const valid = await infoForm.value.validate().catch(() => false)
      if (!valid) return
      
      saving.value = true
      try {
        const updateData = {
          phone: form.value.phone,
          email: form.value.email,
          avatar: form.value.avatar,
          bio: form.value.bio || ''  // 添加个人简介字段
        }
        const res = await updateUserInfo(updateData)
        ElMessage.success('保存成功')
        // 更新表单数据为返回的最新数据
        if (res) {
          form.value = { ...form.value, ...res }
          originalForm.value = { ...res }
        } else {
          originalForm.value = { ...form.value }
        }
        store.dispatch('getUserInfo')
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败：' + (error.message || '未知错误'))
      } finally {
        saving.value = false
      }
    }
    
    const resetForm = () => {
      form.value = { ...originalForm.value }
      ElMessage.info('已重置')
    }
    
    const changePassword = async () => {
      const valid = await passwordFormRef.value.validate().catch(() => false)
      if (!valid) return
      
      changingPassword.value = true
      try {
        await updatePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        ElMessage.success('密码修改成功，请重新登录')
        // 延迟1秒后退出登录并跳转
        setTimeout(() => {
          store.dispatch('logout')
          router.push('/login')
        }, 1500)
      } catch (error) {
        console.error('修改密码失败:', error)
        ElMessage.error('修改密码失败：' + (error.message || '请检查原密码是否正确'))
      } finally {
        changingPassword.value = false
      }
    }
    
    const onAvatarChange = (url) => {
      tempAvatar.value = url
    }
    
    const saveAvatar = async () => {
      if (!tempAvatar.value) {
        ElMessage.warning('请先上传头像')
        return
      }
      uploading.value = true
      try {
        form.value.avatar = tempAvatar.value
        await updateUserInfo({ avatar: tempAvatar.value })
        ElMessage.success('头像更新成功')
        showAvatarDialog.value = false
        store.dispatch('getUserInfo')
      } catch (error) {
        console.error('保存头像失败:', error)
      } finally {
        uploading.value = false
      }
    }
    
    const formatDate = (date) => {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN')
    }
    
    return {
      form,
      infoForm,
      passwordFormRef,
      activeTab,
      menuItems,
      rules,
      passwordRules,
      saving,
      changingPassword,
      showAvatarDialog,
      uploading,
      tempAvatar,
      passwordForm,
      stats,
      activities,
      passwordStrength,
      passwordStrengthWidth,
      passwordStrengthText,
      saveProfile,
      resetForm,
      changePassword,
      onAvatarChange,
      saveAvatar,
      formatDate
    }
  }
}
</script>

<style scoped>
.profile-page {
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

/* 布局 */
.profile-layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: var(--space-xl);
}

/* 左侧边栏 */
.profile-sidebar {
  position: sticky;
  top: 80px;
  height: fit-content;
}

.user-card {
  background: linear-gradient(160deg, rgba(30, 47, 64, 0.84) 0%, rgba(20, 31, 44, 0.96) 100%);
  border: 1px solid rgba(84, 123, 157, 0.35);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  text-align: center;
  margin-bottom: var(--space-lg);
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(8px);
}

.avatar-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto var(--space-md);
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
}

.user-avatar {
  border: 3px solid var(--border-color);
  transition: all var(--transition-normal);
}

.avatar-wrapper:hover .user-avatar {
  filter: brightness(0.7);
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-fast);
  color: white;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay .el-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.avatar-overlay span {
  font-size: 12px;
}

.user-name {
  font-size: var(--font-size-lg);
  color: var(--text-primary);
  margin-bottom: var(--space-sm);
}

.user-role {
  margin-bottom: var(--space-sm);
}

.join-time {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

/* 导航菜单 */
.profile-nav {
  background: linear-gradient(160deg, rgba(30, 47, 64, 0.84) 0%, rgba(20, 31, 44, 0.96) 100%);
  border: 1px solid rgba(84, 123, 157, 0.35);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-soft);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md) var(--space-lg);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
  border-left: 2px solid transparent;
  border-bottom: 1px solid rgba(70, 101, 129, 0.28);
}

.nav-item:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.nav-item.active {
  background: rgba(26, 159, 255, 0.1);
  color: var(--color-primary);
  border-left-color: var(--color-primary);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.12);
}

.nav-item .el-icon:first-child {
  font-size: 18px;
}

.nav-item span {
  flex: 1;
}

.nav-item .arrow {
  font-size: 12px;
  opacity: 0;
  transition: all var(--transition-fast);
}

.nav-item:hover .arrow,
.nav-item.active .arrow {
  opacity: 1;
  transform: translateX(4px);
}

/* 右侧内容 */
.profile-content {
  background: linear-gradient(160deg, rgba(30, 47, 64, 0.82) 0%, rgba(20, 31, 44, 0.96) 100%);
  border: 1px solid rgba(84, 123, 157, 0.35);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(8px);
}

.content-section {
  padding: var(--space-xl);
}

.section-header {
  margin-bottom: var(--space-xl);
  padding-bottom: var(--space-lg);
  border-bottom: 1px solid var(--border-color);
}

.section-header h2 {
  font-size: var(--font-size-xl);
  color: var(--text-primary);
  margin-bottom: var(--space-xs);
}

.section-header p {
  color: var(--text-muted);
  font-size: var(--font-size-sm);
}

/* 表单 */
.profile-form :deep(.el-input__wrapper) {
  background: var(--bg-tertiary) !important;
  box-shadow: 0 0 0 1px var(--border-color) inset !important;
}

.profile-form :deep(.el-input__inner) {
  color: var(--text-primary) !important;
}

.profile-form :deep(.el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 1px var(--color-primary) inset !important;
}

.profile-form :deep(.el-textarea__inner) {
  background: var(--bg-tertiary) !important;
  border-color: var(--border-color) !important;
  color: var(--text-primary) !important;
}

.profile-form :deep(.el-textarea__inner:focus) {
  border-color: var(--color-primary) !important;
}

.form-tip {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  margin-top: 4px;
  display: block;
}

/* 密码强度 */
.password-strength {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-top: var(--space-sm);
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

.strength-bar {
  width: 100px;
  height: 4px;
  background: var(--bg-tertiary);
  border-radius: 2px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  border-radius: 2px;
  transition: all var(--transition-fast);
}

.strength-fill.weak {
  background: var(--color-danger);
}

.strength-fill.medium {
  background: var(--color-warning);
}

.strength-fill.strong {
  background: var(--color-success);
}

.weak { color: var(--color-danger); }
.medium { color: var(--color-warning); }
.strong { color: var(--color-success); }

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-lg);
  margin-bottom: var(--space-xl);
}

.stat-card {
  background: rgba(22, 34, 48, 0.92);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--space-lg);
  display: flex;
  align-items: center;
  gap: var(--space-md);
  transition: transform var(--transition-fast), border-color var(--transition-fast);
}

.stat-card:hover {
  transform: translateY(-2px);
  border-color: rgba(102, 192, 244, 0.5);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.stat-icon.blue { background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%); }
.stat-icon.green { background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%); }
.stat-icon.orange { background: linear-gradient(135deg, #E6A23C 0%, #ebb563 100%); }
.stat-icon.purple { background: linear-gradient(135deg, #9254de 0%, #b37feb 100%); }

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: var(--text-primary);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

/* 最近活动 */
.recent-activity h3 {
  font-size: var(--font-size-lg);
  color: var(--text-primary);
  margin-bottom: var(--space-lg);
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-md);
  padding: var(--space-md);
  background: rgba(22, 34, 48, 0.92);
  border-radius: var(--radius-md);
  border: 1px solid rgba(70, 101, 129, 0.28);
}

.activity-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.activity-icon.success {
  background: rgba(103, 194, 58, 0.1);
  color: var(--color-success);
}

.activity-icon.warning {
  background: rgba(230, 162, 60, 0.1);
  color: var(--color-warning);
}

.activity-content {
  flex: 1;
}

.activity-text {
  color: var(--text-primary);
  margin-bottom: 4px;
}

.activity-time {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

/* 头像弹窗 */
.avatar-dialog :deep(.el-dialog) {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
}

.avatar-dialog :deep(.el-dialog__title) {
  color: var(--text-primary);
}

.avatar-upload-content {
  text-align: center;
  padding: var(--space-lg);
}

.avatar-preview {
  margin-bottom: var(--space-lg);
}

.avatar-preview :deep(.el-avatar) {
  border: 4px solid var(--border-color);
}

.upload-tip {
  margin-top: var(--space-md);
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

/* 动画 */
.fade-in {
  animation: fadeIn 0.3s ease-out;
}

/* 响应式 */
@media (max-width: 992px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }
  
  .profile-sidebar {
    position: static;
  }
  
  .user-card {
    display: flex;
    align-items: center;
    gap: var(--space-lg);
    text-align: left;
  }
  
  .avatar-wrapper {
    margin: 0;
  }
  
  .profile-nav {
    display: flex;
    overflow-x: auto;
  }
  
  .nav-item {
    white-space: nowrap;
    border-left: none;
    border-bottom: 3px solid transparent;
  }
  
  .nav-item.active {
    border-left-color: transparent;
    border-bottom-color: var(--color-primary);
  }
}

@media (max-width: 576px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .section-header h2 {
    font-size: var(--font-size-lg);
  }
}
</style>
