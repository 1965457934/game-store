<template>
  <router-view />
</template>

<style>
/* 全局重置和基础样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  scroll-behavior: smooth;
}

body {
  font-family: var(--font-family);
  background: var(--gradient-page);
  color: var(--text-primary);
  line-height: 1.6;
  min-height: 100vh;
  letter-spacing: 0.1px;
}

body::before {
  content: '';
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: -1;
  background:
    radial-gradient(circle at 15% 20%, rgba(26, 159, 255, 0.09) 0%, transparent 25%),
    radial-gradient(circle at 85% 76%, rgba(164, 208, 7, 0.08) 0%, transparent 24%);
}

/* 深色主题下的 Element Plus 覆盖 */
.el-header {
  background: var(--bg-secondary) !important;
  border-bottom: 1px solid var(--border-color);
}

.el-main {
  background: var(--bg-primary) !important;
  padding: 0 !important;
}

.el-footer {
  background: var(--bg-secondary) !important;
  border-top: 1px solid var(--border-color);
  color: var(--text-muted) !important;
}

/* 卡片样式 */
.el-card {
  background: var(--gradient-surface) !important;
  border: 1px solid var(--border-color) !important;
  color: var(--text-primary) !important;
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-soft), var(--shadow-inset);
}

.el-card:hover {
  border-color: var(--color-primary) !important;
  box-shadow: var(--shadow-card-hover);
}

.el-card__body {
  padding: 0 !important;
}

/* 按钮样式 */
.el-button--primary {
  background: var(--gradient-primary) !important;
  border: none !important;
  color: white !important;
  font-weight: 500;
  transition: all var(--transition-normal);
}

.el-button--primary:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-glow);
  color: white !important;
}

.el-button {
  font-weight: 600;
}

.el-button--danger {
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%) !important;
  border: none !important;
  color: white !important;
}

.el-button--success {
  background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%) !important;
  border: none !important;
  color: white !important;
}

.el-button--warning {
  background: linear-gradient(135deg, #E6A23C 0%, #ebb563 100%) !important;
  border: none !important;
  color: white !important;
}

.el-button--info {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%) !important;
  border: none !important;
  color: white !important;
}

/* 输入框样式 */
.el-input__wrapper {
  background: var(--bg-tertiary) !important;
  box-shadow: 0 0 0 1px var(--border-color) inset !important;
  border-radius: var(--radius-md) !important;
  transition: box-shadow var(--transition-fast), transform var(--transition-fast);
}

.el-input__wrapper:focus-within {
  box-shadow: 0 0 0 1px var(--color-primary) inset, 0 0 0 4px rgba(26, 159, 255, 0.12) !important;
}

.el-input__inner {
  color: var(--text-primary) !important;
}

.el-input__inner::placeholder {
  color: var(--text-muted) !important;
}

/* 下拉选择器 */
.el-select .el-input__wrapper {
  background: var(--bg-tertiary) !important;
}

.el-select-dropdown {
  background: var(--bg-card) !important;
  border: 1px solid var(--border-color) !important;
}

.el-select-dropdown__item {
  color: var(--text-primary) !important;
}

.el-select-dropdown__item:hover,
.el-select-dropdown__item.selected {
  background: var(--bg-tertiary) !important;
  color: var(--color-primary) !important;
}

/* 菜单样式 */
.el-menu {
  background: transparent !important;
  border-bottom: none !important;
}

.el-menu-item {
  color: var(--text-secondary) !important;
  transition: all var(--transition-normal);
}

.el-menu-item:hover,
.el-menu-item.is-active {
  color: var(--color-primary) !important;
  background: transparent !important;
}

/* 分页样式 */
.el-pagination {
  color: var(--text-secondary) !important;
}

.el-pagination button {
  background: var(--bg-card) !important;
  color: var(--text-secondary) !important;
}

.el-pagination button:hover {
  color: var(--color-primary) !important;
}

.el-pager li {
  background: var(--bg-card) !important;
  color: var(--text-secondary) !important;
}

.el-pager li:hover,
.el-pager li.is-active {
  color: var(--color-primary) !important;
}

/* 轮播图样式 */
.el-carousel__arrow {
  background: rgba(0, 0, 0, 0.5) !important;
  color: white !important;
  transition: all var(--transition-normal);
}

.el-carousel__arrow:hover {
  background: var(--color-primary) !important;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

::-webkit-scrollbar-track {
  background: var(--bg-primary);
}

::-webkit-scrollbar-thumb {
  background: var(--bg-tertiary);
  border-radius: 5px;
  border: 2px solid var(--bg-primary);
}

::-webkit-scrollbar-thumb:hover {
  background: var(--color-primary);
}

/* 滚动条角落 */
::-webkit-scrollbar-corner {
  background: var(--bg-primary);
}
</style>
