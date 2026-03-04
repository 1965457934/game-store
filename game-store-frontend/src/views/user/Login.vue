<template>
  <div class="auth-page">
    <!-- 背景动画 -->
    <div class="bg-animation">
      <div class="bg-gradient"></div>
      <div class="particles">
        <span v-for="i in 20" :key="i" class="particle"></span>
      </div>
    </div>
    
    <div class="auth-container">
      <!-- Logo区域 -->
      <div class="auth-brand" @click="$router.push('/')">
        <el-icon size="48" class="brand-icon"><GameController /></el-icon>
        <h1 class="brand-title">游戏商城</h1>
        <p class="brand-slogan">发现精彩游戏，享受极致体验</p>
      </div>
      
      <!-- 登录卡片 -->
      <el-card class="auth-card" :class="{ 'shake': shakeForm }">
        <div class="card-header">
          <h2>欢迎回来</h2>
          <p>登录您的账户继续</p>
        </div>
        
        <el-form 
          :model="form" 
          :rules="rules" 
          ref="loginForm" 
          class="auth-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名"
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码"
              size="large"
              show-password
              clearable
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-link type="primary" @click="forgotPassword">忘记密码？</el-link>
          </div>
          
          <el-form-item>
            <el-button 
              type="primary" 
              size="large" 
              :loading="loading"
              @click="handleLogin"
              class="submit-btn"
            >
              <el-icon v-if="!loading"><Right /></el-icon>
              <span>立即登录</span>
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="divider">
          <span>或</span>
        </div>
        
        <div class="social-login">
          <el-button circle class="social-btn" @click="socialLogin('wechat')">
            <el-icon><ChatDotRound /></el-icon>
          </el-button>
          <el-button circle class="social-btn" @click="socialLogin('qq')">
            <el-icon><ChatLineRound /></el-icon>
          </el-button>
          <el-button circle class="social-btn" @click="socialLogin('github')">
            <el-icon><Platform /></el-icon>
          </el-button>
        </div>
        
        <div class="auth-footer">
          <span>还没有账号？</span>
          <el-link type="primary" @click="$router.push('/register')">
            立即注册
            <el-icon><ArrowRight /></el-icon>
          </el-link>
        </div>
      </el-card>
    </div>
    
    <!-- 页脚 -->
    <footer class="auth-footer-bar">
      <p>© 2026 游戏商城 Game Store. All rights reserved.</p>
    </footer>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/user'

export default {
  name: 'Login',
  setup() {
    const store = useStore()
    const router = useRouter()
    const loginForm = ref(null)
    const loading = ref(false)
    const rememberMe = ref(false)
    const shakeForm = ref(false)

    const form = reactive({
      username: '',
      password: ''
    })

    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度应为 3-20 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度应为 6-20 个字符', trigger: 'blur' }
      ]
    }

    onMounted(() => {
      // 检查是否有记住的用户名
      const savedUsername = localStorage.getItem('rememberUsername')
      if (savedUsername) {
        form.username = savedUsername
        rememberMe.value = true
      }
    })

    const handleLogin = async () => {
      const valid = await loginForm.value.validate().catch(() => false)
      if (!valid) {
        shakeForm.value = true
        setTimeout(() => shakeForm.value = false, 500)
        return
      }

      loading.value = true
      try {
        const res = await login(form)
        store.dispatch('login', res)
        
        // 记住用户名
        if (rememberMe.value) {
          localStorage.setItem('rememberUsername', form.username)
        } else {
          localStorage.removeItem('rememberUsername')
        }
        
        ElMessage.success({
          message: '登录成功，欢迎回来！',
          duration: 2000
        })
        
        // 跳转到首页或之前访问的页面
        const redirect = router.currentRoute.value.query.redirect || '/'
        router.push(redirect)
      } catch (e) {
        shakeForm.value = true
        setTimeout(() => shakeForm.value = false, 500)
        console.log('登录失败:', e.message)
      } finally {
        loading.value = false
      }
    }

    const forgotPassword = () => {
      ElMessage.info('请联系管理员重置密码')
    }

    const socialLogin = (type) => {
      const typeNames = { wechat: '微信', qq: 'QQ', github: 'GitHub' }
      ElMessage.info(`${typeNames[type]}登录功能即将上线`)
    }

    return {
      form,
      rules,
      loginForm,
      loading,
      rememberMe,
      shakeForm,
      handleLogin,
      forgotPassword,
      socialLogin
    }
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

