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
        <p class="brand-slogan">创建账户，开启游戏之旅</p>
      </div>
      
      <!-- 注册卡片 -->
      <el-card class="auth-card" :class="{ 'shake': shakeForm }">
        <div class="card-header">
          <h2>创建账户</h2>
          <p>填写以下信息完成注册</p>
        </div>
        
        <el-form 
          :model="form" 
          :rules="rules" 
          ref="registerForm" 
          class="auth-form"
          @keyup.enter="handleRegister"
        >
          <el-form-item prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名（3-20个字符）"
              size="large"
              clearable
              maxlength="20"
              show-word-limit
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
              placeholder="请输入密码（6-20个字符）"
              size="large"
              show-password
              clearable
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="form.confirmPassword" 
              type="password" 
              placeholder="请再次输入密码"
              size="large"
              show-password
              clearable
            >
              <template #prefix>
                <el-icon><Key /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="phone">
            <el-input 
              v-model="form.phone" 
              placeholder="请输入手机号"
              size="large"
              clearable
              maxlength="11"
            >
              <template #prefix>
                <el-icon><Phone /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="email">
            <el-input 
              v-model="form.email" 
              placeholder="请输入邮箱（可选）"
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="agreement">
            <el-checkbox v-model="form.agreement">
              <span class="agreement-text">
                我已阅读并同意
                <el-link type="primary" @click.stop="showTerms">用户协议</el-link>
                和
                <el-link type="primary" @click.stop="showPrivacy">隐私政策</el-link>
              </span>
            </el-checkbox>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              size="large" 
              :loading="loading"
              @click="handleRegister"
              class="submit-btn"
            >
              <el-icon v-if="!loading"><CircleCheck /></el-icon>
              <span>立即注册</span>
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="auth-footer">
          <span>已有账号？</span>
          <el-link type="primary" @click="$router.push('/login')">
            立即登录
            <el-icon><ArrowRight /></el-icon>
          </el-link>
        </div>
      </el-card>
    </div>
    
    <!-- 页脚 -->
    <footer class="auth-footer-bar">
      <p>© 2026 游戏商城 Game Store. All rights reserved.</p>
    </footer>
    
    <!-- 协议弹窗 -->
    <el-dialog
      v-model="termsVisible"
      title="用户协议"
      width="600px"
      class="terms-dialog"
    >
      <div class="terms-content">
        <h3>欢迎使用游戏商城</h3>
        <p>1. 用户注册时需要提供真实有效的信息。</p>
        <p>2. 用户应当妥善保管自己的账户密码。</p>
        <p>3. 禁止利用本商城进行任何违法违规活动。</p>
        <p>4. 本商城保留对违规账户进行处理的权利。</p>
        <p>5. 用户购买的游戏仅可用于个人娱乐，不得转售。</p>
        <p>6. 如遇问题请联系客服处理。</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="termsVisible = false">我知道了</el-button>
      </template>
    </el-dialog>
    
    <!-- 隐私政策弹窗 -->
    <el-dialog
      v-model="privacyVisible"
      title="隐私政策"
      width="600px"
      class="terms-dialog"
    >
      <div class="terms-content">
        <h3>隐私保护声明</h3>
        <p>1. 我们重视用户的隐私保护。</p>
        <p>2. 收集的信息仅用于提供服务和改进体验。</p>
        <p>3. 未经用户同意，不会向第三方透露个人信息。</p>
        <p>4. 用户有权查看、修改和删除自己的信息。</p>
        <p>5. 我们采用加密技术保护用户数据安全。</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="privacyVisible = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../../api/user'

export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    const registerForm = ref(null)
    const loading = ref(false)
    const shakeForm = ref(false)
    const termsVisible = ref(false)
    const privacyVisible = ref(false)

    const form = reactive({
      username: '',
      password: '',
      confirmPassword: '',
      phone: '',
      email: '',
      agreement: false
    })

    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== form.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }

    const validateAgreement = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请阅读并同意用户协议'))
      } else {
        callback()
      }
    }

    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度应为 3-20 个字符', trigger: 'blur' },
        { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度应为 6-20 个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' }
      ],
      email: [
        { type: 'email', message: '邮箱格式错误', trigger: 'blur' }
      ],
      agreement: [
        { validator: validateAgreement, trigger: 'change' }
      ]
    }

    const handleRegister = async () => {
      const valid = await registerForm.value.validate().catch(() => false)
      if (!valid) {
        shakeForm.value = true
        setTimeout(() => shakeForm.value = false, 500)
        return
      }

      loading.value = true
      try {
        const { agreement, ...registerData } = form
        await register(registerData)
        ElMessage.success({
          message: '注册成功，正在跳转登录页...',
          duration: 2000
        })
        setTimeout(() => {
          router.push('/login')
        }, 1500)
      } catch (e) {
        shakeForm.value = true
        setTimeout(() => shakeForm.value = false, 500)
        console.log('注册失败:', e.message)
      } finally {
        loading.value = false
      }
    }

    const showTerms = () => {
      termsVisible.value = true
    }

    const showPrivacy = () => {
      privacyVisible.value = true
    }

    return {
      form,
      rules,
      registerForm,
      loading,
      shakeForm,
      termsVisible,
      privacyVisible,
      handleRegister,
      showTerms,
      showPrivacy
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

/* 背景动画 - 与登录页一致 */
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
  max-width: 460px;
  background: var(--bg-card) !important;
  border: 1px solid var(--border-color) !important;
  border-radius: var(--radius-xl) !important;
  backdrop-filter: blur(10px);
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
  box-shadow: 0 0 0 1px var(--color-primary) inset !important;
}

.auth-form :deep(.el-input__wrapper:focus-within .el-input__prefix) {
  color: var(--color-primary);
}

.auth-form :deep(.el-input__count-inner) {
  background: transparent !important;
  color: var(--text-muted);
}

.agreement-text {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.agreement-text .el-link {
  font-size: var(--font-size-sm);
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
}

.submit-btn .el-icon {
  font-size: 18px;
}

/* 底部链接 */
.auth-footer {
  text-align: center;
  color: var(--text-muted);
  font-size: var(--font-size-sm);
  margin-top: var(--space-lg);
  padding-top: var(--space-lg);
  border-top: 1px solid var(--border-color);
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

/* 协议弹窗 */
.terms-dialog :deep(.el-dialog) {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
}

.terms-dialog :deep(.el-dialog__title) {
  color: var(--text-primary);
}

.terms-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: var(--text-muted);
}

.terms-dialog :deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: var(--color-primary);
}

.terms-content {
  color: var(--text-secondary);
  line-height: 1.8;
}

.terms-content h3 {
  color: var(--text-primary);
  margin-bottom: var(--space-md);
}

.terms-content p {
  margin-bottom: var(--space-sm);
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
