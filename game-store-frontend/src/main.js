// 忽略 ResizeObserver 错误
const originalConsoleError = window.console.error;
window.console.error = function(...args) {
  if (args[0] && typeof args[0] === 'string' && args[0].includes('ResizeObserver')) {
    return;
  }
  originalConsoleError.apply(window.console, args);
};

// 忽略 ResizeObserver loop limit exceeded 错误
window.addEventListener('error', (e) => {
  if (e.message && e.message.includes('ResizeObserver')) {
    e.stopImmediatePropagation();
    e.preventDefault();
    return false;
  }
});

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 导入全局样式
import './styles/variables.css'
import './styles/animations.css'
import './styles/utilities.css'
import './styles/admin-common.css'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(store)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