/* 背景动画 */
.bg-animation {
  position: fixed;
  inset: 0;
  z-index: 0;
  overflow: hidden;
}

.bg-gradient {
  position: absolute;
  inset: 0;
  background: 
    radial-gradient(ellipse at 20% 20%, rgba(26, 159, 255, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 80%, rgba(102, 192, 244, 0.1) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 50%, rgba(23, 26, 33, 1) 0%, rgba(27, 40, 56, 1) 100%);
}

.particles {
  position: absolute;
  inset: 0;
}

.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: var(--color-primary);
  border-radius: 50%;
  opacity: 0.3;
  animation: float 15s infinite;
}

.particle:nth-child(1) { left: 10%; top: 20%; animation-delay: 0s; }
.particle:nth-child(2) { left: 20%; top: 80%; animation-delay: 1s; }
.particle:nth-child(3) { left: 30%; top: 40%; animation-delay: 2s; }
.particle:nth-child(4) { left: 40%; top: 60%; animation-delay: 3s; }
.particle:nth-child(5) { left: 50%; top: 10%; animation-delay: 4s; }
.particle:nth-child(6) { left: 60%; top: 90%; animation-delay: 5s; }
.particle:nth-child(7) { left: 70%; top: 30%; animation-delay: 6s; }
.particle:nth-child(8) { left: 80%; top: 70%; animation-delay: 7s; }
.particle:nth-child(9) { left: 90%; top: 50%; animation-delay: 8s; }
.particle:nth-child(10) { left: 15%; top: 90%; animation-delay: 9s; }
.particle:nth-child(11) { left: 25%; top: 10%; animation-delay: 10s; }
.particle:nth-child(12) { left: 35%; top: 85%; animation-delay: 11s; }
.particle:nth-child(13) { left: 45%; top: 25%; animation-delay: 12s; }
.particle:nth-child(14) { left: 55%; top: 75%; animation-delay: 13s; }
.particle:nth-child(15) { left: 65%; top: 15%; animation-delay: 14s; }
.particle:nth-child(16) { left: 75%; top: 95%; animation-delay: 0.5s; }
.particle:nth-child(17) { left: 85%; top: 35%; animation-delay: 1.5s; }
.particle:nth-child(18) { left: 5%; top: 65%; animation-delay: 2.5s; }
.particle:nth-child(19) { left: 95%; top: 45%; animation-delay: 3.5s; }
.particle:nth-child(20) { left: 50%; top: 50%; animation-delay: 4.5s; }

@keyframes float {
  0%, 100% {
    transform: translateY(0) translateX(0);
    opacity: 0.3;
  }
  25% {
    transform: translateY(-20px) translateX(10px);
    opacity: 0.6;
  }
  50% {
    transform: translateY(10px) translateX(-10px);
    opacity: 0.3;
  }
  75% {
    transform: translateY(-10px) translateX(5px);
    opacity: 0.5;
  }
}

/* 主容器 */
.auth-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-xl);
  position: relative;
  z-index: 1;
}

/* 品牌区域 */
.auth-brand {
  text-align: center;
  margin-bottom: var(--space-xl);
  cursor: pointer;
  transition: transform var(--transition-normal);
}

.auth-brand:hover {
  transform: scale(1.02);
}

.brand-icon {
  color: var(--color-primary);
  margin-bottom: var(--space-md);
  animation: pulse 2s ease-in-out infinite;
}

.brand-title {
  font-size: 32px;
  font-weight: bold;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: var(--space-sm);
}

.brand-slogan {
  color: var(--text-muted);
  font-size: var(--font-size-sm);
}

/* 卡片 */
.auth-card {
  width: 100%;
  max-width: 420px;
  background: linear-gradient(160deg, rgba(30, 47, 64, 0.88) 0%, rgba(20, 31, 44, 0.96) 100%) !important;
  border: 1px solid rgba(84, 123, 157, 0.4) !important;
  border-radius: var(--radius-xl) !important;
  backdrop-filter: blur(12px);
  box-shadow: var(--shadow-soft);
}

.auth-card :deep(.el-card__body) {
  padding: var(--space-xl);
}

.card-header {
  text-align: center;
  margin-bottom: var(--space-xl);
}

.card-header h2 {
  font-size: var(--font-size-xl);
  color: var(--text-primary);
  margin-bottom: var(--space-xs);
}

.card-header p {
  color: var(--text-muted);
  font-size: var(--font-size-sm);
}

/* 表单 */
.auth-form :deep(.el-input__wrapper) {
  background: var(--bg-tertiary) !important;
  box-shadow: 0 0 0 1px var(--border-color) inset !important;
  border-radius: var(--radius-md) !important;
  padding: 4px 16px !important;
  transition: box-shadow var(--transition-fast), transform var(--transition-fast);
}

.auth-form :deep(.el-input__inner) {
  color: var(--text-primary) !important;
  height: 44px;
}

.auth-form :deep(.el-input__prefix) {
  color: var(--text-muted);
  margin-right: var(--space-sm);
}

.auth-form :deep(.el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 1px var(--color-primary) inset, 0 0 0 4px rgba(26, 159, 255, 0.12) !important;
}

.auth-form :deep(.el-input__wrapper:focus-within .el-input__prefix) {
  color: var(--color-primary);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-lg);
}

.form-options :deep(.el-checkbox__label) {
  color: var(--text-secondary);
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: var(--font-size-md);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-sm);
  box-shadow: var(--shadow-glow);
}

.submit-btn .el-icon {
  transition: transform var(--transition-fast);
}

.submit-btn:hover .el-icon {
  transform: translateX(4px);
}

.submit-btn:hover {
  transform: translateY(-1px);
}

/* 分隔线 */
.divider {
  display: flex;
  align-items: center;
  margin: var(--space-lg) 0;
  color: var(--text-muted);
  font-size: var(--font-size-sm);
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--border-color);
}

.divider span {
  padding: 0 var(--space-md);
}

/* 社交登录 */
.social-login {
  display: flex;
  justify-content: center;
  gap: var(--space-md);
  margin-bottom: var(--space-lg);
}

.social-btn {
  width: 44px;
  height: 44px;
  background: var(--bg-tertiary) !important;
  border-color: var(--border-color) !important;
  color: var(--text-secondary) !important;
  font-size: 20px;
  transition: all var(--transition-fast);
}

.social-btn:hover {
  border-color: var(--color-primary) !important;
  color: var(--color-primary) !important;
  transform: translateY(-2px);
}

/* 底部链接 */
.auth-footer {
  text-align: center;
  color: var(--text-muted);
  font-size: var(--font-size-sm);
}

.auth-footer .el-link {
  font-size: var(--font-size-sm);
  margin-left: var(--space-xs);
}

.auth-footer .el-link .el-icon {
  margin-left: 2px;
  transition: transform var(--transition-fast);
}

.auth-footer .el-link:hover .el-icon {
  transform: translateX(4px);
}

/* 抖动动画 */
.shake {
  animation: shake 0.5s ease-in-out;
}

/* 页脚 */
.auth-footer-bar {
  text-align: center;
  padding: var(--space-md);
  color: var(--text-muted);
  font-size: var(--font-size-xs);
  position: relative;
  z-index: 1;
}

/* 响应式 */
@media (max-width: 576px) {
  .auth-container {
    padding: var(--space-md);
  }
  
  .brand-title {
    font-size: 24px;
  }
  
  .auth-card {
    max-width: 100%;
  }
  
  .auth-card :deep(.el-card__body) {
    padding: var(--space-lg);
  }
}
</style>
